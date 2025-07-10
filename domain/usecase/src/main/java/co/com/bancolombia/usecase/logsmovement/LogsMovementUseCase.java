package co.com.bancolombia.usecase.logsmovement;

import co.com.bancolombia.model.events.UploadResult;
import co.com.bancolombia.model.movement.gateways.LogsRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LogsMovementUseCase {

    private final LogsRepository logsRepositoryl;

    public Mono<UploadResult> saveLogs(UploadResult uploadResult) {
        return logsRepositoryl.saveLogs(uploadResult);
    }
}
