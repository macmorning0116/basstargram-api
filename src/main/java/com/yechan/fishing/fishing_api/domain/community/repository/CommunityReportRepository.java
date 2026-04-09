package com.yechan.fishing.fishing_api.domain.community.repository;

import com.yechan.fishing.fishing_api.domain.community.entity.CommunityReport;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityReportRepository extends JpaRepository<CommunityReport, Long> {

  boolean existsByReporterUser_IdAndTargetTypeAndTargetId(
      Long reporterUserId, ReportTargetType targetType, Long targetId);

  long countDistinctReporterUser_IdByTargetTypeAndTargetId(
      ReportTargetType targetType, Long targetId);
}
