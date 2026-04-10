package demo.ticket_app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import demo.ticket_app.dto.admin.CreatePlatformSaleRequest;
import demo.ticket_app.entity.PlatformSale;
import demo.ticket_app.entity.Voucher;
import demo.ticket_app.entity.VoucherApplyOn;
import demo.ticket_app.entity.VoucherType;
import demo.ticket_app.exception.ResourceNotFoundException;
import demo.ticket_app.repository.PlatformSaleRepository;
import demo.ticket_app.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional

public class PlatformSaleService {
            /**
             * Lấy tất cả voucher thuộc các platform sale đang active
             */
            public List<Voucher> getAllVouchersInActivePlatformSales() {
                return platformSaleRepository.findActiveAt(LocalDateTime.now())
                        .stream()
                        .map(PlatformSale::getVoucher)
                        .filter(java.util.Objects::nonNull)
                        .toList();
            }
        /**
         * Find PlatformSale by voucher code
         */
        public PlatformSale findByVoucherCode(String voucherCode) {
            return platformSaleRepository.findByVoucherCode(voucherCode)
                    .orElseThrow(() -> new ResourceNotFoundException("No PlatformSale found for voucher code: " + voucherCode));
        }
    private final PlatformSaleRepository platformSaleRepository;
    private final VoucherRepository voucherRepository;

    /**
     * Create a platform-wide sale (admin only)
     */
    public PlatformSale createSale(CreatePlatformSaleRequest request) {
        if (request.validFrom().isAfter(request.validUntil())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "validFrom must be before validUntil");
        }

        // Tạo voucher platform
        Voucher voucher = Voucher.builder()
                .code(generateVoucherCode())
                .voucherType(VoucherType.PLATFORM)
                .discountType(null) // Cần xác định loại discount phù hợp
                .discountValue(request.discountPercentage())
                .minOrderValue(null)
                .usageLimit(null)
                .usedCount(0)
                .validFrom(request.validFrom())
                .validUntil(request.validUntil())
                .applyOn(VoucherApplyOn.ALL)
                .createdBy(null)
                .isActive(true)
                .build();
        voucher = voucherRepository.save(voucher);

        // Tạo platform_sale và liên kết voucher
        PlatformSale sale = PlatformSale.builder()
            .name(request.name())
            .description(request.description())
            .discountPercentage(request.discountPercentage())
            .validFrom(request.validFrom())
            .validUntil(request.validUntil())
            .isActive(true)
            .voucher(voucher)
            .build();

        PlatformSale saved = platformSaleRepository.save(sale);
        log.info("Created platform sale: {} ({}% off, voucherId: {})", saved.getName(), saved.getDiscountPercentage(), voucher.getId());
        return saved;
    }

    private String generateVoucherCode() {
        return "PLAT-" + System.currentTimeMillis();
    }

    /**
     * Get active platform sales
     */
    public List<PlatformSale> getActiveSales() {
        return platformSaleRepository.findActiveAt(LocalDateTime.now());
    }

    /**
     * Get highest active sale (best discount)
     */
    public PlatformSale getHighestActiveSale() {
        return platformSaleRepository.findHighestActiveSaleAt(LocalDateTime.now())
                .orElse(null);
    }

    /**
     * Deactivate a sale
     */
    public void deactivateSale(UUID saleId) {
        PlatformSale sale = platformSaleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found"));
        sale.setIsActive(false);
        platformSaleRepository.save(sale);
        log.info("Deactivated sale: {}", saleId);
    }

    /**
     * List all sales
     */
    public List<PlatformSale> getAllSales(boolean isActive) {
        return platformSaleRepository.findByIsActiveOrderByCreatedAtDesc(isActive);
    }
}
