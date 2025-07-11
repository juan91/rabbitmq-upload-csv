package co.com.bancolombia.usecase.uploadmovents;

import co.com.bancolombia.model.BoxMovementsUploadedEvent;
import co.com.bancolombia.model.box.Box;
import co.com.bancolombia.model.box.gateways.BoxRepository;
import co.com.bancolombia.model.events.exceptionDomain.EventPublishException;
import co.com.bancolombia.model.events.gateways.EventsGateway;
import co.com.bancolombia.model.movement.CsvMovementLine;
import co.com.bancolombia.model.movement.Movement;
import co.com.bancolombia.model.movement.MovementType;
import co.com.bancolombia.model.movement.gateways.MovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@ExtendWith(MockitoExtension.class)
class UploadMoventsUseCaseTest {

    @Mock
    private MovementRepository movementRepository;

    @Mock
    private BoxRepository boxRepository;

    @Mock
    private EventsGateway eventsGateway;

    private UploadMoventsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UploadMoventsUseCase(movementRepository, boxRepository, eventsGateway);
    }

    @Test
    void debeLanzarErrorCuandoCajaNoExiste() {
        String boxId = "BOX-1";
        Mockito.when(boxRepository.findById(boxId)).thenReturn(Mono.empty());

        Mono<BoxMovementsUploadedEvent> result = useCase.uploadMovement(boxId, Flux.empty(), "user", true);

        StepVerifier.create(result)
                .expectErrorMatches(e -> e instanceof IllegalStateException && e.getMessage().equals("Caja no encontrada"))
                .verify();
    }

    @Test
    void debeGuardarMovimientoYEmitirEvento() {
        String boxId = "BOX-1";
        CsvMovementLine line = createValidLine(boxId);

        Mockito.when(boxRepository.findById(boxId)).thenReturn(Mono.just(new Box()));
        Mockito.when(movementRepository.save(Mockito.any(Movement.class))).thenReturn(Mono.just(createMovementFrom(line)));
        Mockito.when(eventsGateway.emit(Mockito.any())).thenReturn(Mono.empty());

        Mono<BoxMovementsUploadedEvent> result = useCase.uploadMovement(boxId, Flux.just(line), "admin", false);

        StepVerifier.create(result)
                .expectNextMatches(event ->
                        event.getBoxId().equals(boxId)
                                && event.getSuccessCount() == 1
                                && event.getFailedCount() == 0)
                .verifyComplete();
    }

    @Test
    void debeCapturarErrorDeMovimientoInvalido() {
        String boxId = "BOX-1";
        CsvMovementLine invalidLine = new CsvMovementLine(); // campos vacíos

        Mockito.when(boxRepository.findById(boxId)).thenReturn(Mono.just(new Box()));
        Mockito.when(eventsGateway.emit(Mockito.any())).thenReturn(Mono.empty());

        Mono<BoxMovementsUploadedEvent> result = useCase.uploadMovement(boxId, Flux.just(invalidLine), "user", false);

        StepVerifier.create(result)
                .expectNextMatches(event -> event.getSuccessCount() == 0 && event.getFailedCount() == 1)
                .verifyComplete();
    }

    @Test
    void debeHacerRollbackSiFallaEventoYFlagActivo() {
        String boxId = "BOX-1";
        CsvMovementLine line = createValidLine(boxId);

        Mockito.when(boxRepository.findById(boxId)).thenReturn(Mono.just(new Box()));
        Mockito.when(movementRepository.save(Mockito.any(Movement.class))).thenReturn(Mono.just(createMovementFrom(line)));
        Mockito.when(eventsGateway.emit(Mockito.any())).thenReturn(Mono.error(new RuntimeException("Error evento")));
        Mockito.when(movementRepository.deleteByBoxIdAndMovementId(Mockito.eq(boxId), Mockito.any())).thenReturn(Mono.empty());

        Mono<BoxMovementsUploadedEvent> result = useCase.uploadMovement(boxId, Flux.just(line), "user", true);

        StepVerifier.create(result)
                .expectError(EventPublishException.class)
                .verify();
    }

    @Test
    void noHaceRollbackSiFallaEventoYFlagDesactivado() {
        String boxId = "BOX-1";
        CsvMovementLine line = createValidLine(boxId);

        Mockito.when(boxRepository.findById(boxId)).thenReturn(Mono.just(new Box()));
        Mockito.when(movementRepository.save(Mockito.any(Movement.class))).thenReturn(Mono.just(createMovementFrom(line)));
        Mockito.when(eventsGateway.emit(Mockito.any())).thenReturn(Mono.error(new RuntimeException("Error evento")));

        Mono<BoxMovementsUploadedEvent> result = useCase.uploadMovement(boxId, Flux.just(line), "user", false);

        StepVerifier.create(result)
                .expectNextMatches(event -> event.getSuccessCount() == 1 && event.getFailedCount() == 0)
                .verifyComplete();
    }

    // utilidades para pruebas

    private CsvMovementLine createValidLine(String boxId) {
        CsvMovementLine line = new CsvMovementLine();
        line.setBoxId(boxId);
        line.setMovementId(UUID.randomUUID().toString());
        line.setDate(LocalDateTime.now().toString());
        line.setAmount("1000");
        line.setType("INCOME");
        line.setCurrency("COP");
        line.setDescription("Ingreso válido");
        return line;
    }

    private Movement createMovementFrom(CsvMovementLine line) {
        return Movement.builder()
                .movementId(line.getMovementId())
                .boxId(line.getBoxId())
                .date(LocalDateTime.parse(line.getDate()))
                .amount(new BigDecimal(line.getAmount()))
                .currency(line.getCurrency())
                .type(MovementType.valueOf(line.getType()))
                .description(line.getDescription())
                .build();
    }
}