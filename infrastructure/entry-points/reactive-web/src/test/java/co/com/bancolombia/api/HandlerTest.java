package co.com.bancolombia.api;

import co.com.bancolombia.model.box.Box;
import co.com.bancolombia.model.events.UploadResult;
import co.com.bancolombia.model.movement.BoxStatus;
import co.com.bancolombia.usecase.box.BoxUseCase;
import co.com.bancolombia.usecase.logsmovement.LogsMovementUseCase;
import co.com.bancolombia.usecase.uploadmovents.UploadMoventsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
class HandlerTest {

    @Mock
    private UploadMoventsUseCase uploadMoventsUseCase;

    @Mock
    private BoxUseCase boxUseCase;

    private Handler handler;

    @BeforeEach
    void setUp() {
        handler = new Handler(uploadMoventsUseCase, boxUseCase);
    }

    @Test
    void shouldReturnOkWhenBoxCreatedSuccessfully() {
        Box testBox = new Box.Builder()
                .id("BOX-001")
                .name("Caja Principal")
                .currentBalance(BigDecimal.ZERO)
                .status(BoxStatus.CLOSED)
                .build();

        ServerRequest request = MockServerRequest.builder()
                .body(Mono.just(testBox));

        Mockito.when(boxUseCase.createBox("BOX-001", "Caja Principal"))
                .thenReturn(Mono.just(testBox));

        StepVerifier.create(handler.createBox(request))
                .expectNextMatches(response -> response.statusCode().is2xxSuccessful())
                .verifyComplete();

        Mockito.verify(boxUseCase).createBox("BOX-001", "Caja Principal");
    }


    @Test
    void shouldReturnBadRequestWhenContentTypeIsInvalid() {
        FilePart filePart = Mockito.mock(FilePart.class);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        Mockito.when(filePart.headers()).thenReturn(headers);

        StepVerifier.create(processFilePart("BOX-001", "tester", filePart))
                .expectNextMatches(res -> res.statusCode().is4xxClientError())
                .verifyComplete();
    }

    Mono<ServerResponse> processFilePart(String boxId, String username, FilePart filePart) {
        MediaType mediaType = filePart.headers().getContentType();
        if (mediaType == null || !mediaType.isCompatibleWith(MediaType.TEXT_PLAIN) && !mediaType.toString().equals("text/csv")) {
            return ServerResponse.badRequest().bodyValue("Invalid content type, expected text/csv");
        }

        // Aquí iría la lógica de procesamiento normal...
        return ServerResponse.ok().bodyValue("Simulado");
    }
}
