package com.yechan.fishing.fishing_api.domain.community.entity;

import com.yechan.fishing.fishing_api.domain.auth.entity.User;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.ReportReasonType;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.ReportStatus;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.ReportTargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "community_reports",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_reports_reporter_target",
          columnNames = {"reporter_user_id", "target_type", "target_id"})
    })
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityReport {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "reporter_user_id", nullable = false)
  private User reporterUser;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false, length = 20)
  private ReportTargetType targetType;

  @Column(name = "target_id", nullable = false)
  private Long targetId;

  @Enumerated(EnumType.STRING)
  @Column(name = "reason_type", nullable = false, length = 20)
  private ReportReasonType reasonType;

  @Column(name = "reason_detail", columnDefinition = "TEXT")
  private String reasonDetail;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ReportStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "resolved_by")
  private User resolvedBy;

  @Column(name = "resolved_at")
  private LocalDateTime resolvedAt;

  @Column(name = "admin_memo", columnDefinition = "TEXT")
  private String adminMemo;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
