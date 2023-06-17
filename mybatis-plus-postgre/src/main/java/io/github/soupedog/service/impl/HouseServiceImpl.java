package io.github.soupedog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.soupedog.dao.HouseMapper;
import io.github.soupedog.domain.po.House;
import io.github.soupedog.service.HouseService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Xavier
 * @date 2023/6/16
 * @since 1.0
 */
@Service
public class HouseServiceImpl extends ServiceImpl<HouseMapper, House> implements HouseService {
    public Long getNextHid() {
        return baseMapper.getNextHid();
    }

    public House customSaveHouse(House house) {
        baseMapper.customSaveHouse(house);
        return house;
    }

    public int customSaveOrUpdateHouseMultiple(List<House> houseList) {
        return baseMapper.customSaveOrUpdateHouseMultiple(houseList);
    }
}
