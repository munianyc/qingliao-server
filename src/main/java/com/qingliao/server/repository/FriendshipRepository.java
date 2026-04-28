package com.qingliao.server.repository;

import com.qingliao.server.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("SELECT COUNT(f) > 0 FROM Friendship f WHERE (f.userId = :u1 AND f.friendId = :u2) OR (f.userId = :u2 AND f.friendId = :u1)")
    boolean areFriends(Long u1, Long u2);

    @Query("SELECT f FROM Friendship f WHERE f.userId = :userId OR f.friendId = :userId")
    List<Friendship> findByUserId(Long userId);

    void deleteByUserIdAndFriendId(Long userId, Long friendId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Friendship f WHERE f.userId = :userId OR f.friendId = :userId")
    void deleteAllByUserId(Long userId);
}
