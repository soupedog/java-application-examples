package io.github.soupedog.jpa.controller;

import hygge.util.template.HyggeJsonUtilContainer;
import hygge.web.template.definition.HyggeController;
import io.github.soupedog.jpa.controller.doc.JoinFeatureControllerDoc;
import io.github.soupedog.jpa.domain.dto.CreateCapitalPoolRequestDTO;
import io.github.soupedog.jpa.domain.po.CapitalPool;
import io.github.soupedog.jpa.domain.po.Investor;
import io.github.soupedog.jpa.service.CapitalPoolServiceImpl;
import io.github.soupedog.jpa.service.InvestorServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Xavier
 * @date 2023/8/15
 * @since 1.0
 */
@RestController
public class JoinFeatureController extends HyggeJsonUtilContainer implements JoinFeatureControllerDoc, HyggeController<ResponseEntity<?>> {
    @Autowired
    private InvestorServiceImpl investorService;
    @Autowired
    private CapitalPoolServiceImpl capitalPoolService;

    @Override
    @PostMapping("/join/investor")
    public ResponseEntity<?> saveInvestor(@RequestBody Investor investor) {
        // PO 和 DTO 用同一个是愚蠢的操作，此处仅用于图方便演示

        // Investor 已经通过 " @ManyToMany(mappedBy="xxx")" 放弃关联关系的维护，capitalPools 不为 null 产生效果为去自动验证前置 CapitalPool 数据是否存在，可能会终止操作
        investor.setCapitalPools(null);
        return success(investorService.saveInvestor(investor));
    }

    @Override
    @GetMapping("/join/investor")
    public ResponseEntity<?> queryInvestor(@RequestParam Long iid) {
        return success(investorService.queryInvestorByIid(iid));
    }

    @Override
    @PostMapping("/join/capitalPool")
    public ResponseEntity<?> saveCapitalPool(@RequestBody CreateCapitalPoolRequestDTO createCapitalPoolRequestDTO) {
        // PO 部分属性设置了不参与 json 序列化反序列化，这里 DTO 没法图方便省略了

        CapitalPool capitalPool = createCapitalPoolRequestDTO.getCapitalPool();
        capitalPool.setInvestors(createCapitalPoolRequestDTO.getInvestors());
        return success(capitalPoolService.saveCapitalPool(capitalPool));
    }

    @Override
    @GetMapping("/join/capitalPool")
    public ResponseEntity<?> queryCapitalPool(@RequestParam Long cpId) {
        return success(capitalPoolService.queryCapitalPoolByCpId(cpId));
    }
}
