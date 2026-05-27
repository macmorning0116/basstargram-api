package com.yechan.fishing.fishing_api.domain.analysis.entity;

import com.yechan.fishing.fishing_api.domain.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "analysis_daily_usages",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_analysis_daily_usages_user_date",
          columnNames = {"user_id", "usage_date"})
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisDailyUsage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "usage_date", nullable = false)
  private LocalDate usageDate;

  @Column(name = "request_count", nullable = false)
  private int requestCount;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  public static AnalysisDailyUsage firstUsage(User user, LocalDate usageDate, LocalDateTime now) {
    AnalysisDailyUsage usage = new AnalysisDailyUsage();
    usage.user = user;
    usage.usageDate = usageDate;
    usage.requestCount = 1;
    usage.createdAt = now;
    usage.updatedAt = now;
    return usage;
  }

  public void increment(LocalDateTime now) {
    requestCount++;
    updatedAt = now;
  }
}
