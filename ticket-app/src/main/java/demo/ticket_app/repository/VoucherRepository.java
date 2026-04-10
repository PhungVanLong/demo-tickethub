package demo.ticket_app.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import demo.ticket_app.entity.Voucher;
import demo.ticket_app.entity.VoucherApplyOn;
import demo.ticket_app.entity.VoucherType;
import jakarta.persistence.LockModeType;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, UUID> {
    
    Optional<Voucher> findByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Voucher v WHERE v.code = :code")
    Optional<Voucher> findByCodeForUpdate(@Param("code") String code);
    
    List<Voucher> findByIsActive(boolean isActive);
    
    List<Voucher> findByApplyOn(VoucherApplyOn applyOn);
    
    List<Voucher> findByVoucherType(VoucherType voucherType);
    
    List<Voucher> findByAssignedToUser(UUID userId);
    
    List<Voucher> findByEventId(Long eventId);
    
    List<Voucher> findByOrganizerIdAndVoucherType(UUID organizerId, VoucherType voucherType);
    
    @Query("SELECT v FROM Voucher v WHERE v.isActive = true AND v.validFrom <= :currentTime AND v.validUntil >= :currentTime")
    List<Voucher> findActiveVouchersAt(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT v FROM Voucher v WHERE v.code = :code AND v.isActive = true AND v.validFrom <= :currentTime AND v.validUntil >= :currentTime")
    Optional<Voucher> findActiveVoucherByCode(@Param("code") String code, 
                                           @Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT v FROM Voucher v WHERE v.assignedToUser = :userId AND v.isActive = true AND v.validFrom <= :currentTime AND v.validUntil >= :currentTime")
    List<Voucher> findValidUserVouchers(@Param("userId") UUID userId, 
                                       @Param("currentTime") LocalDateTime currentTime);

        @Query("""
                        SELECT v FROM Voucher v
                        WHERE v.isActive = true
                            AND v.validFrom <= :currentTime
                            AND v.validUntil >= :currentTime
                            AND (
                                        v.assignedToUser = :userId
                                        OR (v.assignedToUser IS NULL AND v.applyOn = :applyOn)
                                    )
                        """)
        List<Voucher> findAvailableVouchersForUser(@Param("userId") UUID userId,
                                                                                                @Param("applyOn") VoucherApplyOn applyOn,
                                                                                                @Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT COUNT(v) FROM Voucher v WHERE v.isActive = true")
    long countActiveVouchers();
    
    @Query("SELECT v FROM Voucher v WHERE v.minOrderValue <= :orderAmount AND v.isActive = true AND v.validFrom <= :currentTime AND v.validUntil >= :currentTime")
    List<Voucher> findApplicableVouchers(@Param("orderAmount") BigDecimal orderAmount, 
                                        @Param("currentTime") LocalDateTime currentTime);
}
