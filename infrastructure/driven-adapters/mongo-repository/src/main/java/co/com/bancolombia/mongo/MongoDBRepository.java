package co.com.bancolombia.mongo;

import co.com.bancolombia.mongo.data.BoxData;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;

public interface MongoDBRepository extends ReactiveMongoRepository<BoxData, String>, ReactiveQueryByExampleExecutor<BoxData> {
}
