package com.qingliao.server.repository;

import com.qingliao.server.entity.MomentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MomentLikeRepository extends JpaRepository<MomentLike, Long> {
    List<MomentLike> findByMomentId(Long momentId);
    Optional<MomentLike> findByMomentIdAndUserId(Long momentId, Long userId);
    long countByMomentId(Long momentId);
    void deleteByMomentIdAndUserId(Long momentId, Long userId);
}
