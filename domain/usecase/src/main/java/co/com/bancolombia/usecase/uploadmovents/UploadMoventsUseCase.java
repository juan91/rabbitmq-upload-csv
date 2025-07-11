package co.com.bancolombia.usecase.uploadmovents;

import co.com.bancolombia.model.BoxMovementsUploadedEvent;
import co.com.bancolombia.model.events.exceptionDomain.EventPublishException;
import co.com.bancolombia.model.events.gateways.EventsGateway;
import co.com.bancolombia.model.movement.CsvMovementLine;
import co.com.bancolombia.model.movement.Movement;
import co.com.bancolombia.model.events.UploadResult;
import co.com.bancolombia.model.box.gateways.BoxRepository;
import co.com.bancolombia.model.movement.MovementValidator;
import co.com.bancolombia.model.movement.gateways.MovementRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.*;
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
    public Mono<BoxMovementsUploadedEvent> uploadMovement(String boxId, Flux<CsvMovementLine> csvLines, String username, boolean failOnEventError) {
        return boxRepository.findById(boxId)
                .switchIfEmpty(Mono.error(new IllegalStateException("Caja no encontrada")))
                .flatMap(box -> {
                    Set<String> movementIds = ConcurrentHashMap.newKeySet();
                    AtomicInteger successCount = new AtomicInteger(0);
                    AtomicInteger lineNumber = new AtomicInteger(1); // empieza en 1 después del header
                    List<UploadResult.ErrorLine> errorLines = Collections.synchronizedList(new ArrayList<>());
                    Set<String> successfulMovementIds = ConcurrentHashMap.newKeySet(); // Para rollback si fallla

                    return csvLines
                            .flatMap(csvLine -> {
                                int currentLine = lineNumber.getAndIncrement();
                                return Mono.fromCallable(() ->
                                                MovementValidator.validateAndMap(csvLine, movementIds, boxId)
                                        )
                                        .flatMap(validMovement ->
                                                movementRepository.save(validMovement)
                                                        .doOnSuccess(saved -> successfulMovementIds.add(saved.getMovementId()))
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
                            .then(Mono.defer(() ->
                                emitirReporteFinal(boxId, username, failOnEventError, successCount, errorLines, successfulMovementIds)
                            ));
                });

    }

    private Mono<BoxMovementsUploadedEvent> emitirReporteFinal(String boxId,
                                                               String username,
                                                               boolean failOnEventError,
                                                               AtomicInteger successCount,
                                                               List<UploadResult.ErrorLine> errorLines,
                                                               Set<String> successfulMovementIds) {

        int total = successCount.get() + errorLines.size();

        UploadResult result = UploadResult.builder()
                .boxId(boxId)
                .total(total)
                .success(successCount.get())
                .failed(errorLines.size())
                .errors(errorLines)
                .build();

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
                    if (failOnEventError) {
                        return Flux.fromIterable(successfulMovementIds)
                                .flatMap(movementId -> movementRepository.deleteByBoxIdAndMovementId(boxId, movementId))
                                .then(Mono.error(new EventPublishException(
                                        "Rollback ejecutado correctamente: los movimientos validos fueron revertidos debido a un error en la emision del evento.", err)));
                    } else {
                        System.out.println("Error al emitir evento -> " + err.getMessage());
                        return Mono.just(event);
                    }
                });
    }

    public Flux<Movement> findByIdBox(String idBox) {
        return movementRepository.findByIdBox(idBox);
    }

}
