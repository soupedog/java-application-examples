package io.github.soupedog.listener;

import io.github.soupedog.domain.User;
import io.github.soupedog.listener.base.HyggeChannelAwareMessageListener;
import io.github.soupedog.listener.base.HyggeRabbitMqListenerContext;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
@Component
public class HyggeEventAListener extends HyggeChannelAwareMessageListener<User> {
    public HyggeEventAListener(@Value("${test.demo.rabbit.environment-name}") String environmentName) {
        super("HyggeEventA", environmentName);
    }

    @Override
    public User formatMessageAsEntity(HyggeRabbitMqListenerContext context, String messageStringVal) {
        User user = jsonHelper.readAsObject(messageStringVal, User.class);
        MDC.put("traceId", user.getUid());
        return user;
    }

    @Override
    public void onReceive(HyggeRabbitMqListenerContext context, User messageEntity) throws Exception {
        Thread.sleep(10000);
    }

    @Override
    public void finishHook(HyggeRabbitMqListenerContext context) {
        MDC.remove("traceId");
    }
}
