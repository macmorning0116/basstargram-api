package com.yechan.fishing.fishing_api.domain.search.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SearchPostsRequest(
        String q,
        String boardKey,
        String cursor,
        @Min(1) @Max(100) Integer size
) {
    public int safeSize() {
        return size == null ? 20 : size;
    }
}
