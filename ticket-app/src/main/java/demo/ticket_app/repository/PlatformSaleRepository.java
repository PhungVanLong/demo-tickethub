package demo.ticket_app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import demo.ticket_app.entity.PlatformSale;

@Repository
public interface PlatformSaleRepository extends JpaRepository<PlatformSale, UUID> {
        @Query("SELECT ps FROM PlatformSale ps JOIN ps.voucher v WHERE v.code = :voucherCode")
        Optional<PlatformSale> findByVoucherCode(@Param("voucherCode") String voucherCode);
    
    @Query("SELECT ps FROM PlatformSale ps WHERE ps.isActive = true AND ps.validFrom <= :currentTime AND ps.validUntil >= :currentTime")
    List<PlatformSale> findActiveAt(@Param("currentTime") LocalDateTime currentTime);
    
    @Query("SELECT ps FROM PlatformSale ps WHERE ps.isActive = true AND ps.validFrom <= :currentTime AND ps.validUntil >= :currentTime ORDER BY ps.discountPercentage DESC LIMIT 1")
    Optional<PlatformSale> findHighestActiveSaleAt(@Param("currentTime") LocalDateTime currentTime);
    
    List<PlatformSale> findByIsActiveOrderByCreatedAtDesc(boolean isActive);
}
