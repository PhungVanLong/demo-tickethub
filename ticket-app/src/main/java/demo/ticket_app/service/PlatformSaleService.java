package demo.ticket_app.service;

import demo.ticket_app.dto.admin.CreatePlatformSaleRequest;
import demo.ticket_app.entity.PlatformSale;
import demo.ticket_app.exception.ResourceNotFoundException;
import demo.ticket_app.repository.PlatformSaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PlatformSaleService {

    private final PlatformSaleRepository platformSaleRepository;

    /**
     * Create a platform-wide sale (admin only)
     */
    public PlatformSale createSale(CreatePlatformSaleRequest request) {
        if (request.validFrom().isAfter(request.validUntil())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "validFrom must be before validUntil");
        }
        
        PlatformSale sale = PlatformSale.builder()
                .name(request.name())
                .description(request.description())
                .discountPercentage(request.discountPercentage())
                .validFrom(request.validFrom())
                .validUntil(request.validUntil())
                .isActive(true)
                .build();
        
        PlatformSale saved = platformSaleRepository.save(sale);
        log.info("Created platform sale: {} ({}% off)", saved.getName(), saved.getDiscountPercentage());
        return saved;
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
