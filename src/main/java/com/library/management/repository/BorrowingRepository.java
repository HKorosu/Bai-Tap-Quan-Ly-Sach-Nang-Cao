package com.library.management.repository;

import com.library.management.model.Borrowing;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {
    @EntityGraph(attributePaths = {"member", "details", "details.book", "payments", "waivers"})
    List<Borrowing> findAllByOrderByBorrowedDateDesc();

    @EntityGraph(attributePaths = {"member", "details", "details.book", "payments", "waivers"})
    @Query("select distinct b from Borrowing b where b.id = :id")
    Optional<Borrowing> findWithDetailsById(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"member", "details", "details.book", "payments", "waivers"})
    @Query("select distinct b from Borrowing b where b.id = :id")
    Optional<Borrowing> findLockedWithDetailsById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"member", "details"})
    List<Borrowing> findByMemberId(Long memberId);
}
