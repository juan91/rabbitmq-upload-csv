package co.com.bancolombia.mongo;

import co.com.bancolombia.mongo.data.MovementData;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MongoDBMovementRepository extends ReactiveMongoRepository<MovementData, String>, ReactiveQueryByExampleExecutor<MovementData> {

    Flux<MovementData> findByBoxId(String boxId);
    Mono<Void> deleteByBoxIdAndMovementId(String boxId, String movementId);
}
