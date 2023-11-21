package io.github.soupedog.rabbitmq.service.listener.base;

import com.rabbitmq.client.Channel;
import hygge.commons.constant.ConstantParameters;
import hygge.commons.exception.InternalRuntimeException;
import io.github.soupedog.rabbitmq.service.listener.base.definition.HyggeListenerBatchFeature;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareBatchMessageListener;
import org.springframework.boot.logging.LogLevel;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @author Xavier
 * @date 2023/4/17
 * @since 1.0
 */
public abstract class HyggeChannelAwareMessageListenerBatch<T> implements HyggeListenerBatchFeature<T>, ChannelAwareBatchMessageListener {
    protected String listenerName;
    protected String environmentName;
    protected long requeueToTailMillisecondInterval = 500L;
    protected int maxRequeueTimes = 500;
    protected static String headersKeyEnvironmentName = DEFAULT_HEADERS_KEY_ENVIRONMENT_NAME;
    protected static String headersKeyRequeueToTailCounter = DEFAULT_HEADERS_KEY_REQUEUE_TO_TAIL_COUNTER;

    protected HyggeChannelAwareMessageListenerBatch(String listenerName, String environmentName) {
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
        ArrayList<HyggeRabbitMQMessageItem<T>> rawMessageList = collectionHelper.filterNonemptyItemAsArrayList(false, messages, HyggeRabbitMQMessageItem::new);
        context.setRawMessageList(rawMessageList);
        context.setChannel(channel);

        // 是否忽略当前消息，并需要尝试恢复到消费前等效的状态
        if (isRequeueEnable(context)) {
            List<HyggeRabbitMQMessageItem<T>> nextRawMessageList = requeue(context);

            // requeue 行为之后如果已经不剩任何一条数据时，结束消费流程
            if (nextRawMessageList.isEmpty()) {
                return;
            } else {
                // 用 requeue 之后剩下的部分重置 rawMessageList
                context.setRawMessageList(nextRawMessageList);
            }
        }

        for (HyggeRabbitMQMessageItem<T> item : context.getRawMessageList()) {
            // 更新当前最大 deliveryTag
            context.setMaxDeliveryTagIntelligently(item.getMessage().getMessageProperties().getDeliveryTag());

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

        // 筛选出需要业务扫尾处理的消息
        List<HyggeRabbitMQMessageItem<T>> needsBusinessLogicFinishList = collectionHelper.filterNonemptyItemAsArrayList(false, context.getRawMessageList(),
                messageItem -> {
                    switch (messageItem.getStatus()) {
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

        // 筛选出需要重试的消息
        List<HyggeRabbitMQMessageItem<T>> needsRetryList = collectionHelper.filterNonemptyItemAsArrayList(false, context.getRawMessageList(),
                messageItem -> {
                    if (StatusEnums.NEEDS_RETRY.equals(messageItem.getStatus())) {
                        return messageItem;
                    } else {
                        return null;
                    }
                }
        );

        if (!needsRetryList.isEmpty()) {
            retryHook(context, needsRetryList);
        }

        // 日志对象覆写并输出
        for (HyggeRabbitMQMessageItem<T> item : context.getRawMessageList()) {
            messageHeadersOverwrite(context, item);
            messageBodyOverwrite(context, item);
        }

        String prefixInfo = String.format("HyggeBatchListener(%s): Received message.", getListenerName());
        printMessageEntityLog(context, context.getRawMessageList(), prefixInfo);
    }

    @Override
    public boolean isRequeueEnable(HyggeRabbitMqBatchListenerContext<T> context) {
        for (HyggeRabbitMQMessageItem<T> item : context.getRawMessageList()) {
            String messageEnvironmentName = getValueFromHeaders(item, headersKeyEnvironmentName, true);

            if (!environmentName.equals(messageEnvironmentName) && parameterHelper.isNotEmpty(messageEnvironmentName)) {
                item.setStatus(StatusEnums.NEEDS_REQUEUE);
            }
        }

        // 至少存在一个 NEEDS_REQUEUE 类型时则需要进行重回队列
        return context.getRawMessageList().stream().anyMatch(messageItem -> StatusEnums.NEEDS_REQUEUE.equals(messageItem.getStatus()));
    }

    @Override
    public List<HyggeRabbitMQMessageItem<T>> requeue(HyggeRabbitMqBatchListenerContext<T> context) {
        List<HyggeRabbitMQMessageItem<T>> nextRawMessageList = new ArrayList<>();
        List<HyggeRabbitMQMessageItem<T>> needsRequeueMessageList = new ArrayList<>();

        for (HyggeRabbitMQMessageItem<T> item : context.getRawMessageList()) {
            if (item.getStatus().equals(StatusEnums.NEEDS_REQUEUE)) {
                needsRequeueMessageList.add(item);
            } else {
                nextRawMessageList.add(item);
            }
        }

        // 当前消息重新发送到队尾
        Channel channel = context.getChannel();

        CompletableFuture<?>[] futureArray = new CompletableFuture[needsRequeueMessageList.size()];

        for (int i = 0; i < needsRequeueMessageList.size(); i++) {
            HyggeRabbitMQMessageItem<T> item = needsRequeueMessageList.get(i);

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    ack(context, item);

                    if (!item.statusExpected(StatusEnums.ACK_SUCCESS)) {
                        throw item.getException();
                    } else {
                        item.setStatus(StatusEnums.REQUEUE_HALF_SUCCESS);
                    }

                    // 防止重新投递过于迅速
                    Thread.sleep(requeueToTailMillisecondInterval);

                    Message message = item.getMessage();

                    int requeueCounter = parameterHelper.integerFormatOfNullable(headersKeyRequeueToTailCounter, getValueFromHeaders(item, headersKeyRequeueToTailCounter, true), 0);

                    requeueToTail(channel, message, headersKeyRequeueToTailCounter, requeueCounter, maxRequeueTimes);
                    item.setStatus(StatusEnums.REQUEUE_SUCCESS);
                } catch (Exception e) {
                    item.setStatus(StatusEnums.REQUEUE_FAILURE);

                    if (e instanceof InternalRuntimeException) {
                        // 其实是超出 requeue 次数上限异常，不需要异常堆栈信息
                        context.setLoglevelIntelligently(LogLevel.WARN);
                    } else {
                        item.setException(e);
                        context.setLoglevelIntelligently(LogLevel.ERROR);
                    }
                }
            });

            futureArray[i] = future;
        }

        String prefixInfo = null;
        try {
            CompletableFuture.allOf(futureArray).get();
        } catch (Exception e) {
            for (HyggeRabbitMQMessageItem<T> item : needsRequeueMessageList) {
                item.setException(e);
                prefixInfo = String.format("HyggeBatchListener(%s): Fail to requeue.(The \"status\" value is no longer accurate)", getListenerName());
                context.setLoglevelIntelligently(LogLevel.ERROR);
            }
        } finally {
            switch (context.getLoglevel()) {
                case WARN:
                case ERROR:
                case FATAL:
                    if (prefixInfo == null) {
                        prefixInfo = String.format("HyggeBatchListener(%s): Some exceptions occurred during requeue.", getListenerName());
                    }

                    List<HyggeRabbitMQMessageItem<T>> unexpectedItemList = collectionHelper.filterNonemptyItemAsArrayList(false, needsRequeueMessageList,
                            (item -> StatusEnums.REQUEUE_SUCCESS.equals(item.getStatus()) ? null : item)
                    );

                    for (HyggeRabbitMQMessageItem<T> item : unexpectedItemList) {
                        String headersStringVal = formatMessageHeadersAsString(context, item);
                        String messageStringVal = formatMessageBodyAsString(context, item);
                        item.setHeadersStringVal(headersStringVal);
                        item.setMessageStringVal(messageStringVal);

                        // 日志对象覆写并输出
                        messageHeadersOverwrite(context, item);
                        messageBodyOverwrite(context, item);
                    }

                    printMessageEntityLog(context, unexpectedItemList, prefixInfo);
                    break;
                default:
                    // do nothing by default
            }
        }

        return nextRawMessageList;
    }

    @Override
    public String formatMessageHeadersAsString(HyggeRabbitMqBatchListenerContext<T> context, HyggeRabbitMQMessageItem<T> messageItem) {
        return jsonHelper.formatAsString(messageItem.getMessage().getMessageProperties().getHeaders());
    }

    @Override
    public String formatMessageBodyAsString(HyggeRabbitMqBatchListenerContext<T> context, HyggeRabbitMQMessageItem<T> messageItem) {
        return new String(messageItem.getMessage().getBody(), StandardCharsets.UTF_8);
    }

    @Override
    public void printMessageEntityLog(HyggeRabbitMqBatchListenerContext<T> context, List<HyggeRabbitMQMessageItem<T>> messageList, String prefixInfo) {
        StringBuilder stringBuilder = new StringBuilder(prefixInfo);
        stringBuilder.append(ConstantParameters.LINE_SEPARATOR);

        // 异常去重，保障一个异常日志仅输出一次，消息用 exceptionId 与异常建立关联关系
        HashMap<Throwable, String> exceptionMap = null;

        for (HyggeRabbitMQMessageItem<T> item : messageList) {
            stringBuilder.append("status:").append(item.getStatus());
            stringBuilder.append(" ");
            stringBuilder.append("headers:").append(item.getHeadersStringVal());
            stringBuilder.append(" ");
            stringBuilder.append("body:").append(item.getMessageStringVal());

            if (item.isExceptionOccurred()) {
                stringBuilder.append(" ");
                if (exceptionMap == null) {
                    exceptionMap = new HashMap<>(messageList.size() * 2);
                }

                String exceptionId = exceptionMap.get(item.getException());
                if (exceptionId == null) {
                    exceptionId = UUID.randomUUID().toString();
                    exceptionMap.put(item.getException(), exceptionId);
                    String innerExceptionPrefixInfo = String.format("HyggeBatchListener(%s): InnerException(%s).", getListenerName(), exceptionId);
                    log.error(innerExceptionPrefixInfo, item.getException());
                }

                stringBuilder.append("exception:").append(exceptionId);
            }

            stringBuilder.append(ConstantParameters.LINE_SEPARATOR);
        }

        String logInfo = parameterHelper.removeStringFormTail(stringBuilder, ConstantParameters.LINE_SEPARATOR, 1).toString();

        // 批量模式是输出 InnerException 不在当前记录上输出异常
        printLog(context.getLoglevel(), logInfo, null);
    }

    @Override
    public void autoAck(HyggeRabbitMqBatchListenerContext<T> context) {
        // 进行 nackStatusCheckAndReset 后，如果全为同种 ack 类型，可以进行批量提交
        HyggeRabbitMqBatchListenerContext.MultipleAckInfo multipleAckInfo = context.analyzeMultipleAckInfo();

        if (multipleAckInfo.isMultipleAckEnable()) {
            if (multipleAckInfo.getAction().equals(StatusEnums.NEEDS_ACK)) {
                ackMultiple(context);
            } else {
                nackMultiple(context);
            }
        } else {
            for (HyggeRabbitMQMessageItem<T> item : context.getRawMessageList()) {
                // 手动进行过 ack 的消息跳过
                if (item.isAutoAckTriggered()) {
                    continue;
                }

                switch (item.getStatus()) {
                    case NEEDS_ACK:
                        ack(context, item);
                        break;
                    case NEEDS_NACK:
                        nack(context, item);
                        break;
                    default:
                        item.setException(new InternalRuntimeException("Status should be one of NEEDS_ACK/NEEDS_NACK but we found " + item.getStatus().toString() + "."));
                }
            }
        }
    }
}
