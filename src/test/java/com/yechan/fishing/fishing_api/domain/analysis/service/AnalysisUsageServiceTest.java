package com.yechan.fishing.fishing_api.domain.analysis.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.yechan.fishing.fishing_api.domain.analysis.entity.AnalysisDailyUsage;
import com.yechan.fishing.fishing_api.domain.analysis.repository.AnalysisDailyUsageRepository;
import com.yechan.fishing.fishing_api.domain.auth.entity.User;
import com.yechan.fishing.fishing_api.domain.auth.entity.enums.AuthProvider;
import com.yechan.fishing.fishing_api.domain.auth.repository.UserRepository;
import com.yechan.fishing.fishing_api.global.exception.ErrorCode;
import com.yechan.fishing.fishing_api.global.exception.FishingException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AnalysisUsageServiceTest {

  @Mock private AnalysisDailyUsageRepository usageRepository;

  @Mock private UserRepository userRepository;

  private AnalysisUsageService service;

  @BeforeEach
  void setUp() {
    Clock clock = Clock.fixed(Instant.parse("2026-05-27T03:00:00Z"), ZoneId.of("UTC"));
    service = new AnalysisUsageService(usageRepository, userRepository, clock);
  }

  @Test
  void consumeDailyUsage_whenFirstUsage_createsUsageRow() {
    User user =
        User.create(
            AuthProvider.KAKAO, "123456", "angler@example.com", "앵글러", null, LocalDateTime.now());
    ReflectionTestUtils.setField(user, "id", 1L);

    given(usageRepository.findByUser_IdAndUsageDate(1L, LocalDate.of(2026, 5, 27)))
        .willReturn(Optional.empty());
    given(userRepository.findById(1L)).willReturn(Optional.of(user));

    service.consumeDailyUsage(1L);

    then(usageRepository).should().save(any(AnalysisDailyUsage.class));
  }

  @Test
  void consumeDailyUsage_whenUnderLimit_incrementsUsage() {
    User user =
        User.create(
            AuthProvider.KAKAO, "123456", "angler@example.com", "앵글러", null, LocalDateTime.now());
    AnalysisDailyUsage usage =
        AnalysisDailyUsage.firstUsage(
            user, LocalDate.of(2026, 5, 27), LocalDateTime.of(2026, 5, 27, 12, 0));

    given(usageRepository.findByUser_IdAndUsageDate(1L, LocalDate.of(2026, 5, 27)))
        .willReturn(Optional.of(usage));

    service.consumeDailyUsage(1L);

    assertEquals(2, usage.getRequestCount());
  }

  @Test
  void consumeDailyUsage_whenLimitReached_throwsLimitExceeded() {
    User user =
        User.create(
            AuthProvider.KAKAO, "123456", "angler@example.com", "앵글러", null, LocalDateTime.now());
    AnalysisDailyUsage usage =
        AnalysisDailyUsage.firstUsage(
            user, LocalDate.of(2026, 5, 27), LocalDateTime.of(2026, 5, 27, 12, 0));
    for (int i = 1; i < AnalysisUsageService.DAILY_LIMIT; i++) {
      usage.increment(LocalDateTime.of(2026, 5, 27, 12, i));
    }

    given(usageRepository.findByUser_IdAndUsageDate(1L, LocalDate.of(2026, 5, 27)))
        .willReturn(Optional.of(usage));

    FishingException ex = assertThrows(FishingException.class, () -> service.consumeDailyUsage(1L));

    assertEquals(ErrorCode.ANALYSIS_DAILY_LIMIT_EXCEEDED, ex.getErrorCode());
  }

  @Test
  void getDailyUsage_whenNoUsage_returnsFullRemainingCount() {
    given(usageRepository.findRequestCountByUserIdAndUsageDate(1L, LocalDate.of(2026, 5, 27)))
        .willReturn(Optional.empty());

    var result = service.getDailyUsage(1L);

    assertEquals(LocalDate.of(2026, 5, 27), result.date());
    assertEquals(AnalysisUsageService.DAILY_LIMIT, result.dailyLimit());
    assertEquals(0, result.usedCount());
    assertEquals(5, result.remainingCount());
  }

  @Test
  void getDailyUsage_whenUsageExists_returnsRemainingCount() {
    given(usageRepository.findRequestCountByUserIdAndUsageDate(1L, LocalDate.of(2026, 5, 27)))
        .willReturn(Optional.of(2));

    var result = service.getDailyUsage(1L);

    assertEquals(2, result.usedCount());
    assertEquals(3, result.remainingCount());
  }
}
