package demo.ticket_app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "seat_map_id", nullable = false)
    private Long seatMapId;
    
    @Column(name = "ticket_tier_id")
    private Long ticketTierId;
    
    @Column(name = "row_label", nullable = false, length = 10)
    private String rowLabel;
    
    @Column(name = "col_number", nullable = false)
    private Integer colNumber;
    
    @Column(name = "seat_code", nullable = false, unique = true, length = 20)
    private String seatCode;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SeatStatus status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_map_id", insertable = false, updatable = false)
    private SeatMap seatMap;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_tier_id", insertable = false, updatable = false)
    private TicketTier ticketTier;
    
    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = SeatStatus.AVAILABLE;
        }
    }
}
