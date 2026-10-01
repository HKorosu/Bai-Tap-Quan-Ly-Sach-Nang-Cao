package com.library.management.repository;

import com.library.management.model.FinePolicy;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface FinePolicyRepository extends JpaRepository<FinePolicy, Long> {
    Optional<FinePolicy> findFirstByActiveTrue();
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from FinePolicy p where p.active = true")
    List<FinePolicy> findAllActiveForUpdate();
    List<FinePolicy> findAllByOrderByIdDesc();
}
