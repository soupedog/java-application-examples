package io.github.soupedog.mybatis.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.SpringBootVFS;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisXMLLanguageDriver;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariDataSource;
import io.github.soupedog.mybatis.dao.handler.AutoUpdateTimestampOfCreateAndUpdateMetaObjectHandler;
import io.github.soupedog.mybatis.dao.handler.UserConfigurationTypeHandler;
import io.github.soupedog.mybatis.domain.po.inner.UserConfiguration;
import org.apache.ibatis.io.VFS;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;

/**
 * @author Xavier
 * @date 2023/5/15
 * @since 1.0
 */
@Configuration
@MapperScan(basePackages = {"io.github.soupedog.mybatis.dao"}, sqlSessionFactoryRef = "webAppSqlSessionFactory")
public class DatabaseConfig {
    @Value("${db.userName:postgres}")
    private String userName;
    @Value("${db.password:0000}")
    private String password;

    /**
     * @see "https://jdbc.postgresql.org/documentation/use/"
     */
    @Bean("webAppDataSource")
    public HikariDataSource webAppDataSource() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setUsername(userName);
        hikariDataSource.setPassword(password);
        // postgres 是默认数据库，有需要可以改成其他自主创建的
        hikariDataSource.setJdbcUrl("jdbc:postgresql://localhost:5432/postgres");
        hikariDataSource.setDriverClassName(org.postgresql.Driver.class.getName());
        hikariDataSource.setMinimumIdle(1);
        hikariDataSource.setMaximumPoolSize(10);
        // 毫秒
        hikariDataSource.setIdleTimeout(600000);
        return hikariDataSource;
    }

    /**
     * mybatis-plus 文档对编码式配置的说明很少，总之先照源码抄吧。
     * <p>
     * 另外，如果我们不是自定义 MybatisSqlSessionFactoryBean 的话，其他很多组件都可以托管给 Spring</br>
     * 例如 {@link AutoUpdateTimestampOfCreateAndUpdateMetaObjectHandler} 定义为 Spring Bean 便可自动注册，而不需要在此处显示指定
     *
     * @see MybatisPlusAutoConfiguration#sqlSessionFactory(DataSource)
     */
    @Bean("webAppSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("webAppDataSource") HikariDataSource hikariDataSource) {
        // 这个才能使 Mybatis-plus 生效(否则无法映射数据库操作方法)
        // 如果使用 Mybatis 则是 "org.mybatis.spring.SqlSessionFactoryBean" 这个类
        MybatisSqlSessionFactoryBean mybatisSqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        mybatisSqlSessionFactoryBean.setDataSource(hikariDataSource);

        VFS.addImplClass(SpringBootVFS.class);
        String typeAliasesPackage = "io.github.soupedog.mybatis.domain.po;";
        // 扫描Mybatis所用到的返回entity类型
        mybatisSqlSessionFactoryBean.setTypeAliasesPackage(typeAliasesPackage);
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            // 扫描 Mybatis 所用到 mapper.xml
            mybatisSqlSessionFactoryBean.setMapperLocations(resolver.getResources("classpath*:/mapper/*.xml"));
            // 如果使用 Mybatis 则是 "org.apache.ibatis.session.Configuration"
            MybatisConfiguration configuration = new MybatisConfiguration();

            // 这也是一种开启 sql 日志的办法，但是输出的 sql 语句仍然带占位符，不能直接丢进数据库执行
            // 个人评价不如 "logging.level" 方式开启日志
            // configuration.setLogImpl(org.apache.ibatis.logging.stdout.StdOutImpl.class);
            configuration.setDefaultScriptingLanguage(MybatisXMLLanguageDriver.class);
            configuration.setJdbcTypeForNull(JdbcType.NULL);
            // 不开启下划线转驼峰(postgre 的 sql 语句大小写敏感，而且不加双引号的大写会默认为小写，开启转换更方便)
            configuration.setMapUnderscoreToCamelCase(true);
            mybatisSqlSessionFactoryBean.setConfiguration(configuration);

            // 如果有特殊字段类型需要在数据库与 PO 之间来回转换，可以注册多种转换(此处配置的是全局生效)
            // 局部生效可在 PO 对象属性上标记 "com.baomidou.mybatisplus.annotation.TableField" 来指定
            TypeHandlerRegistry registry = configuration.getTypeHandlerRegistry();
            registry.register(UserConfiguration.class, JdbcType.VARCHAR, new UserConfigurationTypeHandler());
            registry.register(JdbcType.VARCHAR, new UserConfigurationTypeHandler());

            // 自动设置时间戳
            GlobalConfig globalConfig = GlobalConfigUtils.defaults();
            globalConfig.setMetaObjectHandler(new AutoUpdateTimestampOfCreateAndUpdateMetaObjectHandler());
            mybatisSqlSessionFactoryBean.setGlobalConfig(globalConfig);

            return mybatisSqlSessionFactoryBean.getObject();
        } catch (Exception e) {
            e.printStackTrace();
            // 主动中断服务
            System.exit(0);
            return null;
        }
    }
}
