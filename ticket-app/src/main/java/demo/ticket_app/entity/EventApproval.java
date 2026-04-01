package demo.ticket_app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "event_approvals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventApproval {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "event_id", nullable = false)
    private Long eventId;
    
    @Column(name = "admin_id", nullable = false)
    private UUID adminId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false)
    private ApprovalDecision decision;
    
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
    
    @Column(name = "decided_at")
    private LocalDateTime decidedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", insertable = false, updatable = false)
    private Event event;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", insertable = false, updatable = false)
    private User admin;
}
