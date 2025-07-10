package co.com.bancolombia.usecase.logsmovement;

import static org.junit.jupiter.api.Assertions.*;
import co.com.bancolombia.model.box.Box;
import co.com.bancolombia.model.box.gateways.BoxRepository;
import co.com.bancolombia.model.events.UploadResult;
import co.com.bancolombia.model.movement.BoxStatus;
import co.com.bancolombia.model.movement.gateways.LogsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class LogsMovementUseCaseTest {

    @Mock
    private LogsRepository logsRepositoryl;

    @InjectMocks
    private LogsMovementUseCase logsMovementUseCase;

    @Test
    void saveLogs_shouldReturnUploadResult() {
        UploadResult mockResult = UploadResult.builder()
                .total(10)
                .success(8)
                .failed(2)
                .errors(List.of(
                        UploadResult.ErrorLine.builder()
                                .lineNumber(5)
                                .reason("Formato inválido")
                                .rawLine("linea original")
                                .build()
                ))
                .build();

        Mockito.when(logsRepositoryl.saveLogs(mockResult)).thenReturn(Mono.just(mockResult));

        StepVerifier.create(logsMovementUseCase.saveLogs(mockResult))
                .expectNext(mockResult)
                .verifyComplete();

        Mockito.verify(logsRepositoryl).saveLogs(mockResult);
    }
}