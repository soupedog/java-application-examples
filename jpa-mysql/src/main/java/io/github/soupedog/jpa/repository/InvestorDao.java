package io.github.soupedog.jpa.repository;

import io.github.soupedog.jpa.domain.po.Investor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Xavier
 * @date 2023/8/15
 * @since 1.0
 */
@Repository
public interface InvestorDao extends JpaRepository<Investor, Long> {

}
