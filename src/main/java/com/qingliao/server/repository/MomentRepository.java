package com.qingliao.server.repository;

import com.qingliao.server.entity.Moment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface MomentRepository extends JpaRepository<Moment, Long> {

    @Query("SELECT m FROM Moment m WHERE m.userId IN :friendIds ORDER BY m.createdAt DESC")
    List<Moment> findFriendsMoments(List<Long> friendIds);

    List<Moment> findByUserIdOrderByCreatedAtDesc(Long userId);
}
