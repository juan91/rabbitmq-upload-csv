package co.com.bancolombia.api.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@Order(-2)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "Error interno";

        if (ex instanceof IllegalStateException) {
            status = HttpStatus.BAD_REQUEST;
            message = ex.getMessage();
        }

        Map<String, Object> errorResponse = Map.of(
                "status", status.value(),
                "message", message,
                "error", status.getReasonPhrase()
        );

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            return exchange.getResponse().writeWith(Mono.just(bufferFactory.wrap(bytes)));
        } catch (Exception e) {
            return exchange.getResponse().setComplete();
        }
    }
}