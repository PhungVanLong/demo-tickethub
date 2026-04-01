package demo.ticket_app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "seat_maps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatMap {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "event_id", nullable = false)
    private Long eventId;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "layout_json", columnDefinition = "JSON")
    private String layoutJson;

    @Column(name = "image_url", length = 500)
    private String imageUrl;
    
    @Column(name = "total_rows", nullable = false)
    private Integer totalRows;
    
    @Column(name = "total_cols", nullable = false)
    private Integer totalCols;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    private Event event;
    
    @OneToMany(mappedBy = "seatMap", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TicketTier> ticketTiers;
    
    @OneToMany(mappedBy = "seatMap", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Seat> seats;
}
