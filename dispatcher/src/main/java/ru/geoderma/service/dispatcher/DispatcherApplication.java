package ru.geoderma.service.dispatcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.geoderma.service.dispatcher.handler.ServerWebSocketHandler;

@Slf4j
@SpringBootApplication
public class DispatcherApplication {

    static void main(final String... args) {
        log.info("Starting dispatcher...");
        ConfigurableApplicationContext context = SpringApplication.run(DispatcherApplication.class, args);

        Runtime.getRuntime()
                .addShutdownHook(new Thread(() -> context.getBean(ServerWebSocketHandler.class).disconnect()));
    }

}
