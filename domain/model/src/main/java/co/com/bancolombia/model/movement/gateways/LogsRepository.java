package co.com.bancolombia.model.movement.gateways;

import co.com.bancolombia.model.events.UploadResult;
import reactor.core.publisher.Mono;

public interface LogsRepository {

    public Mono<UploadResult> saveLogs(UploadResult uploadResult);
}
