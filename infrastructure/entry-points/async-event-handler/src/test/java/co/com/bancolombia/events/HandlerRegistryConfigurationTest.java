package co.com.bancolombia.events;

import co.com.bancolombia.events.handlers.EventsHandler;
import co.com.bancolombia.usecase.logsmovement.LogsMovementUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.async.api.HandlerRegistry;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class) // si usas JUnit 5
class HandlerRegistryConfigurationTest {

    @Mock
    private LogsMovementUseCase logsMovementUseCase;

    @InjectMocks
    private EventsHandler eventsHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // inicializa los mocks y el InjectMocks
    }

    @Test
    void testHandlerRegistry() {
        HandlerRegistryConfiguration handlerRegistryConfiguration = new HandlerRegistryConfiguration();
        HandlerRegistry handlerRegistry = handlerRegistryConfiguration.handlerRegistry(eventsHandler);
        assertNotNull(handlerRegistry);
    }
}
