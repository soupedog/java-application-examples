# 环境准备

## docker 安装 postgreSQL 示例

***warn:** 请根据自己机器环境指定 -v 宿主机数据卷挂载*

```
docker run -d --name PostgresSQL_15 -p 5432:5432 -e PGDATA=/var/lib/postgresql/data/pgdata -e POSTGRES_PASSWORD=0000 -v /d/LocalMiddleware/Database/PostgreSQL:/var/lib/postgresql/data postgres:15.2
```

## 数据库初始化

见项目内 ``mybatis-plus-postgre/src/main/resources/init.sql``

# 示例摘要

- Mybatis-Plus 编程式配置
- 自定义 SQL
    - 自增主键
    - 需手动获取的序列主键
    - 批量插入或更新
- 对象自动转 JSON 存储到数据库字符串字段
- Mybatis-Plus 代理的方法自动更新时间戳
- SQL 信息输出到日志
