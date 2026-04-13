package com.yechan.fishing.fishing_api.domain.community.dto;

import java.util.List;

public record CommunityPostsResponse(
    List<CommunityPostSummaryItem> items, int size, Long nextCursor) {}
