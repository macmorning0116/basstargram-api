package com.yechan.fishing.fishing_api.domain.community.repository;

import com.yechan.fishing.fishing_api.domain.community.entity.CommunityPost;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.VisibilityStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

  List<CommunityPost> findAllByVisibilityStatusOrderByIdDesc(
      VisibilityStatus visibilityStatus, Pageable pageable);

  List<CommunityPost> findAllByVisibilityStatusAndIdLessThanOrderByIdDesc(
      VisibilityStatus visibilityStatus, Long cursor, Pageable pageable);

  Optional<CommunityPost> findByIdAndVisibilityStatus(
      Long postId, VisibilityStatus visibilityStatus);
}
