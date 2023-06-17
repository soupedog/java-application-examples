package io.github.soupedog.controller;

import hygge.web.template.HyggeWebUtilContainer;
import hygge.web.template.definition.HyggeController;
import io.github.soupedog.controller.doc.HouseCustomControllerDoc;
import io.github.soupedog.domain.po.House;
import io.github.soupedog.service.impl.HouseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Xavier
 * @date 2023/6/16
 * @since 1.0
 */
@RestController
public class HouseCustomController extends HyggeWebUtilContainer implements HouseCustomControllerDoc, HyggeController<ResponseEntity<?>> {
    @Autowired
    private HouseServiceImpl houseService;

    @Override
    @PostMapping("/house/custom")
    public ResponseEntity<?> customSaveHouse(@RequestBody House house) {
        // 这是一个愚蠢的操作，PO 和 DTO 用同一个，此处仅用于图方便演示
        house.setHid(null);
        house.setCreateTs(null);
        house.setLastUpdateTs(null);
        return success(houseService.customSaveHouse(house));
    }

    @Override
    @PostMapping("/house/custom/multiple")
    public ResponseEntity<?> customSaveOrUpdateHouseMultiple(@RequestBody List<House> houseList) {
        houseList.forEach(item -> {
            item.setHid(null);
            item.setCreateTs(null);
            item.setLastUpdateTs(null);
        });

        houseService.customSaveOrUpdateHouseMultiple(houseList);
        return success(houseList);
    }
}
