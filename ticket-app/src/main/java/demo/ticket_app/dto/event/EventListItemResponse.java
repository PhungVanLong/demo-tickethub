package demo.ticket_app.dto.event;

import demo.ticket_app.entity.EventStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record EventListItemResponse(
        Long id,
        String title,
        String slug,
        String category,
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
