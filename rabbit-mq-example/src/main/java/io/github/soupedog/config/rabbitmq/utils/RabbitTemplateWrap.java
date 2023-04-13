package io.github.soupedog.config.rabbitmq.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
@Slf4j
@Component
public class RabbitTemplateWrap implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RabbitTemplate) {
            RabbitTemplate currentBean = (RabbitTemplate) bean;
            log.info("RabbitTemplateWrap----" + beanName);
        }

        return bean;
    }
}
