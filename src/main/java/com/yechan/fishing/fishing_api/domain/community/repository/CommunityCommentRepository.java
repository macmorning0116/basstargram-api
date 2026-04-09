package com.yechan.fishing.fishing_api.domain.community.repository;

import com.yechan.fishing.fishing_api.domain.community.entity.CommunityComment;
import com.yechan.fishing.fishing_api.domain.community.entity.enums.VisibilityStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {

  List<CommunityComment> findAllByPost_IdAndVisibilityStatusOrderByCreatedAtAsc(
      Long postId, VisibilityStatus visibilityStatus);

  Optional<CommunityComment> findByIdAndPost_Id(Long commentId, Long postId);
}
