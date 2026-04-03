package demo.ticket_app.dto.event;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import demo.ticket_app.entity.EventStatus;

public record EventDetailResponse(
        Long id,
        String title,
        String slug,
        String category,
        String description,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        String venue,
        String city,
        String country,
        String imageUrl,
        String bannerUrl,
        BigDecimal minPrice,
        BigDecimal originalPrice,
        EventStatus status,
        boolean featured,
        List<String> tags,
        BigDecimal rating,
        long reviewCount,
        long soldCount,
        long totalCapacity,
        OrganizerSummaryResponse organizer
) {
}
