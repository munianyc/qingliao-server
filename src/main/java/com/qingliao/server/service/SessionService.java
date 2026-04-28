package com.qingliao.server.service;

import com.qingliao.server.entity.*;
import com.qingliao.server.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class SessionService {

    private final ChatSessionRepository sessionRepo;
    private final SessionMemberRepository memberRepo;
    private final UserRepository userRepo;

    public SessionService(ChatSessionRepository sessionRepo, SessionMemberRepository memberRepo,
                          UserRepository userRepo) {
        this.sessionRepo = sessionRepo;
        this.memberRepo = memberRepo;
        this.userRepo = userRepo;
    }

    public List<ChatSession> getSessions(Long userId) {
        return sessionRepo.findSessionsByUserId(userId);
    }

    @Transactional
    public ChatSession getOrCreateSingleSession(Long userId1, Long userId2) {
        // Find existing single session
        List<ChatSession> sessions = sessionRepo.findSessionsByUserId(userId1);
        for (ChatSession s : sessions) {
            if (s.getType() == 0) {
                List<SessionMember> members = memberRepo.findBySessionId(s.getId());
                boolean hasBoth = members.stream().anyMatch(m -> m.getUserId().equals(userId1))
                        && members.stream().anyMatch(m -> m.getUserId().equals(userId2));
                if (hasBoth) return s;
            }
        }

        ChatSession session = new ChatSession();
        session.setType(0);
        sessionRepo.save(session);

        SessionMember m1 = new SessionMember(); m1.setSessionId(session.getId()); m1.setUserId(userId1);
        SessionMember m2 = new SessionMember(); m2.setSessionId(session.getId()); m2.setUserId(userId2);
        memberRepo.save(m1);
        memberRepo.save(m2);
        return session;
    }

    @Transactional
    public ChatSession createGroup(String name, List<Long> memberIds, Long creatorId) {
        ChatSession session = new ChatSession();
        session.setType(1);
        session.setName(name);
        sessionRepo.save(session);

        Set<Long> ids = new LinkedHashSet<>(memberIds);
        ids.add(creatorId);
        for (Long uid : ids) {
            SessionMember sm = new SessionMember();
            sm.setSessionId(session.getId());
            sm.setUserId(uid);
            memberRepo.save(sm);
        }
        return session;
    }

    public List<User> getMembers(Long sessionId) {
        List<SessionMember> members = memberRepo.findBySessionId(sessionId);
        List<Long> ids = members.stream().map(SessionMember::getUserId).toList();
        return userRepo.findByIds(ids);
    }

    @Transactional
    public void addMember(Long sessionId, Long userId) {
        if (memberRepo.findBySessionIdAndUserId(sessionId, userId).isEmpty()) {
            SessionMember sm = new SessionMember();
            sm.setSessionId(sessionId);
            sm.setUserId(userId);
            memberRepo.save(sm);
        }
    }

    @Transactional
    public void removeMember(Long sessionId, Long userId) {
        memberRepo.findBySessionIdAndUserId(sessionId, userId)
                .ifPresent(memberRepo::delete);
    }

    @Transactional
    public void deleteSession(Long sessionId, Long userId) {
        // Only allow the member to delete the session for themselves
        memberRepo.findBySessionIdAndUserId(sessionId, userId)
                .ifPresent(memberRepo::delete);
        // If no members left, clean up the session
        if (memberRepo.findBySessionId(sessionId).isEmpty()) {
            sessionRepo.deleteById(sessionId);
        }
    }
}
