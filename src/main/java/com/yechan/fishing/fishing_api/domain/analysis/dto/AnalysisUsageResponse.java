package com.yechan.fishing.fishing_api.domain.analysis.dto;

import java.time.LocalDate;

public record AnalysisUsageResponse(
    LocalDate date, int dailyLimit, int usedCount, int remainingCount) {}
