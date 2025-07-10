package co.com.bancolombia.events;

import co.com.bancolombia.events.handlers.EventsHandler;
import co.com.bancolombia.usecase.logsmovement.LogsMovementUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.reactivecommons.async.api.HandlerRegistry;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class HandlerRegistryConfigurationTest {

    @Mock
    private LogsMovementUseCase logsMovementUseCase;

    @InjectMocks
    private EventsHandler eventsHandler;


    @Test
    void testHandlerRegistry() {
        HandlerRegistryConfiguration handlerRegistryConfiguration = new HandlerRegistryConfiguration();
        HandlerRegistry handlerRegistry = handlerRegistryConfiguration.handlerRegistry(eventsHandler);

        assertNotNull(handlerRegistry);
    }
}
