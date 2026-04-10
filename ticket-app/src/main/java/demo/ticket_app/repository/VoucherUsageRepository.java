package demo.ticket_app.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import demo.ticket_app.entity.VoucherUsage;

@Repository
public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, UUID> {
}