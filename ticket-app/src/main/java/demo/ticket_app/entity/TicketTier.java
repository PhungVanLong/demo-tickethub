package demo.ticket_app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ticket_tiers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketTier {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "seat_map_id", nullable = false)
    private Long seatMapId;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tier_type", nullable = false)
    private TicketTierType tierType;
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "quantity_total", nullable = false)
    private Integer quantityTotal;
    
    @Column(name = "quantity_available", nullable = false)
    private Integer quantityAvailable;
    
    @Column(name = "color_code", length = 10)
    private String colorCode;
    
    @Column(name = "sale_start")
    private LocalDateTime saleStart;
    
    @Column(name = "sale_end")
    private LocalDateTime saleEnd;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_map_id", insertable = false, updatable = false)
    private SeatMap seatMap;
    
    @OneToMany(mappedBy = "ticketTier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Seat> seats;
    
    @OneToMany(mappedBy = "ticketTier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;
    
    @OneToMany(mappedBy = "ticketTier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketPromotion> promotions;
}
