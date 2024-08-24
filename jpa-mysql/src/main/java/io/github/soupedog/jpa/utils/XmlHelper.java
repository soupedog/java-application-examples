package io.github.soupedog.jpa.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import hygge.commons.exception.UtilRuntimeException;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * @author Xavier
 * @date 2024/8/25
 * @since 1.0
 */
public class XmlHelper {
    /**
     * 去除空白、换行方式输出
     */
    public static final ObjectMapper mapper = new XmlMapper();
    /**
     * 自动换行方便人工阅读方式输出
     */
    public static final ObjectMapper mapperForIndent = new XmlMapper();

    static {
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
        defaultConfig(mapper);

        mapperForIndent.configure(SerializationFeature.INDENT_OUTPUT, true);
        defaultConfig(mapperForIndent);
    }

    private XmlHelper() {
        throw new IllegalStateException("Utility class");
    }

    private static void defaultConfig(ObjectMapper target) {
        // 遇到多余属性反序列化时也不认为错误
        target.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 允许数字 0 开头
        target.configure(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS.mappedFeature(), true);
        target.registerModule(new JavaTimeModule());
        target.configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, false);
        target.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        // 序列化出空对象抛出异常
        target.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
        // 忽略序列化时为 null 的属性
        target.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static String formatAsString(Object target, Class<?> targetClass) {
        return formatAsString(false, target, targetClass);
    }

    public static String formatAsString(boolean enableIndent, Object target, Class<?> targetClass) {
        ObjectMapper currentObjectMapper = enableIndent ? mapperForIndent : mapper;
        try {
            String result;
            if (target instanceof String) {
                if (!StringUtils.hasText((String) target)) {
                    return (String) target;
                }
                String firstVal = target.toString().substring(0, 1);
                if (!firstVal.equals("<")) {
                    throw new UtilRuntimeException("XMLHelper fail to formatAsString:not a standard xml.");
                }
                result = currentObjectMapper.writeValueAsString(currentObjectMapper.readValue((String) target, targetClass));
            } else {
                result = currentObjectMapper.writeValueAsString(target);
            }
            return result;
        } catch (IOException e) {
            throw new UtilRuntimeException("XMLHelper fail to format.", e);
        }
    }

    public static <T> T readAsObject(String xmlTarget, Class<T> tClass) {
        if (xmlTarget == null) {
            return null;
        }
        try {
            if (String.class.equals(tClass)) {
                return (T) xmlTarget;
            }
            return mapper.readValue(xmlTarget, tClass);
        } catch (JsonProcessingException e) {
            throw new UtilRuntimeException("XMLHelper fail to readAsObject to " + tClass.getName() + ":" + xmlTarget, e);
        }
    }

    public static ObjectMapper getMAPPER() {
        return mapper;
    }

    public static ObjectMapper getMapperForIndent() {
        return mapperForIndent;
    }
}