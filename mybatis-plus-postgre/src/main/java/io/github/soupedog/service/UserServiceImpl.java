package io.github.soupedog.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import hygge.commons.exception.ParameterRuntimeException;
import hygge.util.UtilCreator;
import hygge.util.bo.ColumnInfo;
import hygge.util.definition.DaoHelper;
import io.github.soupedog.dao.UserMapper;
import io.github.soupedog.domain.po.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 我个人不太喜欢 {@link ServiceImpl} 的设计，它会另 service 层对外暴露过多不必要的方法，我个人认为它还是应该属于 dao 层更合理
 * <p>
 * 例如下列功能是等效的：
 * <pre>
 *     userService.query().eq("uid", uid).one();
 *
 *     userMapper.selectById(uid);
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

    @Autowired
    private UserMapper userMapper;

    private static final Collection<ColumnInfo> forUpdate = new ArrayList<>();

    static {
        forUpdate.add(new ColumnInfo(true, false, "name", "\"NAME\"").toStringColumn(1, 50));
        forUpdate.add(new ColumnInfo(true, false, "balance", null, 2, RoundingMode.HALF_UP, BigDecimal.ZERO, new BigDecimal(Integer.MAX_VALUE)));
        forUpdate.add(new ColumnInfo(true, true, "configuration", null));
    }

    public User saveUser(User user) {
        save(user);
        return user;
    }

    public User saveUser2(User user) {
        user.setCreateTs(new Timestamp(System.currentTimeMillis()));
        user.setLastUpdateTs(user.getCreateTs());
        userMapper.saveUser2(user);
        return user;
    }

    public boolean updateUser(Long uid, Map<String, Object> updateInfo, Timestamp currentTs) {
        HashMap<String, Object> updateMap = daoHelper.filterOutTheFinalColumns(updateInfo, forUpdate, map -> {
            map.put("last_update_ts", currentTs);
            return map;
        });

        if (updateMap.size() <= 1) {
            // 过滤后仅有 last_update_ts 字段时，则说明无有效更新字段
            throw new ParameterRuntimeException("The update information cannot be empty, they can be [name]/[configuration].");
        }

        int affectedLine = userMapper.updateUser2(uid, updateMap, currentTs);

        if (affectedLine != 1) {
            log.warn("Fail to update User({}), affectedLine expected 1, but we found {}.", uid, affectedLine);
            return false;
        }
        return true;
    }

    public User queryUserByUid(Long uid) {
        return userMapper.selectById(uid);
    }

    public Map<String, User> queryUserByUid(Collection<Long> uidCollection) {
        return userMapper.queryUserMultiple(uidCollection, null);
    }
}