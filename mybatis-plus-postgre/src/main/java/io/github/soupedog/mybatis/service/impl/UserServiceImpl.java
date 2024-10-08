package io.github.soupedog.mybatis.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hygge.util.UtilCreator;
import hygge.util.bo.ColumnInfo;
import hygge.util.definition.CollectionHelper;
import hygge.util.definition.DaoHelper;
import io.github.soupedog.mybatis.dao.UserMapper;
import io.github.soupedog.mybatis.domain.po.User;
import io.github.soupedog.mybatis.domain.po.UserSimple;
import io.github.soupedog.mybatis.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 我个人不太喜欢 {@link ServiceImpl} 的设计，它会另 service 层对外暴露过多不必要的方法，我个人认为它还是应该属于 dao 层更合理
 * <p>
 * 例如下列功能是等效的：
 * <pre>
 *     userService.query().eq("uid", uid).one();
 *
 *     baseMapper.selectById(uid);
 * </pre>
 *
 * @author Xavier
 * @date 2023/5/15
 * @since 1.0
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private static final DaoHelper daoHelper = UtilCreator.INSTANCE.getDefaultInstance(DaoHelper.class);

    private static final Collection<ColumnInfo> forUpdate = new ArrayList<>();

    static {
        forUpdate.add(new ColumnInfo(true, false, "name", "\"NAME\"").toStringColumn(1, 50));
        forUpdate.add(new ColumnInfo(true, false, "balance", null, 2, RoundingMode.HALF_UP, BigDecimal.ZERO, new BigDecimal(Integer.MAX_VALUE)));
        forUpdate.add(new ColumnInfo(true, true, "configuration", null));
    }

    @Transactional
    public User saveOrUpdateUser(User user) {
        // 原理是先查询，不存在则进行覆盖式更新
        saveOrUpdate(user);
        return user;
    }

    public List<Object> queryUserSimpleByUid(Long uid) {
        UserSimple userSimple = baseMapper.customSelectOneByUid(uid);

        LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>lambdaQuery()
                // 仅查询 uid 和 balance 两个属性
                .select(User::getUid, User::getBalance)
                .eq(User::getUid, uid);
        return UtilCreator.INSTANCE.getDefaultInstance(CollectionHelper.class)
                .createCollection(userSimple, baseMapper.selectOne(queryWrapper));
    }

    /**
     * 与
     * <pre>
     *     baseMapper.selectById(uid)
     * </pre>
     * 等效
     */
    public User queryUserByUid(Long uid) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.<User>lambdaQuery()
                .eq(User::getUid, uid);
        return baseMapper.selectOne(queryWrapper);
    }

    public User customSaveUser(User user) {
        user.setCreateTs(new Timestamp(System.currentTimeMillis()));
        user.setLastUpdateTs(user.getCreateTs());
        baseMapper.customSaveUser(user);
        return user;
    }

    public boolean customUpdateUser(Long uid, Map<String, Object> updateInfo, Timestamp currentTs) {
        // 筛选结果不为空时才会激发成功回调
        HashMap<String, Object> updateMap = daoHelper.filterOutTheFinalColumns(updateInfo, forUpdate, map -> {
            map.put("last_update_ts", currentTs);
            return map;
        });

        int affectedLine = baseMapper.customUpdateUser(uid, updateMap, currentTs);

        if (affectedLine != 1) {
            log.warn("Fail to update User({}), affectedLine expected 1, but we found {}.", uid, affectedLine);
            return false;
        }
        return true;
    }

    public Map<String, User> customQueryUserMultiple(Collection<Long> uidCollection) {
        return baseMapper.customQueryUserMultiple(uidCollection, null);
    }
}
