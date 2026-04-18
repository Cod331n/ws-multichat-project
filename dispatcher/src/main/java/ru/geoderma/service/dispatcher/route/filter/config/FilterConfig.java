package ru.geoderma.service.dispatcher.route.filter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.geoderma.service.dispatcher.route.filter.MessageFilter;
import ru.geoderma.service.dispatcher.route.filter.impl.EchoFilter;
import ru.geoderma.service.dispatcher.route.filter.impl.ForbiddenPlatformFilter;

import java.util.List;

@Configuration
public class FilterConfig {

    @Bean
    public List<MessageFilter> orderedFilters(final EchoFilter echoFilter,
                                              final ForbiddenPlatformFilter forbiddenFilter) {
        return List.of(echoFilter, forbiddenFilter);
    }

}