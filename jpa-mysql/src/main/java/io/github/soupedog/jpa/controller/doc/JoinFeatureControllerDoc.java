package io.github.soupedog.jpa.controller.doc;

import io.github.soupedog.jpa.domain.dto.CreateCapitalPoolRequestDTO;
import io.github.soupedog.jpa.domain.po.Investor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

/**
 * @author Xavier
 * @date 2023/8/15
 * @since 1.0
 */
@Tag(name = "JoinFeatureController", description = "基于 Spring-Data-Jpa 提供的注解实现")
public interface JoinFeatureControllerDoc {
    @Operation(summary = "创建或修改投资人", description = "根据主键创建或修改")
    ResponseEntity<?> saveInvestor(Investor investor);

    ResponseEntity<?> queryInvestor(Long iid);

    @Operation(summary = "创建或维护资金池", description = "根据主键创建或修改，请确保 资金池发起人/投资人 对象均已存在。<br/> Tips：维护关联关系只需外键正确，关联对象的其他属性可乱写，并不参与落库")
    ResponseEntity<?> saveCapitalPool(CreateCapitalPoolRequestDTO createCapitalPoolRequestDTO);

    ResponseEntity<?> queryCapitalPool(Long cpId);
}
