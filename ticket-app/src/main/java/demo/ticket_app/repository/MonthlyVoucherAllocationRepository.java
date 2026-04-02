package demo.ticket_app.repository;

import demo.ticket_app.entity.MonthlyVoucherAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MonthlyVoucherAllocationRepository extends JpaRepository<MonthlyVoucherAllocation, UUID> {
    
    Optional<MonthlyVoucherAllocation> findByUserIdAndYearMonth(@Param("userId") UUID userId, 
                                                                @Param("yearMonth") String yearMonth);
    
    List<MonthlyVoucherAllocation> findByUserId(UUID userId);
    
    List<MonthlyVoucherAllocation> findByYearMonth(String yearMonth);
}
