package com.yechan.fishing.fishing_api.domain.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCommunityPostRequest(@NotBlank @Size(max = 5000) String content) {}
