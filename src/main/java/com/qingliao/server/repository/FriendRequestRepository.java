package com.qingliao.server.repository;

import com.qingliao.server.entity.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    List<FriendRequest> findByReceiverIdAndStatusOrderByCreatedAtDesc(Long receiverId, int status);

    Optional<FriendRequest> findBySenderIdAndReceiverIdAndStatus(Long senderId, Long receiverId, int status);

    long countByReceiverIdAndStatus(Long receiverId, int status);
}
