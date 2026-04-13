CREATE EXTENSION IF NOT EXISTS postgis;

ALTER TABLE community_posts
    ADD COLUMN location GEOGRAPHY(Point, 4326);

UPDATE community_posts
SET location = ST_SetSRID(ST_MakePoint(longitude, latitude), 4326)::geography
WHERE latitude IS NOT NULL
  AND longitude IS NOT NULL;

DROP INDEX IF EXISTS idx_community_posts_map;

ALTER TABLE community_posts
    DROP COLUMN latitude,
    DROP COLUMN longitude;

CREATE INDEX idx_community_posts_location ON community_posts USING GIST (location);
