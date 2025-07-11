package co.com.bancolombia.api;

import co.com.bancolombia.model.box.Box;
import co.com.bancolombia.model.movement.CsvMovementLine;
import co.com.bancolombia.model.movement.Movement;
import co.com.bancolombia.usecase.box.BoxUseCase;
import co.com.bancolombia.usecase.uploadmovents.UploadMoventsUseCase;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class Handler {

    private final UploadMoventsUseCase uploadCsvMovementLinesUseCase;
    private final BoxUseCase boxUseCase;
    @Value("${app.fail-on-event-error:false}")
    private boolean failOnEventError;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    public Mono<ServerResponse> uploadCsvMovementLines(ServerRequest request) {
        String boxId = request.pathVariable("boxId");
        String username = request.headers().firstHeader("X-User");

        return request.multipartData()
                .flatMap(parts -> {

                    FilePart filePart = (FilePart) parts.toSingleValueMap().get("file");
                    if (filePart == null) {
                        return ServerResponse.badRequest().bodyValue("Missing 'file' part in form-data");
                    }

                    MediaType mediaType = filePart.headers().getContentType();
                    if (mediaType == null || (!mediaType.isCompatibleWith(MediaType.TEXT_PLAIN) && !mediaType.toString().equals("text/csv"))) {
                        return ServerResponse.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                                .bodyValue("Invalid content type, expected text/csv");
                    }

                    // Leer archivo completo y parsear con commons-csv
                    Flux<CsvMovementLine> CsvMovementLineLines = DataBufferUtils.join(filePart.content())
                            .flatMap(dataBuffer -> {
                                int fileSize = dataBuffer.readableByteCount();
                                if (fileSize > MAX_FILE_SIZE) {
                                    DataBufferUtils.release(dataBuffer);
                                    return Mono.error(new IllegalArgumentException(
                                            String.format("El archivo no debe exceder los 5MB. Tamaño actual: %.2f MB", fileSize / 1024.0 / 1024.0)
                                    ));
                                }
                                String content = StandardCharsets.UTF_8.decode(dataBuffer.asByteBuffer()).toString();
                                DataBufferUtils.release(dataBuffer);
                                return Mono.just(content);
                            })
                            .flatMapMany(content ->
                                    Mono.fromCallable(() -> parseCsvLines(content))
                                            .subscribeOn(Schedulers.boundedElastic())
                                            .flatMapMany(Flux::fromIterable)
                            );

                    return uploadCsvMovementLinesUseCase.uploadMovement(boxId, CsvMovementLineLines, username != null ? username : "anonymous", failOnEventError)
                            .flatMap(result -> ServerResponse.ok().bodyValue(result))
                            .onErrorResume(e -> {
                                if (e instanceof IllegalStateException && e.getMessage().equals("Caja no encontrada")) {
                                    return ServerResponse.notFound().build();
                                }
                                return Mono.error(e);
                            });
                });
    }

    private List<CsvMovementLine> parseCsvLines(String content) {
        try {
            Reader reader = new StringReader(content);
            CSVParser parser = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withTrim()
                    .parse(reader);

            List<CsvMovementLine> lines = new ArrayList<>();
            for (CSVRecord record : parser) {
                lines.add(CsvMovementLine.builder()
                        .movementId(record.get("movementId"))
                        .boxId(record.get("boxId"))
                        .date(record.get("date"))
                        .type(record.get("type"))
                        .amount(record.get("amount"))
                        .currency(record.get("currency"))
                        .description(record.get("description"))
                        .build());
            }

            return lines;
        } catch (IllegalArgumentException | IOException e) {
            throw new RuntimeException("Invalid CSV format: " + e.getMessage()); // ✅ correcto
        }
    }

    public Mono<ServerResponse> createBox(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(Box.class).flatMap(box -> boxUseCase.createBox(box.getId(), box.getName()))
                .flatMap(currentBox -> ServerResponse.ok().body(BodyInserters.fromValue(currentBox)));
    }

    public Mono<ServerResponse> movements(ServerRequest serverRequest) {
        var idBox = serverRequest.pathVariable("boxId");
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(uploadCsvMovementLinesUseCase.findByIdBox(idBox), Movement.class);
    }

}
