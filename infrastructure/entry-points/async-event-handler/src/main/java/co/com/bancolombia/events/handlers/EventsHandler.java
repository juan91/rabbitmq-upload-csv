package co.com.bancolombia.events.handlers;

import co.com.bancolombia.model.events.UploadResult;
import co.com.bancolombia.usecase.logsmovement.LogsMovementUseCase;
import lombok.AllArgsConstructor;
import org.reactivecommons.api.domain.DomainEvent;
import org.reactivecommons.async.impl.config.annotations.EnableEventListeners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@AllArgsConstructor
@EnableEventListeners
public class EventsHandler {

    private static final Logger log = LoggerFactory.getLogger(EventsHandler.class);
    private final LogsMovementUseCase logsMovementUseCase;

    public Mono<Void> handleEventMovement(DomainEvent<UploadResult> event) {
        UploadResult result = event.getData();

        String errorSummary = result.getErrors() == null ? "[]" :
                result.getErrors().stream()
                        .map(err -> String.format("{line: %d, reason: %s}", err.getLineNumber(), err.getReason()))
                        .collect(Collectors.joining(", ", "[", "]"));

        log.info("Event received -> Total: {}, Success: {}, Failed: {}, Errors: {}",
                result.getTotal(), result.getSuccess(), result.getFailed(), errorSummary);

        return logsMovementUseCase.saveLogs(event.getData())
                .doOnError(e -> log.error("Error saving logs result", e))
                .doOnSuccess(e -> log.error("log registered success :)"))
                .then();
    }
}
