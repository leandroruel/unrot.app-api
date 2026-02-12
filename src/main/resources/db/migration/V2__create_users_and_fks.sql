CREATE TABLE users (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email        VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_users_email ON users (email);

ALTER TABLE posts
    ADD CONSTRAINT fk_posts_author_id
    FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE RESTRICT;

ALTER TABLE post_comments
    ADD CONSTRAINT fk_post_comments_user_id
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE post_likes
    ADD CONSTRAINT fk_post_likes_user_id
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE post_bookmarks
    ADD CONSTRAINT fk_post_bookmarks_user_id
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
