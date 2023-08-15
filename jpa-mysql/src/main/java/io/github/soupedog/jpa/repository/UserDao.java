package io.github.soupedog.jpa.repository;

import io.github.soupedog.jpa.domain.po.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

/**
 * @author Xavier
 * @date 2023/8/15
 * @since 1.0
 */
@Repository
public interface UserDao extends JpaRepository<User, Long> {

    /**
     * 接口按特定规则命名会自动生成数据库操作方法
     * <p>
     * <a href="https://docs.spring.io/spring-data/jpa/docs/2.7.14/reference/html/#jpa.query-methods.query-creation">名称语法糖</a>
     */
    User findUserByName(String userName);

    @Query(value = "SELECT * FROM local_test.user WHERE uid <=:maxId", nativeQuery = true)
    Page<User> queryUserListByMaxIdNative(@Param("maxId") Long maxId, Pageable pageable);

    /**
     * 需要 {@link User} 有对应的构造函数 "RAND()" 是演示赋值可用 mysql 各种函数
     */
    @Query(value = "SELECT new io.github.soupedog.jpa.domain.po.User(uid,name,RAND()) FROM User WHERE uid <=:maxId",
            countQuery = "SELECT COUNT(*) FROM User WHERE uid <=:maxId")
    Page<User> queryUserListByMaxIdHQL(@Param("maxId") Long maxId, Pageable pageable);

    /**
     * <code>
     * createTs + '0.003'
     * </code>
     * 操作会精度丢失，此方法内在运算是 double 类型，会出现想加 3 毫秒实际上却是 2 毫秒等情况，而应该使用 TIME 时间对象
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE local_test.user SET createTs = createTs + TIME '00:00:00.003'", nativeQuery = true)
    int plus3MillisecondForCreateTs();
}
