package com.yechan.fishing.fishing_api.domain.search.dto;

import java.util.List;

public record SearchPostsResponse(
        List<SearchPostItem> items,
        long total,
        int size,
        String nextCursor
) {
}
