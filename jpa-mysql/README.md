# 环境准备

## docker 安装 MySQL 示例

***warn:** 请根据自己机器环境指定 -v 宿主机数据卷挂载*

```
docker run -d --name MySQL_8 -p 3306:3306 -v /d/LocalMiddleware/Database/MySQL:/var/lib/mysql -e MYSQL_ROOT_PASSWORD=0000 -e TZ=Asia/Shanghai mysql:8.0.33
```

# 示例摘要

- JPA 编程式配置
- 自定义 SQL
    - Named Query(根据方法名称自动映射查询逻辑)
    - 原生 SQL 查询
    - HQL 查询
- 对象自动转 JSON 存储到数据库字符串字段
- JPA 代理的方法自动更新时间戳
- 根据 PO 对象自动建表
  - 默认建表字段顺序是字典排序，通过用 `LinkedHashMap` 改写 `org.hibernate.cfg.PropertyContainer` 变成以 PO 对象属性定义顺序建表 
- SQL 信息输出到日志
