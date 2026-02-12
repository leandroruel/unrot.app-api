CREATE TABLE categories (
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL,
    slug VARCHAR(50) NOT NULL
);

CREATE UNIQUE INDEX idx_categories_slug ON categories (slug);

INSERT INTO categories (name, slug) VALUES
    ('Science', 'science'),
    ('Technology', 'technology'),
    ('AI', 'ai'),
    ('Nature', 'nature');

ALTER TABLE posts ADD COLUMN category_id UUID REFERENCES categories (id);

CREATE INDEX idx_posts_category_id ON posts (category_id);
