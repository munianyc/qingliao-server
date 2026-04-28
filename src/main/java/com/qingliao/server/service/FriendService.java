package com.qingliao.server.service;

import com.qingliao.server.entity.*;
import com.qingliao.server.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class FriendService {

    private final FriendRequestRepository requestRepo;
    private final FriendshipRepository friendshipRepo;
    private final UserRepository userRepo;
    private final ChatSessionRepository sessionRepo;
    private final SessionMemberRepository memberRepo;

    public FriendService(FriendRequestRepository requestRepo, FriendshipRepository friendshipRepo,
                         UserRepository userRepo, ChatSessionRepository sessionRepo,
                         SessionMemberRepository memberRepo) {
        this.requestRepo = requestRepo;
        this.friendshipRepo = friendshipRepo;
        this.userRepo = userRepo;
        this.sessionRepo = sessionRepo;
        this.memberRepo = memberRepo;
    }

    public String sendRequest(Long senderId, Long receiverId, String message) {
        if (senderId.equals(receiverId)) return "不能添加自己为好友";
        if (friendshipRepo.areFriends(senderId, receiverId)) return "已经是好友了";
        if (requestRepo.findBySenderIdAndReceiverIdAndStatus(senderId, receiverId, 0).isPresent())
            return "已发送过申请";
        if (requestRepo.findBySenderIdAndReceiverIdAndStatus(receiverId, senderId, 0).isPresent())
            return "对方已向你发送过申请";

        FriendRequest req = new FriendRequest();
        req.setSenderId(senderId);
        req.setReceiverId(receiverId);
        req.setMessage(message != null ? message : "");
        requestRepo.save(req);
        return null;
    }

    @Transactional
    public void acceptRequest(Long requestId, Long currentUserId) {
        FriendRequest req = requestRepo.findById(requestId).orElse(null);
        if (req == null || !req.getReceiverId().equals(currentUserId)) return;

        req.setStatus(1);
        requestRepo.save(req);

        Friendship fs = new Friendship();
        fs.setUserId(req.getSenderId());
        fs.setFriendId(req.getReceiverId());
        friendshipRepo.save(fs);

        // Create chat session
        ChatSession session = new ChatSession();
        session.setType(0);
        sessionRepo.save(session);

        memberRepo.save(createMember(session.getId(), req.getSenderId()));
        memberRepo.save(createMember(session.getId(), req.getReceiverId()));
    }

    @Transactional
    public void rejectRequest(Long requestId, Long currentUserId) {
        FriendRequest req = requestRepo.findById(requestId).orElse(null);
        if (req == null || !req.getReceiverId().equals(currentUserId)) return;
        req.setStatus(2);
        requestRepo.save(req);
    }

    public List<FriendRequest> getPendingRequests(Long userId) {
        return requestRepo.findByReceiverIdAndStatusOrderByCreatedAtDesc(userId, 0);
    }

    public long getPendingCount(Long userId) {
        return requestRepo.countByReceiverIdAndStatus(userId, 0);
    }

    public void deleteFriend(Long userId, Long friendId) {
        friendshipRepo.deleteByUserIdAndFriendId(userId, friendId);
    }

    private SessionMember createMember(Long sessionId, Long userId) {
        SessionMember sm = new SessionMember();
        sm.setSessionId(sessionId);
        sm.setUserId(userId);
        return sm;
    }
}
