package io.github.soupedog.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Swagger 链接 "http://localhost:8080/swagger-ui/index.html"
 * <p>
 * 2.7.18 对应的最后一个 Spring-Kafka 版本文档 "https://docs.spring.io/spring-kafka/docs/2.8.11/reference/html/#preface"
 *
 * @author Xavier
 * @date 2026/4/14
 */
@EnableKafka
@SpringBootApplication
public class KafkaMqExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(KafkaMqExampleApplication.class, args);
    }
}
