package demo.ticket_app.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import demo.ticket_app.dto.common.PageResponse;
import demo.ticket_app.dto.event.CreateEventRequest;
import demo.ticket_app.dto.event.EventDetailResponse;
import demo.ticket_app.dto.event.EventListItemResponse;
import demo.ticket_app.dto.event.OrganizerSummaryResponse;
import demo.ticket_app.entity.ApprovalDecision;
import demo.ticket_app.entity.Event;
import demo.ticket_app.entity.EventApproval;
import demo.ticket_app.entity.EventStatus;
import demo.ticket_app.entity.SeatMap;
import demo.ticket_app.entity.TicketTier;
import demo.ticket_app.entity.TicketTierType;
import demo.ticket_app.entity.UserRole;
import demo.ticket_app.exception.ResourceNotFoundException;
import demo.ticket_app.repository.EventApprovalRepository;
import demo.ticket_app.repository.EventRepository;
import demo.ticket_app.repository.SeatMapRepository;
import demo.ticket_app.repository.TicketTierRepository;
import demo.ticket_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventService {

    private static final String AUTO_DEFAULT_TIER_NAME = "General Admission (Auto)";
    private static final String AUTO_DEFAULT_SEAT_MAP_NAME = "Default Seat Map (Auto)";

    private final EventRepository eventRepository;
    private final SeatMapRepository seatMapRepository;
    private final TicketTierRepository ticketTierRepository;
    private final EventApprovalRepository eventApprovalRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<EventListItemResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::toEventListItem)
                .toList();
    }

    public List<Event> getPublishedEvents() {
        return eventRepository.findByStatus(EventStatus.PUBLISHED);
    }

    @Transactional(readOnly = true)
    public PageResponse<EventListItemResponse> getPublishedEvents(
            int page,
            int size,
            String category,
            String city,
            Boolean featured,
            String sort
    ) {
        String normalizedCategory = normalizeFilter(category);
        String normalizedCity = normalizeFilter(city);

        if ("price_asc".equals(sort) || "price_desc".equals(sort)) {
            List<EventListItemResponse> all = eventRepository.findPublishedEvents(normalizedCategory, normalizedCity, featured)
                .stream()
                .map(this::toEventListItem)
                .sorted("price_desc".equals(sort)
                    ? Comparator.comparing(EventListItemResponse::minPrice, Comparator.nullsLast(BigDecimal::compareTo)).reversed()
                    : Comparator.comparing(EventListItemResponse::minPrice, Comparator.nullsLast(BigDecimal::compareTo)))
                .toList();

            int safePage = Math.max(page, 0);
            int safeSize = Math.max(size, 1);
            int fromIndex = Math.min(safePage * safeSize, all.size());
            int toIndex = Math.min(fromIndex + safeSize, all.size());
            List<EventListItemResponse> content = all.subList(fromIndex, toIndex);
            int totalPages = all.isEmpty() ? 0 : (int) Math.ceil((double) all.size() / safeSize);

            return new PageResponse<>(content, safePage, safeSize, all.size(), totalPages);
        }

        var pageable = PageRequest.of(page, size, resolveSort(sort));
        var events = eventRepository.findPublishedEvents(normalizedCategory, normalizedCity, featured, pageable).map(this::toEventListItem);
        return PageResponse.from(events);
    }

    @Transactional(readOnly = true)
    public List<EventListItemResponse> getPendingEvents() {
        return eventRepository.findByStatus(EventStatus.PENDING).stream()
                .map(this::toEventListItem)
                .toList();
    }

    public List<Event> getEventsByOrganizer(UUID organizerId) {
        return eventRepository.findByOrganizerId(organizerId);
    }

    @Transactional(readOnly = true)
    public List<EventListItemResponse> getEventListByOrganizer(UUID organizerId) {
        return eventRepository.findByOrganizerId(organizerId).stream()
                .map(this::toEventListItem)
                .toList();
    }

    public List<Event> getEventsByCity(String city) {
        return eventRepository.findByCity(city);
    }

    @Transactional(readOnly = true)
    public List<EventListItemResponse> getEventListByCity(String city) {
        return eventRepository.findByCity(city).stream()
                .map(this::toEventListItem)
                .toList();
    }

    public List<Event> getEventsByCityAndStatus(String city, EventStatus status) {
        return eventRepository.findByStatusAndCity(status, city);
    }

    @Transactional(readOnly = true)
    public List<EventListItemResponse> getEventListByCityAndStatus(String city, EventStatus status) {
        return eventRepository.findByStatusAndCity(status, city).stream()
                .map(this::toEventListItem)
                .toList();
    }

    public Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
    }

    @Transactional(readOnly = true)
    public EventDetailResponse getEventDetailById(Long eventId, UUID requesterId) {
        Event event = getEventById(eventId);
        boolean isOwner = requesterId != null && requesterId.equals(event.getOrganizerId());
        if (event.getStatus() != EventStatus.PUBLISHED && !isOwner) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }
        return toEventDetail(event);
    }

    public Event createEvent(CreateEventRequest request, UUID organizerId) {
        if (!request.startTime().isBefore(request.endTime())) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }
        Event event = Event.builder()
                .organizerId(organizerId)
                .title(request.title())
                .slug(buildSlug(request.title()))
                .category(request.category())
                .description(request.description())
                .venue(request.venue())
                .city(request.city())
                .country(request.country())
                .locationCoords(request.locationCoords())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .bannerUrl(request.bannerUrl())
                .imageUrl(request.imageUrl())
                .featured(Boolean.TRUE.equals(request.featured()))
                .tags(joinTags(request.tags()))
                .rating(BigDecimal.ZERO)
                .reviewCount(0L)
                .status(EventStatus.PENDING)
                .isPublished(false)
                .build();

        Event savedEvent = eventRepository.save(event);
        createAutoDefaultTierIfRequested(savedEvent, request);
        log.info("Created new event with id: {}", savedEvent.getId());
        return savedEvent;
    }

    /** Legacy overload — kept for backward compatibility */
    public Event createEvent(Event event) {
        event.setStatus(EventStatus.PENDING);
        event.setIsPublished(false);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        Event savedEvent = eventRepository.save(event);
        log.info("Created new event with id: {}", savedEvent.getId());
        return savedEvent;
    }

    public Event updateEvent(Long eventId, Event eventDetails, UUID requesterId) {
        Event existingEvent = getEventById(eventId);
        if (!canManageEvent(existingEvent, requesterId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "You do not have permission to update this event");
        }
        if (eventDetails.getStartTime() != null && eventDetails.getEndTime() != null
                && !eventDetails.getStartTime().isBefore(eventDetails.getEndTime())) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }

        existingEvent.setTitle(eventDetails.getTitle());
        existingEvent.setDescription(eventDetails.getDescription());
        existingEvent.setVenue(eventDetails.getVenue());
        existingEvent.setCity(eventDetails.getCity());
        existingEvent.setCountry(eventDetails.getCountry());
        existingEvent.setLocationCoords(eventDetails.getLocationCoords());
        existingEvent.setStartTime(eventDetails.getStartTime());
        existingEvent.setEndTime(eventDetails.getEndTime());
        existingEvent.setBannerUrl(eventDetails.getBannerUrl());
        existingEvent.setImageUrl(eventDetails.getImageUrl());
        if (eventDetails.getFeatured() != null) {
            existingEvent.setFeatured(eventDetails.getFeatured());
        }
        if (eventDetails.getTags() != null) {
            existingEvent.setTags(eventDetails.getTags());
        }
        if (eventDetails.getCategory() != null) {
            existingEvent.setCategory(eventDetails.getCategory());
        }
        existingEvent.setUpdatedAt(LocalDateTime.now());

        Event updatedEvent = eventRepository.save(existingEvent);
        log.info("Updated event with id: {}", updatedEvent.getId());
        return updatedEvent;
    }

    public Event approveEvent(Long eventId, UUID adminId, String reason) {
        Objects.requireNonNull(adminId, "adminId is required");
        Event event = getEventById(eventId);
        event.setStatus(EventStatus.PUBLISHED);
        event.setIsPublished(true);
        event.setUpdatedAt(LocalDateTime.now());

        userRepository.findById(event.getOrganizerId()).ifPresent(user -> {
            if (user.getRole() == UserRole.CUSTOMER) {
                user.setRole(UserRole.ORGANIZER);
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
            }
        });

        eventApprovalRepository.save(EventApproval.builder()
                .eventId(eventId)
                .adminId(adminId)
                .decision(ApprovalDecision.APPROVED)
                .reason(reason)
                .decidedAt(LocalDateTime.now())
                .build());

        Event approvedEvent = eventRepository.save(event);
        log.info("Approved event with id: {}", eventId);
        return approvedEvent;
    }

    public Event rejectEvent(Long eventId, UUID adminId, String reason) {
        Objects.requireNonNull(adminId, "adminId is required");
        Event event = getEventById(eventId);
        event.setStatus(EventStatus.REJECTED);
        event.setIsPublished(false);
        event.setUpdatedAt(LocalDateTime.now());

        Event rejectedEvent = eventRepository.save(event);

        long publishedEvents = eventRepository.countByOrganizerIdAndStatus(event.getOrganizerId(), EventStatus.PUBLISHED);
        if (publishedEvents == 0) {
            userRepository.findById(event.getOrganizerId()).ifPresent(user -> {
                if (user.getRole() == UserRole.ORGANIZER) {
                    user.setRole(UserRole.CUSTOMER);
                    user.setUpdatedAt(LocalDateTime.now());
                    userRepository.save(user);
                }
            });
        }

        eventApprovalRepository.save(EventApproval.builder()
                .eventId(eventId)
                .adminId(adminId)
                .decision(ApprovalDecision.REJECTED)
                .reason(reason)
                .decidedAt(LocalDateTime.now())
                .build());

        log.info("Rejected event with id: {}", eventId);
        return rejectedEvent;
    }

    @Transactional(readOnly = true)
    public void validateOrganizerCanManageStaffForEvent(Long eventId, UUID organizerId) {
        Objects.requireNonNull(organizerId, "organizerId is required");
        Event event = getEventById(eventId);

        if (!organizerId.equals(event.getOrganizerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to manage staff for this event");
        }

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Staff accounts can only be created after event approval");
        }
    }

    public void deleteEvent(Long eventId, UUID requesterId) {
        Event event = getEventById(eventId);
        if (!canManageEvent(event, requesterId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "You do not have permission to delete this event");
        }
        eventRepository.delete(event);
        log.info("Deleted event with id: {}", eventId);
    }

    private boolean canManageEvent(Event event, UUID requesterId) {
        if (requesterId == null) {
            return false;
        }
        if (event.getOrganizerId().equals(requesterId)) {
            return true;
        }
        return userRepository.findById(requesterId)
                .map(user -> user.getRole() == UserRole.ADMIN)
                .orElse(false);
    }

    public List<Event> searchEvents(String searchTerm) {
        return eventRepository.findByTitleOrDescriptionContaining(searchTerm);
    }

    @Transactional(readOnly = true)
    public List<EventListItemResponse> searchEventListItems(String searchTerm) {
        return eventRepository.findByTitleOrDescriptionContaining(searchTerm).stream()
                .map(this::toEventListItem)
                .toList();
    }

    public long getTotalEventsCount() {
        return eventRepository.count();
    }

    public long getPublishedEventsCount() {
        return eventRepository.countByStatus(EventStatus.PUBLISHED);
    }

    public long getPendingEventsCount() {
        return eventRepository.countByStatus(EventStatus.PENDING);
    }

    public long getEventsByOrganizerCount(UUID organizerId) {
        return eventRepository.countByOrganizerIdAndStatus(organizerId, EventStatus.PUBLISHED);
    }

    public long getTotalSoldTicketsForEvent(Long eventId) {
        return ticketTierRepository.countSoldTicketsByEventId(eventId);
    }

    public long getTotalTicketsForEvent(Long eventId) {
        return ticketTierRepository.countTotalTicketsByEventId(eventId);
    }

    private Sort resolveSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "startTime");
        }
        return switch (sort) {
            case "date_desc" -> Sort.by(Sort.Direction.DESC, "startTime");
            case "date_asc" -> Sort.by(Sort.Direction.ASC, "startTime");
            case "rating_desc" -> Sort.by(Sort.Direction.DESC, "rating");
            default -> Sort.by(Sort.Direction.ASC, "startTime");
        };
    }

    private String normalizeFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    private EventListItemResponse toEventListItem(Event event) {
        Event normalized = normalizeEvent(event);
        EventPricingMetrics metrics = pricingMetrics(event.getId());
        return new EventListItemResponse(
                normalized.getId(),
                normalized.getTitle(),
                normalized.getSlug(),
                normalized.getCategory(),
                toUtc(normalized.getStartTime()),
                toUtc(normalized.getEndTime()),
                normalized.getVenue(),
                normalized.getCity(),
                normalized.getCountry(),
                normalized.getImageUrl(),
                normalized.getBannerUrl(),
                metrics.minPrice(),
                metrics.originalPrice(),
                normalized.getStatus(),
                Boolean.TRUE.equals(normalized.getFeatured()),
                parseTags(normalized.getTags()),
                Objects.requireNonNullElse(normalized.getRating(), BigDecimal.ZERO),
                Objects.requireNonNullElse(normalized.getReviewCount(), 0L),
                metrics.soldCount(),
                metrics.totalCapacity(),
                toOrganizer(normalized.getOrganizerId())
        );
    }

    private EventDetailResponse toEventDetail(Event event) {
        Event normalized = normalizeEvent(event);
        EventPricingMetrics metrics = pricingMetrics(event.getId());
        return new EventDetailResponse(
                normalized.getId(),
                normalized.getTitle(),
                normalized.getSlug(),
                normalized.getCategory(),
                normalized.getDescription(),
                toUtc(normalized.getStartTime()),
                toUtc(normalized.getEndTime()),
                normalized.getVenue(),
                normalized.getCity(),
                normalized.getCountry(),
                normalized.getImageUrl(),
                normalized.getBannerUrl(),
                metrics.minPrice(),
                metrics.originalPrice(),
                normalized.getStatus(),
                Boolean.TRUE.equals(normalized.getFeatured()),
                parseTags(normalized.getTags()),
                Objects.requireNonNullElse(normalized.getRating(), BigDecimal.ZERO),
                Objects.requireNonNullElse(normalized.getReviewCount(), 0L),
                metrics.soldCount(),
                metrics.totalCapacity(),
                toOrganizer(normalized.getOrganizerId())
        );
    }

    private Event normalizeEvent(Event event) {
        if (event.getSlug() == null || event.getSlug().isBlank()) {
            event.setSlug(buildSlug(event.getTitle()));
        }
        if (event.getCategory() == null || event.getCategory().isBlank()) {
            event.setCategory("General");
        }
        if (event.getCountry() == null || event.getCountry().isBlank()) {
            event.setCountry("Vietnam");
        }
        if (event.getImageUrl() == null || event.getImageUrl().isBlank()) {
            event.setImageUrl("https://placehold.co/600x400?text=Event");
        }
        if (event.getBannerUrl() == null || event.getBannerUrl().isBlank()) {
            event.setBannerUrl(event.getImageUrl());
        }
        if (event.getTags() == null) {
            event.setTags("");
        }
        if (event.getRating() == null) {
            event.setRating(BigDecimal.ZERO);
        }
        if (event.getReviewCount() == null) {
            event.setReviewCount(0L);
        }
        if (event.getFeatured() == null) {
            event.setFeatured(false);
        }
        return event;
    }

    private OrganizerSummaryResponse toOrganizer(UUID organizerId) {
        if (organizerId == null) {
            return new OrganizerSummaryResponse(null, "Unknown organizer", false);
        }
        var user = userRepository.findById(organizerId).orElse(null);
        if (user == null) {
            return new OrganizerSummaryResponse(organizerId, "Unknown organizer", false);
        }
        return new OrganizerSummaryResponse(
                organizerId,
                user.getFullName() != null && !user.getFullName().isBlank() ? user.getFullName() : user.getEmail(),
                Boolean.TRUE.equals(user.getIsVerified())
        );
    }

    private EventPricingMetrics pricingMetrics(Long eventId) {
        List<TicketTier> tiers = ticketTierRepository.findBySeatMapEventId(eventId);
        boolean hasCustomTier = tiers.stream().anyMatch(tier -> !isAutoDefaultTier(tier));
        if (hasCustomTier) {
            tiers = tiers.stream().filter(tier -> !isAutoDefaultTier(tier)).toList();
        }
        if (tiers.isEmpty()) {
            return new EventPricingMetrics(null, null, 0L, 0L);
        }
        BigDecimal minPrice = tiers.stream().map(TicketTier::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal maxPrice = tiers.stream().map(TicketTier::getPrice).max(BigDecimal::compareTo).orElse(minPrice);
        long totalCapacity = tiers.stream().map(TicketTier::getQuantityTotal).filter(Objects::nonNull).mapToLong(Integer::longValue).sum();
        long available = tiers.stream().map(TicketTier::getQuantityAvailable).filter(Objects::nonNull).mapToLong(Integer::longValue).sum();
        long soldCount = Math.max(0, totalCapacity - available);
        return new EventPricingMetrics(minPrice, maxPrice, soldCount, totalCapacity);
    }

    private OffsetDateTime toUtc(LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.atOffset(ZoneOffset.UTC);
    }

    private List<String> parseTags(String raw) {
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private String joinTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return tags.stream().filter(Objects::nonNull).map(String::trim).filter(s -> !s.isBlank()).reduce((a, b) -> a + "," + b).orElse(null);
    }

    private String buildSlug(String title) {
        String base = title == null ? "event" : title.trim().toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
        if (base.isBlank()) {
            base = "event";
        }
        return base + "-" + System.currentTimeMillis();
    }

    private record EventPricingMetrics(BigDecimal minPrice, BigDecimal originalPrice, long soldCount, long totalCapacity) {
    }

    private void createAutoDefaultTierIfRequested(Event event, CreateEventRequest request) {
        if (request.defaultPrice() == null) {
            return;
        }
        if (request.defaultPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        int quantity = java.util.Optional.ofNullable(request.defaultTierQuantity()).orElse(1);

        SeatMap seatMap = SeatMap.builder()
                .eventId(event.getId())
                .name(AUTO_DEFAULT_SEAT_MAP_NAME)
                .totalRows(1)
                .totalCols(Math.max(1, quantity))
                .layoutJson(null)
                .imageUrl(null)
                .build();
        SeatMap savedSeatMap = seatMapRepository.save(seatMap);

        TicketTier tier = TicketTier.builder()
                .seatMapId(savedSeatMap.getId())
                .name(AUTO_DEFAULT_TIER_NAME)
                .tierType(TicketTierType.GENERAL)
                .price(request.defaultPrice())
                .quantityTotal(quantity)
                .quantityAvailable(quantity)
                .colorCode(null)
                .saleStart(event.getStartTime())
                .saleEnd(event.getEndTime())
                .build();
        ticketTierRepository.save(tier);
    }

    private boolean isAutoDefaultTier(TicketTier tier) {
        return AUTO_DEFAULT_TIER_NAME.equals(tier.getName());
    }
}
