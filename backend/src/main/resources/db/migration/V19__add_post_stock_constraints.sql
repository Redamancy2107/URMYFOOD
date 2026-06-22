DO $$ BEGIN
    ALTER TABLE posts
        ADD CONSTRAINT chk_posts_max_quantity_non_negative
        CHECK (max_quantity >= 0);
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    ALTER TABLE posts
        ADD CONSTRAINT chk_posts_remaining_quantity_non_negative
        CHECK (remaining_quantity >= 0);
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    ALTER TABLE posts
        ADD CONSTRAINT chk_posts_remaining_quantity_not_greater_than_max
        CHECK (remaining_quantity <= max_quantity);
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;
