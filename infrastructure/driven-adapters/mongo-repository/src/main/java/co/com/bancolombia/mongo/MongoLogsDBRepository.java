package co.com.bancolombia.mongo;

import co.com.bancolombia.mongo.data.UploadResultData;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;

public interface MongoLogsDBRepository extends ReactiveMongoRepository<UploadResultData, String>, ReactiveQueryByExampleExecutor<UploadResultData> {
}
