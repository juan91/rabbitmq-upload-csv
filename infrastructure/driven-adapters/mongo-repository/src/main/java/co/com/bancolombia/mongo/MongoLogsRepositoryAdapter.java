package co.com.bancolombia.mongo;

import co.com.bancolombia.model.events.UploadResult;
import co.com.bancolombia.model.movement.gateways.LogsRepository;
import co.com.bancolombia.mongo.data.UploadResultData;
import co.com.bancolombia.mongo.helper.AdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class MongoLogsRepositoryAdapter extends AdapterOperations<UploadResult, UploadResultData, String, MongoLogsDBRepository>
 implements LogsRepository
{
    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    public MongoLogsRepositoryAdapter(MongoLogsDBRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, UploadResult.class));
    }

    @Override
    public Mono<UploadResult> saveLogs(UploadResult uploadResult) {
        return repository.save(toDocument(uploadResult)).map(this::toDto);
    }

    public UploadResultData toDocument(UploadResult dto) {
        return UploadResultData.builder()
                .total(dto.getTotal())
                .success(dto.getSuccess())
                .failed(dto.getFailed())
                .errors(
                        dto.getErrors().stream()
                                .map(err -> UploadResultData.ErrorLine.builder()
                                        .lineNumber(err.getLineNumber())
                                        .reason(err.getReason())
                                        .rawLine(err.getRawLine())
                                        .build())
                                .toList())
                .build();
    }

    public UploadResult toDto(UploadResultData doc) {
        return UploadResult.builder()
                .total(doc.getTotal())
                .success(doc.getSuccess())
                .failed(doc.getFailed())
                .errors(
                        doc.getErrors().stream()
                                .map(err -> UploadResult.ErrorLine.builder()
                                        .lineNumber(err.getLineNumber())
                                        .reason(err.getReason())
                                        .rawLine(err.getRawLine())
                                        .build())
                                .toList())
                .build();
    }
}
