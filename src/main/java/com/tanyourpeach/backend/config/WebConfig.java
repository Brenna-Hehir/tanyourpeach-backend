package com.tanyourpeach.backend.config;

import com.tanyourpeach.backend.web.CorrelationIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import jakarta.servlet.DispatcherType;
import java.util.EnumSet;

@Configuration
public class WebConfig {
    @Bean
    public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilter() {
        FilterRegistrationBean<CorrelationIdFilter> reg = new FilterRegistrationBean<>();
        reg.setFilter(new CorrelationIdFilter());
        reg.setName("correlationIdFilter");
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE); // before Spring Security
        reg.setAsyncSupported(true);
        reg.setDispatcherTypes(EnumSet.of(
            DispatcherType.REQUEST,
            DispatcherType.ERROR,
            DispatcherType.FORWARD // optional, but handy for internal forwards
        ));
        return reg;
    }
}