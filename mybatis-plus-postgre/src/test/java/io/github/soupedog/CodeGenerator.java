package io.github.soupedog;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

/**
 * Mybatis-plus 配套的代码生成器，读取数据库生成 PO/DAO/Service/Controller 层代码
 * <p>
 * (Mybatis 也有代码生成器，但没有这个好用，用法也类似，此处不多做演示)
 *
 * @author Xavier
 * @date 2024/8/29
 * @since 1.0
 */
class CodeGenerator {
    @Test
    void initCode() {
        // 此处演示 的 postgreSQL 事实上也支持其他数据库，切换修改 url 切换驱动即可
        FastAutoGenerator.create("jdbc:postgresql://localhost:5432/postgres?currentSchema=local_test", "postgres", "0000")
                .globalConfig(builder -> builder
                        .author("Baomidou")
                        .outputDir(Paths.get(System.getProperty("user.dir")) + "/src/main/java")
                        .commentDate("yyyy-MM-dd")
                )
                .packageConfig(builder -> builder
                        .parent("com.baomidou.mybatisplus")
                        .entity("entity")
                        .mapper("mapper")
                        .service("service")
                        .serviceImpl("service.impl")
                        .xml("mapper.xml")
                )
                .strategyConfig(builder -> builder
                        .entityBuilder()
                        .enableLombok()
                )
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}
