package com.qingliao.server.repository;

import com.qingliao.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.id != :currentUserId AND (u.nickname LIKE %:keyword% OR u.username LIKE %:keyword%)")
    List<User> searchUsers(String keyword, Long currentUserId);

    @Query("SELECT u FROM User u INNER JOIN Friendship f ON (f.friendId = u.id AND f.userId = :userId) OR (f.userId = u.id AND f.friendId = :userId) WHERE u.id != :userId ORDER BY u.nickname ASC")
    List<User> findFriends(Long userId);

    @Query("SELECT u FROM User u WHERE u.id IN :ids")
    List<User> findByIds(List<Long> ids);
}
