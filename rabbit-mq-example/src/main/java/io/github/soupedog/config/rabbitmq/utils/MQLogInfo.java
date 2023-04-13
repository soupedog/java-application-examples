package io.github.soupedog.config.rabbitmq.utils;

/**
 * @author Xavier
 * @date 2023/4/14
 * @since 1.0
 */
public class MQLogInfo {
    private String modelName;
    private MessageInfo message;
    private Integer replyCode;
    private String replyText;
    private String exchange;
    private String routingKey;

    private MQLogInfo() {
    }

    public MQLogInfo(String modelName) {
        this.modelName = modelName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public MessageInfo getMessage() {
        return message;
    }

    public void setMessage(MessageInfo message) {
        this.message = message;
    }

    public Integer getReplyCode() {
        return replyCode;
    }

    public void setReplyCode(Integer replyCode) {
        this.replyCode = replyCode;
    }

    public String getReplyText() {
        return replyText;
    }

    public void setReplyText(String replyText) {
        this.replyText = replyText;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public static class MessageInfo {
        private Object headers;
        private Object body;

        public Object getHeaders() {
            return headers;
        }

        public void setHeaders(Object headers) {
            this.headers = headers;
        }

        public Object getBody() {
            return body;
        }

        public void setBody(Object body) {
            this.body = body;
        }
    }
}

