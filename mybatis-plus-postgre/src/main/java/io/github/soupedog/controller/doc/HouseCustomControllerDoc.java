package io.github.soupedog.controller.doc;

import io.github.soupedog.domain.po.House;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * @author Xavier
 * @date 2023/6/16
 * @since 1.0
 */
@Tag(name = "HouseCustomController", description = "基于 Mybatis 自定义 XML 配置实现")
public interface HouseCustomControllerDoc {
    ResponseEntity<?> customSaveHouse(House house);

    ResponseEntity<?> customSaveOrUpdateHouseMultiple(List<House> houseList);
}
