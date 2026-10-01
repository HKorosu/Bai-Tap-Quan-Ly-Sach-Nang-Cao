package com.library.management.service;

import com.library.management.model.FinePolicy;
import com.library.management.repository.FinePolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class FinePolicyService {
    private final FinePolicyRepository repository;
    public FinePolicyService(FinePolicyRepository repository) { this.repository = repository; }

    public List<FinePolicy> findAll() { return repository.findAllByOrderByIdDesc(); }

    @Transactional
    public FinePolicy activate(BigDecimal dailyAmount, BigDecimal maxAmount, int graceDays) {
        if (dailyAmount == null || dailyAmount.signum() < 0) {
            throw new IllegalArgumentException("Phí phạt mỗi ngày không được âm.");
        }
        if (maxAmount != null && maxAmount.signum() < 0) {
            throw new IllegalArgumentException("Mức phí tối đa không được âm.");
        }
        if (graceDays < 0) throw new IllegalArgumentException("Số ngày miễn phí không được âm.");
        repository.findAllActiveForUpdate().forEach(FinePolicy::deactivate);
        return repository.save(new FinePolicy(dailyAmount, maxAmount, graceDays, true));
    }
}
