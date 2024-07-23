package io.github.soupedog.jpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <a href="https://spring.io/projects/spring-data-jpa#learn">Spring-Data-Jpa 文档</a>
 * <br/>
 * <a href="https://hibernate.org/orm">Hibernate ORM 文档</a>
 * <br/>
 * <a href="https://dev.mysql.com/doc">MySQL 文档</a>
 * <br/>
 * <a href="http://localhost:8080/swagger-ui/index.html">本地 Swagger 控制台</a>
 *
 * @author Xavier
 * @date 2023/8/15
 * @since 1.0
 */
@SpringBootApplication
public class JpaMySQLExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(JpaMySQLExampleApplication.class);
    }
}
