package io.github.soupedog.listener;

import hygge.commons.exception.LightRuntimeException;
import io.github.soupedog.domain.User;
import io.github.soupedog.listener.base.StatusEnums;
import io.github.soupedog.listener.base.HyggeRabbitMQMessageItem;
import io.github.soupedog.listener.base.HyggeChannelAwareMessageListenerBatch;
import io.github.soupedog.listener.base.HyggeRabbitMqBatchListenerContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Xavier
 * @date 2023/4/17
 * @since 1.0
 */
@Component
public class HyggeListenerBatch extends HyggeChannelAwareMessageListenerBatch<User> {
    public HyggeListenerBatch(@Value("${test.demo.rabbit.environment-name}") String environmentName) {
        super("HyggeBatch", environmentName);
    }

    @Override
    public void formatMessageAsEntity(HyggeRabbitMqBatchListenerContext<User> context) {
        List<HyggeRabbitMQMessageItem<User>> rawMessageList = context.getRawMessageList();

        for (HyggeRabbitMQMessageItem<User> messageItem : rawMessageList) {
            try {
                User user = jsonHelper.readAsObject(messageItem.getMessageStringVal(), User.class);
                messageItem.setMessageEntity(user);
            } catch (Exception e) {
                messageItem.setException(e);
            }
        }
    }

    @Override
    public void onReceive(HyggeRabbitMqBatchListenerContext<User> context) {
        for (HyggeRabbitMQMessageItem<User> item : context.getRawMessageList()) {
            if (randomHelper.getRandomInteger(0, 9) > 8) {
                item.setException(new LightRuntimeException("模拟异常"));
            }
        }
    }
}
