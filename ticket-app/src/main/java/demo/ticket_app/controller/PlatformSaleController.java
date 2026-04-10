package demo.ticket_app.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import demo.ticket_app.dto.admin.CreatePlatformSaleRequest;
import demo.ticket_app.entity.PlatformSale;
import demo.ticket_app.service.PlatformSaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/platform-sales")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PlatformSaleController {

    private final PlatformSaleService platformSaleService;

    /**
     * Create a platform-wide sale
     */
    @PostMapping
    public ResponseEntity<PlatformSale> createSale(@Valid @RequestBody CreatePlatformSaleRequest request) {
        PlatformSale sale = platformSaleService.createSale(request);
        return ResponseEntity.ok(sale);
    }

    /**
     * Get active platform sales
     */
    @GetMapping("/active")
    public ResponseEntity<List<PlatformSale>> getActiveSales() {
        List<PlatformSale> sales = platformSaleService.getActiveSales();
        return ResponseEntity.ok(sales);
    }

    /**
     * Get all platform sales
     */
    @GetMapping
    public ResponseEntity<List<PlatformSale>> getAllSales(@RequestParam(defaultValue = "true") boolean isActive) {
        List<PlatformSale> sales = platformSaleService.getAllSales(isActive);
        return ResponseEntity.ok(sales);
    }

    /**
     * Deactivate a sale
     */
    @DeleteMapping("/{saleId}")
    public ResponseEntity<Void> deactivateSale(@PathVariable UUID saleId) {
        platformSaleService.deactivateSale(saleId);
        return ResponseEntity.noContent().build();
    }
}
