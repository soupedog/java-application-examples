package io.github.soupedog.listener.base;

import com.rabbitmq.client.Channel;
import hygge.commons.constant.ConstantParameters;
import hygge.commons.exception.InternalRuntimeException;
import io.github.soupedog.listener.base.definition.HyggeBatchListenerFeature;
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
public abstract class HyggeChannelAwareBatchMessageListener<T> implements HyggeBatchListenerFeature<T>, ChannelAwareBatchMessageListener {
    protected String listenerName;
    protected String environmentName;
    protected long requeueToTailMillisecondInterval = 500L;
    protected int maxRequeueTimes = 500;
    protected static String HEADERS_KEY_ENVIRONMENT_NAME = DEFAULT_HEADERS_KEY_ENVIRONMENT_NAME;
    protected static String HEADERS_KEY_REQUEUE_TO_TAIL_COUNTER = DEFAULT_HEADERS_KEY_REQUEUE_TO_TAIL_COUNTER;

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
        ArrayList<HyggeBatchMessageItem<T>> rawMessageList = collectionHelper.filterNonemptyItemAsArrayList(false, messages, HyggeBatchMessageItem::new);
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
        printMessageEntityLog(context.getLoglevel(), context.getRawMessageList(), prefixInfo);
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
            if (item.getAction().equals(ActionEnum.NEEDS_REQUEUE)) {
                needsRequeueMessageList.add(item);
            } else {
                nextRawMessageList.add(item);
            }
        }

        // 当前消息重新发送到队尾
        Channel channel = context.getChannel();

        CompletableFuture<?>[] futureArray = new CompletableFuture[needsRequeueMessageList.size()];

        for (int i = 0; i < needsRequeueMessageList.size(); i++) {
            HyggeBatchMessageItem<T> item = needsRequeueMessageList.get(i);

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    ack(context, item);

                    // 防止重新投递过于迅速
                    Thread.sleep(requeueToTailMillisecondInterval);

                    Message message = item.getMessage();

                    int requeueCounter = parameterHelper.integerFormatOfNullable(HEADERS_KEY_REQUEUE_TO_TAIL_COUNTER, getValueFromHeaders(item, HEADERS_KEY_REQUEUE_TO_TAIL_COUNTER, true), 0);

                    requeueToTail(channel, message, HEADERS_KEY_REQUEUE_TO_TAIL_COUNTER, requeueCounter, maxRequeueTimes);
                    item.setAction(ActionEnum.REQUEUE_SUCCESS);
                } catch (Exception e) {
                    item.setAction(ActionEnum.REQUEUE_FAILURE);
                    item.setThrowable(e);
                    if (e instanceof InternalRuntimeException) {
                        // 其实是超出 requeue 次数上限异常，不需要异常堆栈信息
                        item.setThrowable(null);
                        item.setAction(ActionEnum.REQUEUE_FAILURE);
                        context.setLoglevelIntelligently(LogLevel.WARN);
                    } else {
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
            for (HyggeBatchMessageItem<T> item : needsRequeueMessageList) {
                item.setThrowable(e);
                prefixInfo = String.format("HyggeBatchListener(%s) fail to requeue.(The \"action\" value is no longer accurate)", getListenerName());
                context.setLoglevelIntelligently(LogLevel.ERROR);
            }
        } finally {
            switch (context.getLoglevel()) {
                case WARN:
                case ERROR:
                case FATAL:
                    if (prefixInfo == null) {
                        prefixInfo = String.format("HyggeBatchListener(%s) some exception occurred during requeue.", getListenerName());
                    }

                    for (HyggeBatchMessageItem<T> item : context.getRawMessageList()) {
                        String headersStringVal = formatMessageHeadersAsString(context, item);
                        String messageStringVal = formatMessageBodyAsString(context, item);
                        item.setHeadersStringVal(headersStringVal);
                        item.setMessageStringVal(messageStringVal);

                        // 日志对象覆写并输出
                        item.setHeadersStringVal(messageHeadersOverwrite(context, item));
                        item.setMessageStringVal(messageBodyOverwrite(context, item));
                    }

                    printMessageEntityLog(context.getLoglevel(), needsRequeueMessageList, prefixInfo);
                    break;
                default:
                    // do nothing by default
            }
        }

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
    public void printMessageEntityLog(LogLevel logLevel, List<HyggeBatchMessageItem<T>> messageList, String prefixInfo) {
        StringBuilder stringBuilder = new StringBuilder(prefixInfo);
        stringBuilder.append(ConstantParameters.LINE_SEPARATOR);

        HashMap<Throwable, String> exceptionMap = null;

        for (HyggeBatchMessageItem<T> item : messageList) {
            stringBuilder.append("action:").append(item.getAction());
            stringBuilder.append(" ");
            stringBuilder.append("headers:").append(item.getHeadersStringVal());
            stringBuilder.append(" ");
            stringBuilder.append("body:").append(item.getMessageStringVal());

            if (item.isExceptionOccurred()) {
                stringBuilder.append(" ");
                if (exceptionMap == null) {
                    exceptionMap = new HashMap<>(messageList.size() * 2);
                }

                String exceptionId = exceptionMap.get(item.getThrowable());
                if (exceptionId == null) {
                    exceptionId = UUID.randomUUID().toString();
                    exceptionMap.put(item.getThrowable(), exceptionId);
                    String innerExceptionPrefixInfo = String.format("HyggeBatchListener(%s) InnerException(%s).", getListenerName(), exceptionId);
                    log.error(innerExceptionPrefixInfo, item.getThrowable());
                }

                stringBuilder.append("exception:").append(exceptionId);
            }

            stringBuilder.append(ConstantParameters.LINE_SEPARATOR);
        }

        String logInfo = parameterHelper.removeStringFormTail(stringBuilder, ConstantParameters.LINE_SEPARATOR, 1).toString();

        printLog(logLevel, logInfo);
    }

    @Override
    public void autoAck(HyggeRabbitMqBatchListenerContext<T> context) {
        // 如果全为同种 ack 类型，可以进行批量提交
        HyggeRabbitMqBatchListenerContext.MultipleAckInfo multipleAckInfo = context.analyzeMultipleAckInfo();
        if (multipleAckInfo.isMultipleAckEnable()) {
            if (multipleAckInfo.getAction().equals(ActionEnum.NEEDS_ACK)) {
                ackMultiple(context);
            } else {
                nackMultiple(context);
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
