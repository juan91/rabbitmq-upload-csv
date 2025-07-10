package co.com.bancolombia.events.handlers;

import co.com.bancolombia.model.events.UploadResult;
import co.com.bancolombia.usecase.logsmovement.LogsMovementUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.api.domain.DomainEvent;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class EventsHandlerTest {

    @Mock
    private LogsMovementUseCase logsMovementUseCase;

    @InjectMocks
    private EventsHandler eventsHandler;



    @Test
    void handleEventATest() {
        UploadResult uploadResult = UploadResult.builder()
                .total(1)
                .success(1)
                .failed(0)
                .build();

        DomainEvent<UploadResult> event = new DomainEvent<>("EVENT", UUID.randomUUID().toString(), uploadResult);
        Mockito.when(logsMovementUseCase.saveLogs(Mockito.any())).thenReturn(Mono.just(UploadResult.builder().failed(2).success(2).total(4).build()));
        StepVerifier.create(eventsHandler.handleEventMovement(event))
                .expectComplete()
                .verify();
    }
}
