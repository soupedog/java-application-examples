package io.github.soupedog.listener.base.definition;

import com.rabbitmq.client.Channel;
import org.springframework.boot.logging.LogLevel;

/**
 * @author Xavier
 * @date 2023/4/18
 * @since 1.0
 */
public interface HyggeRabbitMqListenerContextFeature {
    /**
     * 获取上下文创建时间
     */
    long getStartTs();

    /**
     * 设置上下文创建时间
     */
    void setStartTs(long startTs);

    /**
     * 获取上下文日志级别
     */
    LogLevel getLoglevel();

    /**
     * 智能设置日志级别，允许日志级别在当前危急程度上提升，但不允许危急程度下降
     */
    void setLoglevelIntelligently(LogLevel loglevel);

    /**
     * 设置上下文日志级别
     */
    void setLoglevel(LogLevel loglevel);

    Channel getChannel();

    void setChannel(Channel channel);

    /**
     * 上下文是否发生异常
     */
    boolean isExceptionOccurred();

    /**
     * 上下文是否未发生异常
     */
    boolean isNoExceptionOccurred();
}
