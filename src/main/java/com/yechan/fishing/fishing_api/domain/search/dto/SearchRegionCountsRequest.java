package com.yechan.fishing.fishing_api.domain.search.dto;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record SearchRegionCountsRequest(
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate untilDate) {}
