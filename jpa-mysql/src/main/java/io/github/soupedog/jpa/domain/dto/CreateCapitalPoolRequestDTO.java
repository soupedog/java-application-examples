package io.github.soupedog.jpa.domain.dto;

import io.github.soupedog.jpa.domain.po.CapitalPool;
import io.github.soupedog.jpa.domain.po.Investor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author Xavier
 * @date 2023/8/15
 * @since 1.0
 */
@Getter
@Setter
@Builder
@Generated
@NoArgsConstructor
@AllArgsConstructor
public class CreateCapitalPoolRequestDTO {
    private CapitalPool capitalPool;
    private List<Investor> investors;
}
