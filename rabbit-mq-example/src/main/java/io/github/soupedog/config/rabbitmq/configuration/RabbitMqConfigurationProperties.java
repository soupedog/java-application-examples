package io.github.soupedog.config.rabbitmq.configuration;

import io.github.soupedog.config.rabbitmq.configuration.inner.Annotation;
import io.github.soupedog.config.rabbitmq.configuration.inner.EventBus;
import io.github.soupedog.config.rabbitmq.configuration.inner.Main;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
@ConfigurationProperties(prefix = "test.demo.rabbit")
public class RabbitMqConfigurationProperties {
    private String environmentName;
    private String address;
    private String virtualHost;
    private String userName;
    private String password;
    private Main main;
    private EventBus eventBus;
    private Annotation annotation;

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }
}
