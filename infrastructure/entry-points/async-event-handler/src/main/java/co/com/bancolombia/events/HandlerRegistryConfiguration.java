package co.com.bancolombia.events;
import co.com.bancolombia.events.handlers.EventsHandler;
import co.com.bancolombia.model.events.UploadResult;
import org.reactivecommons.async.api.HandlerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HandlerRegistryConfiguration {

    // see more at: https://reactivecommons.org/reactive-commons-java/#_handlerregistry_2
    @Bean
    public HandlerRegistry handlerRegistry(EventsHandler events) {
        return HandlerRegistry.register()
                .listenEvent("movement.event.created", events::handleEventMovement, UploadResult.class);
    }
}
