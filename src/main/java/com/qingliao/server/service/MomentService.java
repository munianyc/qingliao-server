package com.qingliao.server.service;

import com.qingliao.server.entity.*;
import com.qingliao.server.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MomentService {

    private final MomentRepository momentRepo;
    private final MomentLikeRepository likeRepo;
    private final MomentCommentRepository commentRepo;
    private final FriendshipRepository friendshipRepo;
    private final UserRepository userRepo;
    private final FileStorageService fileStorage;

    public MomentService(MomentRepository momentRepo, MomentLikeRepository likeRepo,
                         MomentCommentRepository commentRepo, FriendshipRepository friendshipRepo,
                         UserRepository userRepo, FileStorageService fileStorage) {
        this.momentRepo = momentRepo;
        this.likeRepo = likeRepo;
        this.commentRepo = commentRepo;
        this.friendshipRepo = friendshipRepo;
        this.userRepo = userRepo;
        this.fileStorage = fileStorage;
    }

    public Moment createMoment(Long userId, String content, List<MultipartFile> images) {
        Moment moment = new Moment();
        moment.setUserId(userId);
        moment.setContent(content != null ? content : "");
        if (images != null && !images.isEmpty()) {
            String urls = images.stream()
                    .map(fileStorage::store)
                    .collect(Collectors.joining(","));
            moment.setImages(urls);
        }
        return momentRepo.save(moment);
    }

    public List<Moment> getFriendMoments(Long userId) {
        List<Friendship> friendships = friendshipRepo.findByUserId(userId);
        List<Long> friendIds = friendships.stream()
                .map(f -> f.getUserId().equals(userId) ? f.getFriendId() : f.getUserId())
                .collect(Collectors.toList());
        friendIds.add(userId);
        return momentRepo.findFriendsMoments(friendIds);
    }

    public List<Moment> getUserMoments(Long userId) {
        return momentRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public boolean toggleLike(Long momentId, Long userId) {
        Optional<MomentLike> existing = likeRepo.findByMomentIdAndUserId(momentId, userId);
        if (existing.isPresent()) {
            likeRepo.delete(existing.get());
            return false;
        } else {
            MomentLike like = new MomentLike();
            like.setMomentId(momentId);
            like.setUserId(userId);
            likeRepo.save(like);
            return true;
        }
    }

    public List<MomentLike> getLikes(Long momentId) {
        return likeRepo.findByMomentId(momentId);
    }

    public MomentComment addComment(Long momentId, Long userId, String content) {
        MomentComment comment = new MomentComment();
        comment.setMomentId(momentId);
        comment.setUserId(userId);
        comment.setContent(content);
        return commentRepo.save(comment);
    }

    public List<MomentComment> getComments(Long momentId) {
        return commentRepo.findByMomentIdOrderByCreatedAtAsc(momentId);
    }
}
