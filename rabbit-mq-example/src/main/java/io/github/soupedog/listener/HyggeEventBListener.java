package io.github.soupedog.listener;

import io.github.soupedog.domain.User;
import io.github.soupedog.listener.base.HyggeChannelAwareMessageListener;
import io.github.soupedog.listener.base.HyggeRabbitMqListenerContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
@Slf4j
@Component
public class HyggeEventBListener extends HyggeChannelAwareMessageListener<User> {
    public HyggeEventBListener(@Value("${test.demo.rabbit.environment-name}") String environmentName) {
        super("HyggeEventB", environmentName);
    }

    @Override
    public User formatMessageAsEntity(HyggeRabbitMqListenerContext context, String messageStringVal) {
        User user = jsonHelper.readAsObject(messageStringVal, User.class);
        MDC.put("traceId", user.getUid());
        return user;
    }

    @Override
    public void onReceive(HyggeRabbitMqListenerContext context, User messageEntity) {
    }

    @Override
    public void finallyHook(HyggeRabbitMqListenerContext context) {
        try {
            MDC.remove("traceId");
        } catch (Exception e) {
            log.error("Unexpected exception.", e);
        }
    }
}
