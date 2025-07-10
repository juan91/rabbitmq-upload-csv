package co.com.bancolombia.usecase.box;

import co.com.bancolombia.model.box.Box;
import co.com.bancolombia.model.box.gateways.BoxRepository;
import co.com.bancolombia.model.movement.BoxStatus;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class BoxUseCase {

    private final BoxRepository boxRepository;

    public Mono<Box> findById(String id) {
        return boxRepository.findById(id);
    }

    public Mono<Box> createBox(String id, String name) {
        return boxRepository.findById(id)
                .flatMap(existing -> Mono.<Box>error(new IllegalStateException("La caja ya existe")))
                .switchIfEmpty(
                        boxRepository.save(new Box.Builder()
                                        .id(id)
                                        .name(name)
                                        .status(BoxStatus.CLOSED)
                                        .currentBalance(BigDecimal.ZERO)
                                        .build())

                );
    }
}
