package co.com.bancolombia.usecase.box;

import co.com.bancolombia.model.box.Box;
import co.com.bancolombia.model.box.gateways.BoxRepository;
import co.com.bancolombia.model.movement.BoxStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BoxUseCaseTest {

    @Mock
    private BoxRepository boxRepository;

    @InjectMocks
    private BoxUseCase boxUseCase;

    private final String boxId = "BOX-001";
    private final String boxName = "Caja Principal";

    private final Box testBox = new Box.Builder()
            .id(boxId)
            .name(boxName)
            .status(BoxStatus.CLOSED)
            .currentBalance(BigDecimal.ZERO)
            .build();

    @Test
    void shouldReturnBoxWhenItExists() {
        Mockito.when(boxRepository.findById(boxId)).thenReturn(Mono.just(testBox));

        StepVerifier.create(boxUseCase.findById(boxId))
                .expectNext(testBox)
                .verifyComplete();

        Mockito.verify(boxRepository).findById(boxId);
    }

    @Test
    void shouldCreateBoxWhenItDoesNotExist() {
        Mockito.when(boxRepository.findById(boxId)).thenReturn(Mono.empty());
        Mockito.when(boxRepository.save(Mockito.any(Box.class))).thenReturn(Mono.just(testBox));

        StepVerifier.create(boxUseCase.createBox(boxId, boxName))
                .expectNextMatches(box -> box.getId().equals(boxId) && box.getName().equals(boxName))
                .verifyComplete();

        Mockito.verify(boxRepository).findById(boxId);
        Mockito.verify(boxRepository).save(Mockito.any(Box.class));
    }

    @Test
    void shouldErrorWhenBoxAlreadyExists() {
        Mockito.when(boxRepository.findById(boxId)).thenReturn(Mono.just(testBox));
        Mockito.when(boxRepository.save(Mockito.any(Box.class))).thenReturn(Mono.just(testBox));
        StepVerifier.create(boxUseCase.createBox(boxId, boxName))
                .expectErrorSatisfies(error ->
                        Assertions.assertTrue(error instanceof IllegalStateException &&
                                error.getMessage().equals("La caja ya existe")))
                .verify();


    }
}
