package io.github.soupedog.listener;

import hygge.commons.exception.LightRuntimeException;
import io.github.soupedog.domain.User;
import io.github.soupedog.listener.base.ActionEnum;
import io.github.soupedog.listener.base.HyggeBatchMessageItem;
import io.github.soupedog.listener.base.HyggeChannelAwareBatchMessageListener;
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
public class HyggeBatchListener extends HyggeChannelAwareBatchMessageListener<User> {
    public HyggeBatchListener(@Value("${test.demo.rabbit.environment-name}") String environmentName) {
        super("HyggeBatch", environmentName);
    }

    @Override
    public void formatMessageAsEntity(HyggeRabbitMqBatchListenerContext<User> context) {
        List<HyggeBatchMessageItem<User>> rawMessageList = context.getRawMessageList();

        for (HyggeBatchMessageItem<User> messageItem : rawMessageList) {
            try {
                User user = jsonHelper.readAsObject(messageItem.getMessageStringVal(), User.class);
                messageItem.setMessageEntity(user);
            } catch (Exception e) {
                messageItem.setThrowable(e);
                messageItem.setAction(ActionEnum.NEEDS_NACK);
            }
        }
    }

    @Override
    public void onReceive(HyggeRabbitMqBatchListenerContext<User> context) {
        for (HyggeBatchMessageItem<User> item : context.getRawMessageList()) {
//            if (randomHelper.getRandomInteger(0, 9) > 8) {
//                item.setThrowable(new LightRuntimeException("模拟异常"));
//                item.setAction(ActionEnum.NEEDS_NACK);
//            }
        }
    }
}
