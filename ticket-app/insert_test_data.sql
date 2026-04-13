DO $$
DECLARE
    event1_id bigint;
    event2_id bigint;
    sm1_id bigint;
    sm2_id bigint;
    org_id uuid := '4b8f6427-8294-463f-835b-2ae006576c8f';
BEGIN
    -- Event 1: Con 1 ve
    INSERT INTO events (organizer_id, title, slug, category, venue, city, country, start_time, end_time, status, is_published, created_at, updated_at, featured, rating, review_count)
    VALUES (org_id, 'Flash Sale Event (1 Ticket Remaining)', 'flash-sale-' || extract(epoch from now()), 'Concert', 'Stadium', 'Hanoi', 'Vietnam', now() + interval '30 days', now() + interval '31 days', 'PUBLISHED', true, now(), now(), false, 0, 0)
    RETURNING id INTO event1_id;

    INSERT INTO seat_maps (event_id, name, total_rows, total_cols, created_at)
    VALUES (event1_id, 'Main Floor', 1, 10, now())
    RETURNING id INTO sm1_id;

    INSERT INTO ticket_tiers (seat_map_id, name, tier_type, price, quantity_total, quantity_available)
    VALUES (sm1_id, 'Last Chance', 'GENERAL', 500000, 2, 1);

    -- Event 2: Con 2 ve
    INSERT INTO events (organizer_id, title, slug, category, venue, city, country, start_time, end_time, status, is_published, created_at, updated_at, featured, rating, review_count)
    VALUES (org_id, 'Limited Edition Event (2 Tickets Remaining)', 'limited-edition-' || extract(epoch from now()), 'Festival', 'Park', 'Hanoi', 'Vietnam', now() + interval '40 days', now() + interval '41 days', 'PUBLISHED', true, now(), now(), false, 0, 0)
    RETURNING id INTO event2_id;

    INSERT INTO seat_maps (event_id, name, total_rows, total_cols, created_at)
    VALUES (event2_id, 'VIP Zone', 1, 5, now())
    RETURNING id INTO sm2_id;

    INSERT INTO ticket_tiers (seat_map_id, name, tier_type, price, quantity_total, quantity_available)
    VALUES (sm2_id, 'Duo', 'VIP', 1000000, 5, 2);
    
    RAISE NOTICE 'Created Test Event 1 with ID: %, Test Event 2 with ID: %', event1_id, event2_id;
END $$;
