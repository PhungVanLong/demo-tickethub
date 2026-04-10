package demo.ticket_app.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import demo.ticket_app.entity.PlatformVoucher;

public interface PlatformVoucherRepository extends JpaRepository<PlatformVoucher, UUID> {
    Optional<PlatformVoucher> findByCode(String code);
}
