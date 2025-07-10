package co.com.bancolombia.events;

import co.com.bancolombia.model.events.gateways.EventsGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.reactivecommons.api.domain.DomainEvent;
import org.reactivecommons.api.domain.DomainEventBus;
import org.reactivecommons.async.impl.config.annotations.EnableDomainEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.logging.Level;

import static reactor.core.publisher.Mono.from;

@RequiredArgsConstructor
@EnableDomainEventBus
public class ReactiveEventsGateway implements EventsGateway {

    private static final Logger log = LoggerFactory.getLogger(ReactiveEventsGateway.class);

    public static final String SOME_EVENT_NAME = "movement.event.created";
    private final DomainEventBus domainEventBus;

    @Override
    public Mono<Void> emit(Object event) {
        log.info("Sending domain event: {}", event.toString());
         return from(domainEventBus.emit(new DomainEvent<>(SOME_EVENT_NAME, UUID.randomUUID().toString(), event)));
    }
}
