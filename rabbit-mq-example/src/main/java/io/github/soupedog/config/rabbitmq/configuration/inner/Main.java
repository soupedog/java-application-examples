package io.github.soupedog.config.rabbitmq.configuration.inner;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
@ConfigurationProperties(prefix = "test.demo.rabbit.main")
public class Main {
    private String exchange;
    private String routingKey;

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }
}
