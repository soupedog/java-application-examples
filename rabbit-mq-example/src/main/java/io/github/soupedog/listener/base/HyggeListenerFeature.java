package io.github.soupedog.listener.base;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
public interface HyggeListenerFeature<T> {

    /**
     * 是否要将当前消息发送回原队列尾部
     * <p>
     * 常用于标记接收的消息不属于当前实例，不应进行消费进而丢回队尾
     */
    default boolean isRequeueToTailEnable(HyggeRabbitMqListenerContext context) throws Exception {
        return false;
    }

    /**
     * 将队列消息 Headers 转化成字符串形式
     */
    String formatMessageHeadersAsString(HyggeRabbitMqListenerContext context) throws Exception;

    /**
     * 将队列消息转化成字符串形式
     */
    String formatMessageBodyAsString(HyggeRabbitMqListenerContext context) throws Exception;

    /**
     * 将队列消息字符串形式转化成对象
     */
    T formatMessageAsEntity(HyggeRabbitMqListenerContext context, String messageStringVal) throws Exception;

    /**
     * 打印接收到的消息信息
     * <p>
     * 警告：该方法本身不应该抛出异常，它可能被用于各种异常处理时的兜底环节
     */
    void printMessageLog(HyggeRabbitMqListenerContext context, String headersStringVal, String messageStringVal);

    /**
     * 收到消息后需要做的业务处理
     */
    void onReceive(HyggeRabbitMqListenerContext context, T messageEntity) throws Exception;

    /**
     * 尝试重试行为，返回是否确实执行了重试逻辑
     */
    default boolean retryHook(HyggeRabbitMqListenerContext context) throws Exception {
        return false;
    }

    /**
     * 通常是消息处理流程的最后一个环节，常用于业务处理结束时的一些扫尾工作
     * <p>
     * 在自动 ack 执行完成且未执行重试逻辑时触发
     */
    void finishHook(HyggeRabbitMqListenerContext context) throws Exception;
}
