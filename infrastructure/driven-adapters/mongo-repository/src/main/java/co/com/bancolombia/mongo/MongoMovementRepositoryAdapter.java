package co.com.bancolombia.mongo;

import co.com.bancolombia.model.movement.Movement;
import co.com.bancolombia.model.movement.gateways.MovementRepository;
import co.com.bancolombia.mongo.data.MovementData;
import co.com.bancolombia.mongo.helper.AdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MongoMovementRepositoryAdapter extends AdapterOperations<Movement, MovementData, String, MongoDBMovementRepository>
 implements MovementRepository
{
    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    public MongoMovementRepositoryAdapter(MongoDBMovementRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Movement.class));
    }

}
