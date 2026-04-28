package com.qingliao.server.service;

import com.qingliao.server.entity.*;
import com.qingliao.server.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final FileStorageService fileStorage;
    private final FriendshipRepository friendshipRepo;
    private final SessionMemberRepository memberRepo;

    public UserService(UserRepository userRepo, FileStorageService fileStorage,
                       FriendshipRepository friendshipRepo, SessionMemberRepository memberRepo) {
        this.userRepo = userRepo;
        this.fileStorage = fileStorage;
        this.friendshipRepo = friendshipRepo;
        this.memberRepo = memberRepo;
    }

    @Transactional
    public void deleteAccount(Long userId) {
        friendshipRepo.deleteAllByUserId(userId);
        memberRepo.deleteByUserId(userId);
        userRepo.deleteById(userId);
    }

    public User getById(Long id) {
        return userRepo.findById(id).orElse(null);
    }

    public User updateProfile(Long userId, Map<String, String> fields) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return null;
        if (fields.containsKey("nickname")) user.setNickname(fields.get("nickname"));
        if (fields.containsKey("signature")) user.setSignature(fields.get("signature"));
        if (fields.containsKey("gender")) user.setGender(Integer.parseInt(fields.get("gender")));
        if (fields.containsKey("region")) user.setRegion(fields.get("region"));
        // QingLiao ID change — uniqueness + once-per-day limit
        if (fields.containsKey("qingliaoId")) {
            String newId = fields.get("qingliaoId");
            if (newId != null && !newId.isEmpty() && !newId.equals(user.getUsername())) {
                // Validate length
                if (newId.length() < 3) return null;
                // Check uniqueness
                if (userRepo.findByUsername(newId).isPresent()) return null;
                // Check once-per-day limit
                long now = System.currentTimeMillis();
                if (user.getQidModifiedAt() > 0 && (now - user.getQidModifiedAt()) < 86400000L) return null;
                // Allow change
                user.setUsername(newId);
                user.setQidModifiedAt(now);
            }
        }
        return userRepo.save(user);
    }

    public String uploadAvatar(Long userId, MultipartFile file) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return null;
        String url = fileStorage.storeAvatar(file);
        user.setAvatar(url);
        userRepo.save(user);
        return url;
    }

    public List<User> search(String keyword, Long currentUserId) {
        return userRepo.searchUsers(keyword, currentUserId);
    }

    public List<User> getFriends(Long userId) {
        return userRepo.findFriends(userId);
    }

    public boolean isUsernameTaken(String username) {
        return userRepo.findByUsername(username).isPresent();
    }

    public void updateOnlineStatus(Long userId, int status) {
        userRepo.findById(userId).ifPresent(user -> {
            user.setOnlineStatus(status);
            user.setLastOnline(System.currentTimeMillis());
            userRepo.save(user);
        });
    }
}
