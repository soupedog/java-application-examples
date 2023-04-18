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
public class HyggeEventAListener extends HyggeChannelAwareMessageListener<User> {
    public HyggeEventAListener(@Value("${test.demo.rabbit.environment-name}") String environmentName) {
        super("HyggeEventA", environmentName);
    }

    @Override
    public User formatAsEntity(HyggeRabbitMqListenerContext<User> context) {
        User user = jsonHelper.readAsObject(context.getRwaMessage().getMessageStringVal(), User.class);
        MDC.put("traceId", user.getUid());
        return user;
    }

    @Override
    public void onReceive(HyggeRabbitMqListenerContext<User> context, User messageEntity) throws Exception {
        Thread.sleep(10000);
    }

    @Override
    public void finallyHook(HyggeRabbitMqListenerContext<User> context) {
        try {
            MDC.remove("traceId");
        } catch (Exception e) {
            log.error("Unexpected exception.", e);
        }
    }
}
