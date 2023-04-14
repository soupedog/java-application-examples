package io.github.soupedog.config.rabbitmq;

import io.github.soupedog.config.rabbitmq.configuration.RabbitMqConfigurationProperties;
import io.github.soupedog.config.rabbitmq.utils.RabbitmqTemplateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.AbstractConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
@Configuration
@EnableConfigurationProperties(value = {RabbitMqConfigurationProperties.class})
public class RabbitMqConfig {
    private static final Logger log = LoggerFactory.getLogger(RabbitMqConfig.class);
    private final RabbitMqConfigurationProperties properties;

    public RabbitMqConfig(@Autowired RabbitMqConfigurationProperties properties) {
        this.properties = properties;
    }

    @Primary
    @Bean("mainRabbitmqConnectionFactory")
    public CachingConnectionFactory mainRabbitmqConnectionFactory() {
        Message.setMaxBodyLength(999999999);
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        // xx.xx.xx.xx:6672,xx.xx.xx.xx:6672
        cachingConnectionFactory.setAddresses(properties.getAddress());
        cachingConnectionFactory.setVirtualHost(properties.getVirtualHost());
        // ".setShuffleAddresses(true)" is obsolete in higher versions, in latest version you should use "setAddressShuffleMode(AddressShuffleMode.RANDOM)"
        cachingConnectionFactory.setAddressShuffleMode(AbstractConnectionFactory.AddressShuffleMode.RANDOM);
        cachingConnectionFactory.setUsername(properties.getUserName());
        cachingConnectionFactory.setPassword(properties.getPassword());

        cachingConnectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.NONE);
        // rabbitTemplate.setMandatory(true) 时，检测 exchange 是否正确投递到 queue
        cachingConnectionFactory.setPublisherReturns(true);
        return cachingConnectionFactory;
    }

    @Primary
    @Bean("maiRabbitAdmin")
    public RabbitAdmin admin(@Qualifier("mainRabbitmqConnectionFactory") CachingConnectionFactory mainRabbitmqConnectionFactory) {
        return new RabbitAdmin(mainRabbitmqConnectionFactory);
    }

    @Primary
    @Bean("mainRabbitTemplate")
    public RabbitTemplate mainRabbitTemplate(@Qualifier("mainRabbitmqConnectionFactory") CachingConnectionFactory mainRabbitmqConnectionFactory) {
        return RabbitmqTemplateBuilder.buildRabbitTemplate(mainRabbitmqConnectionFactory, log, "main");
    }
}
