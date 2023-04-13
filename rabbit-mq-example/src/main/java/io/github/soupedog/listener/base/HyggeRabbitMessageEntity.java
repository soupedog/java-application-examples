package io.github.soupedog.listener.base;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
public interface HyggeRabbitMessageEntity {

    /**
     * 获取唯一标识
     */
    @JsonIgnore
    <T> T getUniqueIdentification();

    /**
     * 获取消息对象
     */
    @JsonIgnore
    <T> T getMessageEntity();
}
