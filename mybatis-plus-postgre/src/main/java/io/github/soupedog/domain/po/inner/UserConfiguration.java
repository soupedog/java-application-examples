package io.github.soupedog.domain.po.inner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Xavier
 * @date 2023/5/15
 * @since 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserConfiguration {
    private Boolean rich;
    private Boolean irascible;
}
