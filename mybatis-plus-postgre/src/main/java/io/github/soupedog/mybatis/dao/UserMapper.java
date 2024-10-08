package io.github.soupedog.mybatis.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.soupedog.mybatis.domain.po.User;
import io.github.soupedog.mybatis.domain.po.UserSimple;
import io.github.soupedog.mybatis.service.impl.UserServiceImpl;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * BaseMapper 绑定的 {@link User} 对象，Mybatis-Plus 自动扩展出的方法要求返回 {@link User}，而自定义的方法的返回值并非必须是 {@link User}
 *
 * @author Xavier
 * @date 2023/5/15
 * @since 1.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 用 User 表查询结果返回一个非 PO 对象
     * <p>
     * {@link UserServiceImpl#queryUserSimpleByUid(Long)} Mybatis-Plus 扩展方法因为 Mapper 类型限制无法直接返回 {@link UserSimple} 实例
     *
     * @param uid 唯一标识
     */
    UserSimple customSelectOneByUid(Long uid);

    /**
     * 不会覆盖数据库表设置的 defaultValue 的方式插入一个 User 对象（IService 已经提供此功能，该方法仅为演示 mapper.xml 用法）
     *
     * @param user 等待插入的 PO 对象
     * @return 受影响行
     */
    int customSaveUser(@Param("user") User user);

    /**
     * 单纯演示功能，要求有 UK 才能使用 conflict 关键字。<br/>
     * 演示的逻辑是：名字冲突时，设置状态为 INACTIVE 并清空余额
     */
    int customSaveOrUpdateUserMultiple(@Param("userList") List<User> userList);

    /**
     * 更新 User(允许置 null 也允许局部更新)
     *
     * @param uid             唯一标识
     * @param dataMap         修改键值
     * @param updateLimitTime 最后修改时间截止时间，只能修改不晚于该时间的记录
     * @return 数据库受影响行
     */
    int customUpdateUser(@Param("uid") Long uid, @Param("dataMap") Map<String, Object> dataMap, @Param("updateLimitTime") Timestamp updateLimitTime);

    /**
     * 根据主键别名查询 User 对象并用其 User.name 属性做 key 返回 Map 对象(因为 HashMap 无序，故此处 orderInfo 无实际意义，仅展示动态 SQL 用法)
     *
     * @param uidCollection 用户唯一标识
     * @param orderInfo     查询结果集排序信息例如 "createTs ASC,lastUpdateTs DESC" (可空)
     * @return HashMap Key-Value name-User
     */
    @MapKey("name")
    Map<String, User> customQueryUserMultiple(@Param("uidCollection") Collection<Long> uidCollection, @Param("orderInfo") String orderInfo);
}