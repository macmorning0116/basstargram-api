package com.yechan.fishing.fishing_api.domain.search.dto;

import java.util.List;

public record SearchPostItem(
    String articleId,
    String title,
    String url,
    String authorName,
    String publishedAt,
    String boardKey,
    String boardName,
    String species,
    String region,
    String place,
    String accessStatus,
    List<String> tags) {}
