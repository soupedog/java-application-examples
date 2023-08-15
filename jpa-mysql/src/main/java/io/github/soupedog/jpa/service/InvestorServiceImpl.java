package io.github.soupedog.jpa.service;

import io.github.soupedog.jpa.domain.po.Investor;
import io.github.soupedog.jpa.repository.InvestorDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Xavier
 * @date 2023/8/15
 * @since 1.0
 */
@Service
public class InvestorServiceImpl {
    @Autowired
    private InvestorDao investorDao;

    public Investor queryInvestorByIid(Long iid) {
        return investorDao.findById(iid).orElse(null);
    }

    public Investor saveInvestor(Investor investor) {
        return investorDao.save(investor);
    }
}
