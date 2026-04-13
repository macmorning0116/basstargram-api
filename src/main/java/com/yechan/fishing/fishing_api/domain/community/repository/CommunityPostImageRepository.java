package com.yechan.fishing.fishing_api.domain.community.repository;

import com.yechan.fishing.fishing_api.domain.community.entity.CommunityPostImage;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostImageRepository extends JpaRepository<CommunityPostImage, Long> {

  List<CommunityPostImage> findAllByPost_IdInOrderByPost_IdAscSortOrderAsc(
      Collection<Long> postIds);

  List<CommunityPostImage> findAllByPost_IdOrderBySortOrderAsc(Long postId);

  void deleteAllByPost_IdAndIdIn(Long postId, Collection<Long> ids);
}
