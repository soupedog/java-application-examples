package io.github.soupedog.listener;

import io.github.soupedog.domain.User;
import io.github.soupedog.listener.base.HyggeBatchMessageItem;
import io.github.soupedog.listener.base.HyggeChannelAwareBatchMessageListener;
import io.github.soupedog.listener.base.HyggeRabbitMqBatchListenerContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
            User user = jsonHelper.readAsObject(messageItem.getMessageStringVal(), User.class);
            messageItem.setMessageEntity(user);
        }
    }

    @Override
    public void onReceive(HyggeRabbitMqBatchListenerContext<User> context) {
        ArrayList<Long> getDeliveryTagList = collectionHelper.filterNonemptyItemAsArrayList(false, context.getRawMessageList(), item -> item.getMessage().getMessageProperties().getDeliveryTag());
        log.info("{}", getDeliveryTagList);
    }
}