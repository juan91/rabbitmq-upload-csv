package co.com.bancolombia.usecase.uploadmovents;

import co.com.bancolombia.model.BoxMovementsUploadedEvent;
import co.com.bancolombia.model.events.gateways.EventsGateway;
import co.com.bancolombia.model.movement.CsvMovementLine;
import co.com.bancolombia.model.movement.Movement;
import co.com.bancolombia.model.movement.MovementType;
import co.com.bancolombia.model.events.UploadResult;
import co.com.bancolombia.model.box.gateways.BoxRepository;
import co.com.bancolombia.model.movement.gateways.MovementRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


@RequiredArgsConstructor
public class UploadMoventsUseCase {

    private final MovementRepository movementRepository;
    private final BoxRepository boxRepository;
    private final EventsGateway eventsGateway;

    /**
     * Carga movimientos de la caja
     * @param boxId
     * @param csvLines
     * @param username
     * @return Mono<BoxMovementsUploadedEvent>
     */
    public Mono<BoxMovementsUploadedEvent> uploadMovement(String boxId, Flux<CsvMovementLine> csvLines, String username) {
        return boxRepository.findById(boxId)
                .switchIfEmpty(Mono.error(new IllegalStateException("Caja no encontrada")))
                .flatMap(box -> {
                    Set<String> movementIds = ConcurrentHashMap.newKeySet();
                    AtomicInteger successCount = new AtomicInteger(0);
                    AtomicInteger lineNumber = new AtomicInteger(1); // empieza en 1 después del header
                    List<UploadResult.ErrorLine> errorLines = Collections.synchronizedList(new ArrayList<>());

                    return csvLines
                            .flatMap(csvLine -> {
                                int currentLine = lineNumber.getAndIncrement();
                                return validateAndMap(csvLine, movementIds, boxId)
                                        .flatMap(validMovement ->
                                                movementRepository.save(validMovement)
                                                        .then(Mono.fromCallable(() -> {
                                                            successCount.incrementAndGet();
                                                            return true;
                                                        }))
                                        )
                                        .onErrorResume(e -> {
                                            errorLines.add(UploadResult.ErrorLine.builder()
                                                    .lineNumber(currentLine)
                                                    .reason(e.getMessage())
                                                    .rawLine(csvLine.toString()) // opcional
                                                    .build());
                                            return Mono.just(false);
                                        });
                            })
                            .then(Mono.defer(() -> {
                                int total = successCount.get() + errorLines.size();
                                UploadResult result = UploadResult.builder()
                                        .total(total)
                                        .success(successCount.get())
                                        .failed(errorLines.size())
                                        .errors(errorLines)
                                        .build();

                                // Emitir evento
                                BoxMovementsUploadedEvent event = BoxMovementsUploadedEvent.builder()
                                        .boxId(boxId)
                                        .totalLines(result.getTotal())
                                        .successCount(result.getSuccess())
                                        .failedCount(result.getFailed())
                                        .timestamp(LocalDateTime.now())
                                        .uploadedBy(username)
                                        .build();

                                return eventsGateway.emit(result)
                                        .thenReturn(event)
                                        .onErrorResume(err -> {
                                            System.out.println("Fallo al emitir evento, pero se continúa: "+ err.getMessage());
                                            return Mono.just(event);
                                        });
                            }));
                });

    }

    /**
     * Valida cada fila con respecto a las reglas de validación
     * @param line
     * @param movementIds
     * @param expectedBoxId
     * @return Mono<Movement>
     */
    private Mono<Movement> validateAndMap(CsvMovementLine line, Set<String> movementIds, String expectedBoxId) {
        return Mono.fromCallable(() -> {

            // boxId: debe coincidir con el que se está procesando
            if (!expectedBoxId.equals(line.getBoxId())) {
                throw new IllegalArgumentException("boxId mismatch: " + line.getBoxId());
            }

            // movementId: obligatorio, único por archivo.
            if (line.getMovementId() == null || line.getMovementId().isBlank() || line.getMovementId().equalsIgnoreCase("null")) {
                throw new IllegalArgumentException("movementId is required");
            }

            if (!movementIds.add(line.getMovementId())) {
                throw new IllegalArgumentException("movementId is duplicated: " + line.getMovementId());
            }

            // date: formato ISO 8601.
            LocalDateTime date;
            try {
                date = LocalDateTime.parse(line.getDate());
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format: " + line.getDate());
            }

            // type: INCOME o EXPENSE
            MovementType type;
            try {
                type = MovementType.valueOf(line.getType().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid type: " + line.getType());
            }

            // amount: numérico positivo.
            BigDecimal amount;
            try {
                amount = new BigDecimal(line.getAmount());
                if (amount.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Amount must be positive");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid amount: " + line.getAmount());
            }

            // currency: COP, USD o permitido por política.
            if (!List.of("COP", "USD").contains(line.getCurrency())) {
                throw new IllegalArgumentException("Unsupported currency: " + line.getCurrency());
            }

            // description: texto descriptivo no vacío.
            if (line.getDescription() == null || line.getDescription().isBlank() || line.getDescription().length() < 5) {
                throw new IllegalArgumentException("Description is required OR description must be valid");
            }

            return Movement.builder()
                    .movementId(line.getMovementId())
                    .boxId(line.getBoxId())
                    .date(date)
                    .type(type)
                    .amount(amount)
                    .currency(line.getCurrency())
                    .description(line.getDescription())
                    .build();
        });
    }

}
