package co.com.bancolombia.model.movement.gateways;

import co.com.bancolombia.model.events.UploadResult;
import co.com.bancolombia.model.movement.Movement;
import reactor.core.publisher.Mono;

public interface MovementRepository {

    public Mono<Movement> save(Movement movement);

}
