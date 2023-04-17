package io.github.soupedog.listener.base;

import com.rabbitmq.client.Channel;
import hygge.commons.constant.ConstantParameters;
import io.github.soupedog.listener.base.definition.HyggeBatchListenerFeature;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareBatchMessageListener;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Xavier
 * @date 2023/4/17
 * @since 1.0
 */
public abstract class HyggeChannelAwareBatchMessageListener<T> implements HyggeBatchListenerFeature<T>, ChannelAwareBatchMessageListener {
    protected String listenerName;
    protected String environmentName;
    protected long requeueToTailMillisecondInterval = 500L;
    protected int maxRequeueTimes = 1000;
    protected static String HEADERS_KEY_ENVIRONMENT_NAME = "hygge-environment-name";
    protected static String HEADERS_KEY_REQUEUE_TO_TAIL_COUNTER = "hygge-requeue-counter";

    public HyggeChannelAwareBatchMessageListener(String listenerName, String environmentName) {
        this.listenerName = listenerName;
        this.environmentName = environmentName;
    }

    @Override
    public String getListenerName() {
        return listenerName;
    }

    @Override
    public void onMessageBatch(List<Message> messages, Channel channel) {
        HyggeRabbitMqBatchListenerContext<T> context = new HyggeRabbitMqBatchListenerContext<>();

        onMessageMainLogic(messages, channel, context);

        finallyHook(context);
    }

    private void onMessageMainLogic(List<Message> messages, Channel channel, HyggeRabbitMqBatchListenerContext<T> context) {
        ArrayList<HyggeBatchMessageItem<T>> rawMessageList = collectionHelper.filterNonemptyItemAsArrayList(false, messages,
                item -> {
                    // 更新当前最大 deliveryTag
                    context.setMaxDeliveryTagIntelligently(item.getMessageProperties().getDeliveryTag());
                    return new HyggeBatchMessageItem<>(item);
                });
        context.setRawMessageList(rawMessageList);
        context.setChannel(channel);

        // 是否忽略当前消息，并丢回队列尾部
        if (isRequeueEnable(context)) {
            List<HyggeBatchMessageItem<T>> nextRawMessageList = requeue(context);

            // requeue 行为之后如果已经不剩任何一条数据时，结束消费流程
            if (nextRawMessageList.isEmpty()) {
                return;
            } else {
                // 用 requeue 之后剩下的部分重置 rawMessageList
                context.setRawMessageList(nextRawMessageList);
            }
        }

        for (HyggeBatchMessageItem<T> item : context.getRawMessageList()) {
            String headersStringVal = formatMessageHeadersAsString(context, item);
            String messageStringVal = formatMessageBodyAsString(context, item);
            item.setHeadersStringVal(headersStringVal);
            item.setMessageStringVal(messageStringVal);
        }

        // 消息字符串形式转对象
        formatMessageAsEntity(context);

        // 消息业务处理
        onReceive(context);

        // 自动 ack 处理
        autoAck(context);

        // 筛选出需要重试的消息
        List<HyggeBatchMessageItem<T>> needsRetryList = collectionHelper.filterNonemptyItemAsArrayList(false, context.getRawMessageList(),
                messageItem -> {
                    if (ActionEnum.NEEDS_RETRY.equals(messageItem.getAction())) {
                        return messageItem;
                    } else {
                        return null;
                    }
                }
        );

        if (!needsRetryList.isEmpty()) {
            retryHook(context, needsRetryList);
        }

        // 筛选出需要业务扫尾处理的消息
        List<HyggeBatchMessageItem<T>> needsBusinessLogicFinishList = collectionHelper.filterNonemptyItemAsArrayList(false, context.getRawMessageList(),
                messageItem -> {
                    switch (messageItem.getAction()) {
                        case ACK_SUCCESS:
                        case NACK_SUCCESS:
                            return messageItem;
                        default:
                            return null;
                    }
                }
        );

        if (!needsBusinessLogicFinishList.isEmpty()) {
            businessLogicFinishHook(context, needsBusinessLogicFinishList);
        }

        // 日志对象覆写并输出
        for (HyggeBatchMessageItem<T> item : context.getRawMessageList()) {
            item.setHeadersStringVal(messageHeadersOverwrite(context, item));
            item.setMessageStringVal(messageBodyOverwrite(context, item));
        }

        String prefixInfo = String.format("HyggeBatchListener(%s) received message.", getListenerName());
        printMessageEntityLog(context, prefixInfo);
    }

    @Override
    public boolean isRequeueEnable(HyggeRabbitMqBatchListenerContext<T> context) {
        for (HyggeBatchMessageItem<T> item : context.getRawMessageList()) {
            String messageEnvironmentName = getValueFromHeaders(item, HEADERS_KEY_ENVIRONMENT_NAME, true);

            if (!environmentName.equals(messageEnvironmentName) && parameterHelper.isNotEmpty(messageEnvironmentName)) {
                item.setAction(ActionEnum.NEEDS_REQUEUE);
            }
        }

        // 至少存在一个 NEEDS_REQUEUE 类型时则需要进行重回队列
        return context.getRawMessageList().stream().anyMatch(messageItem -> ActionEnum.NEEDS_REQUEUE.equals(messageItem.getAction()));
    }

    @Override
    public List<HyggeBatchMessageItem<T>> requeue(HyggeRabbitMqBatchListenerContext<T> context) {
        List<HyggeBatchMessageItem<T>> nextRawMessageList = new ArrayList<>();
        List<HyggeBatchMessageItem<T>> needsRequeueMessageList = new ArrayList<>();

        for (HyggeBatchMessageItem<T> item : context.getRawMessageList()) {
            if (item.getAction().equals(ActionEnum.NEEDS_RETRY)) {
                needsRequeueMessageList.add(item);
            } else {
                nextRawMessageList.add(item);
            }
        }

        // TODO needsRequeueMessageList 单条回队列
        return nextRawMessageList;
    }

    @Override
    public String formatMessageHeadersAsString(HyggeRabbitMqBatchListenerContext<T> context, HyggeBatchMessageItem<T> messageItem) {
        return jsonHelper.formatAsString(messageItem.getMessage().getMessageProperties().getHeaders());
    }

    @Override
    public String formatMessageBodyAsString(HyggeRabbitMqBatchListenerContext<T> context, HyggeBatchMessageItem<T> messageItem) {
        return new String(messageItem.getMessage().getBody(), StandardCharsets.UTF_8);
    }

    @Override
    public void printMessageEntityLog(HyggeRabbitMqBatchListenerContext<T> context, String prefixInfo) {
        StringBuilder stringBuilder = new StringBuilder(prefixInfo);
        stringBuilder.append(ConstantParameters.LINE_SEPARATOR);

        List<HyggeBatchMessageItem<T>> rawMessageList = context.getRawMessageList();

        for (HyggeBatchMessageItem<T> item : rawMessageList) {
            stringBuilder.append("action:").append(item.getAction());
            stringBuilder.append(" ");
            stringBuilder.append("headers:").append(item.getHeadersStringVal());
            stringBuilder.append(" ");
            stringBuilder.append("body:").append(item.getMessageStringVal());
            stringBuilder.append(ConstantParameters.LINE_SEPARATOR);

            if (item.isExceptionOccurred()) {
                String exceptionId = UUID.randomUUID().toString();

                stringBuilder.append("exception:").append(exceptionId);
                stringBuilder.append(ConstantParameters.LINE_SEPARATOR);

                String innerExceptionPrefixInfo = String.format("HyggeBatchListener(%s) InnerException(%s).", getListenerName(), exceptionId);
                log.error(innerExceptionPrefixInfo, item.getThrowable());
            }
        }

        String logInfo = parameterHelper.removeStringFormTail(stringBuilder, ConstantParameters.LINE_SEPARATOR, 1).toString();

        printLog(context.getLoglevel(), logInfo);
    }

    @Override
    public void autoAck(HyggeRabbitMqBatchListenerContext<T> context) {
        // 如果全为同种 ack 类型，可以进行批量提交
        HyggeRabbitMqBatchListenerContext.MultipleAckInfo multipleAckInfo = context.analyzeMultipleAckInfo();
        if (multipleAckInfo.isMultipleAckEnable()) {

            try {
                context.getChannel().basicAck(context.getMaxDeliveryTag(),true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            for (HyggeBatchMessageItem<T> item : context.getRawMessageList()) {
                // 已进行过 ack 的消息跳过
                if (item.isAutoAckTriggered()) {
                    continue;
                }

                if (item.getAction().equals(ActionEnum.NEEDS_ACK)) {
                    ack(context, item);
                } else {
                    nack(context, item);
                }
            }
        }
    }
}
