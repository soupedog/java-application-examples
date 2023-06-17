package io.github.soupedog.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.soupedog.domain.po.House;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author Xavier
 * @date 2023/6/16
 * @since 1.0
 */
@Mapper
public interface HouseMapper extends BaseMapper<House> {
    Long getNextHid();

    int customSaveHouse(@Param("house") House house);

    /**
     * 没有复杂逻辑，仅演示效果。postgre "null" 和字段类型识别有坑，非字符串类型需要 "::" 强制指定
     * <p>
     * 批量插入，如果 location 冲突，那么直接更新冲突记录的最后修改时间
     * <p>
     * 有个弊端，初次插入时能同时插入 conflict 的记录，后续再插入则
     * <pre>Ensure that not more than one source row matches any one target row.; nested exception is org.postgresql.util.PSQLException: ERROR: MERGE command cannot affect row a second time</pre> <br/>
     * <p>
     * 需要保证你的数组中不存在冲突元素
     */
    int customSaveOrUpdateHouseMultiple(@Param("houseList") List<House> houseList);
}