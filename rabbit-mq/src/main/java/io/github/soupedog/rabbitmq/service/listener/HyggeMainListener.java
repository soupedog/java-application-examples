package io.github.soupedog.rabbitmq.service.listener;

import io.github.soupedog.rabbitmq.domain.User;
import io.github.soupedog.rabbitmq.service.listener.base.HyggeChannelAwareMessageListener;
import io.github.soupedog.rabbitmq.service.listener.base.HyggeRabbitMqListenerContext;
import io.github.soupedog.rabbitmq.service.listener.base.definition.HyggeListenerBaseFeature;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
@Component
public class HyggeMainListener extends HyggeChannelAwareMessageListener<User> {
    public HyggeMainListener(@Value("${test.demo.rabbit.environment-name}") String environmentName) {
        super("HyggeMain", environmentName);
    }

    @Override
    public User formatAsEntity(HyggeRabbitMqListenerContext<User> context, String messageStringVal) {
        User user = HyggeListenerBaseFeature.jsonHelper.readAsObject(messageStringVal, User.class);
        MDC.put("traceId", user.getUid());
        return user;
    }

    @Override
    public void onReceive(HyggeRabbitMqListenerContext<User> context, User messageEntity) {
        if (messageEntity.getAge() < 18) {
            // ack 超时时会重新进入队里并消费(默认是半小时，https://www.rabbitmq.com/consumers.html#acknowledgement-timeout)
            // 超时日志样例：Shutdown Signal: channel error; protocol method: #method<channel.close>(reply-code=406, reply-text=PRECONDITION_FAILED - delivery acknowledgement on channel 1 timed out. Timeout value used: 1800000 ms. This timeout value can be configured, see consumers doc guide to learn more, class-id=0, method-id=0)
//            context.setAutoAckTriggered(true);
        }
    }

    @Override
    public void finallyHook(HyggeRabbitMqListenerContext<User> context) {
        try {
            MDC.remove("traceId");
        } catch (Exception e) {
            HyggeListenerBaseFeature.log.error("Unexpected exception.", e);
        }
    }
}
