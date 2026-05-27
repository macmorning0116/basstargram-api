package com.yechan.fishing.fishing_api.domain.analysis.service;

import com.yechan.fishing.fishing_api.domain.analysis.dto.AnalysisUsageResponse;
import com.yechan.fishing.fishing_api.domain.analysis.entity.AnalysisDailyUsage;
import com.yechan.fishing.fishing_api.domain.analysis.repository.AnalysisDailyUsageRepository;
import com.yechan.fishing.fishing_api.domain.auth.entity.User;
import com.yechan.fishing.fishing_api.domain.auth.repository.UserRepository;
import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalysisUsageService {

  public static final int DAILY_LIMIT = 5;
  private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

  private final AnalysisDailyUsageRepository usageRepository;
  private final UserRepository userRepository;
  private final Clock clock;

  public AnalysisUsageService(
      AnalysisDailyUsageRepository usageRepository, UserRepository userRepository, Clock clock) {
    this.usageRepository = usageRepository;
    this.userRepository = userRepository;
    this.clock = clock;
  }

  @Transactional
  public void consumeDailyUsage(Long userId) {
    LocalDate usageDate = LocalDate.now(clock.withZone(SERVICE_ZONE));
    LocalDateTime now = LocalDateTime.now(clock.withZone(SERVICE_ZONE));

    usageRepository
        .findByUser_IdAndUsageDate(userId, usageDate)
        .ifPresentOrElse(
            usage -> incrementUsage(usage, now), () -> createFirstUsage(userId, usageDate, now));
  }

  @Transactional(readOnly = true)
  public AnalysisUsageResponse getDailyUsage(Long userId) {
    LocalDate usageDate = LocalDate.now(clock.withZone(SERVICE_ZONE));
    int usedCount =
        usageRepository.findRequestCountByUserIdAndUsageDate(userId, usageDate).orElse(0);
    int remainingCount = Math.max(0, DAILY_LIMIT - usedCount);
    return new AnalysisUsageResponse(usageDate, DAILY_LIMIT, usedCount, remainingCount);
  }

  private void incrementUsage(AnalysisDailyUsage usage, LocalDateTime now) {
    if (usage.getRequestCount() >= DAILY_LIMIT) {
      throw new FishingException(ErrorCode.ANALYSIS_DAILY_LIMIT_EXCEEDED);
    }
    usage.increment(now);
  }

  private void createFirstUsage(Long userId, LocalDate usageDate, LocalDateTime now) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new FishingException(ErrorCode.USER_NOT_FOUND));
    usageRepository.save(AnalysisDailyUsage.firstUsage(user, usageDate, now));
  }
}
