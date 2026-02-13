CREATE TABLE post_media (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    post_id UUID NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    file_id VARCHAR(255) NOT NULL,
    url VARCHAR(1024) NOT NULL,
    original_filename VARCHAR(255),
    mime_type VARCHAR(127) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_post_media_post_id ON post_media(post_id);
