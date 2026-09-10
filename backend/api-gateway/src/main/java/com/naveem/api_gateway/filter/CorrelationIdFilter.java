package com.naveem.api_gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final String CORRELATION_ID = "X-Correlation-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID);

        if(correlationId == null || correlationId.isBlank())
            correlationId = UUID.randomUUID().toString();

        String finalCorrelationId = correlationId;
        ServerWebExchange modifiedExchange = exchange.mutate().request(request ->
                                                                    request.headers(headers ->
                                                                            headers.set(CORRELATION_ID, finalCorrelationId)))
                                                                    .build();

        modifiedExchange.getResponse().getHeaders().set(CORRELATION_ID, correlationId);

        long startTime = System.currentTimeMillis();

        log.info("Incoming Request | correlationId={} | method={} | path={}",
                correlationId,
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI().getPath());

        String finalCorrelationId1 = correlationId;
        return chain.filter(modifiedExchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;

            log.info("Completed Request | correlationId={} | status={} | duration={}ms",
                    finalCorrelationId1,
                    modifiedExchange.getResponse().getStatusCode(),
                    duration);
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
