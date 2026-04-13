package com.yechan.fishing.fishing_api.domain.community.dto;

import com.yechan.fishing.fishing_api.domain.community.entity.enums.FishedAtSource;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.LocationSource;
import java.time.LocalDateTime;

public record CommunityPostDefaultsResponse(
    LocalDateTime fishedAt,
    FishedAtSource fishedAtSource,
    Double latitude,
    Double longitude,
    LocationSource locationSource,
    String region,
    String placeName) {}
