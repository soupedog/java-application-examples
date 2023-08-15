package io.github.soupedog.jpa.service;

import io.github.soupedog.jpa.domain.po.CapitalPool;
import io.github.soupedog.jpa.repository.CapitalPoolDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Xavier
 * @date 2023/8/15
 * @since 1.0
 */
@Service
public class CapitalPoolServiceImpl {
    @Autowired
    private CapitalPoolDao capitalPoolDao;

    public CapitalPool queryCapitalPoolByCpId(Long cpId) {
        return capitalPoolDao.findById(cpId).orElse(null);
    }

    public CapitalPool saveCapitalPool(CapitalPool capitalPool) {
        return capitalPoolDao.save(capitalPool);
    }
}
