-- Feed query: WHERE visibility_status = 'VISIBLE' ORDER BY id DESC
CREATE INDEX idx_community_posts_visibility_id
    ON community_posts(visibility_status, id DESC);

-- Author feed query: WHERE user_id = ? AND visibility_status = 'VISIBLE' ORDER BY id DESC
CREATE INDEX idx_community_posts_user_visibility_id
    ON community_posts(user_id, visibility_status, id DESC);

-- Refresh token lookup
CREATE INDEX idx_user_refresh_tokens_token
    ON user_refresh_tokens(refresh_token) WHERE revoked_at IS NULL;
