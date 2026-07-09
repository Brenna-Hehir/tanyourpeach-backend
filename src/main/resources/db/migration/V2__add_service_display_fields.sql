ALTER TABLE services
ADD COLUMN slug VARCHAR(150),
ADD COLUMN short_description VARCHAR(255),
ADD COLUMN service_type ENUM('MAIN_SERVICE', 'ADD_ON') NOT NULL DEFAULT 'MAIN_SERVICE',
ADD COLUMN card_image_url VARCHAR(500),
ADD COLUMN hero_image_url VARCHAR(500),
ADD COLUMN display_order INT NOT NULL DEFAULT 0,
ADD COLUMN rinse_time_min_hours DECIMAL(4,2),
ADD COLUMN rinse_time_max_hours DECIMAL(4,2);

ALTER TABLE services
ADD UNIQUE KEY uk_services_slug (slug),
ADD CONSTRAINT chk_services_slug_null_or_not_blank
    CHECK (
        slug IS NULL
        OR CHAR_LENGTH(TRIM(slug)) > 0
    ),
ADD CONSTRAINT chk_services_short_description_null_or_not_blank
    CHECK (
        short_description IS NULL
        OR CHAR_LENGTH(TRIM(short_description)) > 0
    ),
ADD CONSTRAINT chk_services_card_image_url_null_or_not_blank
    CHECK (
        card_image_url IS NULL
        OR CHAR_LENGTH(TRIM(card_image_url)) > 0
    ),
ADD CONSTRAINT chk_services_hero_image_url_null_or_not_blank
    CHECK (
        hero_image_url IS NULL
        OR CHAR_LENGTH(TRIM(hero_image_url)) > 0
    ),
ADD CONSTRAINT chk_services_display_order_nonnegative
    CHECK (display_order >= 0),
ADD CONSTRAINT chk_services_price_duration_by_type
    CHECK (
        (
            service_type = 'MAIN_SERVICE'
            AND base_price > 0
            AND duration_minutes > 0
        )
        OR
        (
            service_type = 'ADD_ON'
            AND base_price >= 0
            AND duration_minutes >= 0
        )
    ),
ADD CONSTRAINT chk_services_rinse_time_pair_and_order
    CHECK (
        (
            rinse_time_min_hours IS NULL
            AND rinse_time_max_hours IS NULL
        )
        OR
        (
            rinse_time_min_hours IS NOT NULL
            AND rinse_time_max_hours IS NOT NULL
            AND rinse_time_min_hours > 0
            AND rinse_time_max_hours >= rinse_time_min_hours
        )
    );