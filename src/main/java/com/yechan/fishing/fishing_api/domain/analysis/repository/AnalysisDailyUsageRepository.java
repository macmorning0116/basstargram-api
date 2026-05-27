package com.yechan.fishing.fishing_api.domain.analysis.repository;

import com.yechan.fishing.fishing_api.domain.analysis.entity.AnalysisDailyUsage;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnalysisDailyUsageRepository extends JpaRepository<AnalysisDailyUsage, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<AnalysisDailyUsage> findByUser_IdAndUsageDate(Long userId, LocalDate usageDate);

  @Query(
      """
      select u.requestCount
      from AnalysisDailyUsage u
      where u.user.id = :userId and u.usageDate = :usageDate
      """)
  Optional<Integer> findRequestCountByUserIdAndUsageDate(
      @Param("userId") Long userId, @Param("usageDate") LocalDate usageDate);
}
