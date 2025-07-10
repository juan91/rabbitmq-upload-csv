package co.com.bancolombia.api;

import co.com.bancolombia.model.BoxMovementsUploadedEvent;
import co.com.bancolombia.model.box.Box;
import co.com.bancolombia.model.movement.BoxStatus;
import co.com.bancolombia.usecase.box.BoxUseCase;
import co.com.bancolombia.usecase.uploadmovents.UploadMoventsUseCase;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;



@ExtendWith(MockitoExtension.class)
class RouterRestTest {

    @Mock
    private UploadMoventsUseCase uploadCsvMovementLinesUseCase;

    @Mock
    private BoxUseCase boxUseCase;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        Handler handler = new Handler(uploadCsvMovementLinesUseCase, boxUseCase);

        // Usa el router real que sí contiene ambas rutas
        RouterRest routerRest = new RouterRest();
        RouterFunction<ServerResponse> routerFunction = routerRest.routerFunction(handler);

        webTestClient = WebTestClient.bindToRouterFunction(routerFunction).build();
    }

    @Test
    void shouldCreateBoxSuccessfully() {
        // Arrange
        Box box = new Box.Builder().id("BOX-001").name("Caja Principal").build();
        Mockito.when(boxUseCase.createBox(Mockito.eq("BOX-001"), Mockito.eq("Caja Principal"))).thenReturn(Mono.just(box));

        // Act & Assert
        webTestClient.post()
                .uri("/api")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"id\": \"BOX-001\", \"name\": \"Caja Principal\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo("BOX-001")
                .jsonPath("$.name").isEqualTo("Caja Principal");

        Mockito.verify(boxUseCase).createBox("BOX-001", "Caja Principal");
    }

}

