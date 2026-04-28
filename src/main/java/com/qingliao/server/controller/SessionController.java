package com.qingliao.server.controller;

import com.qingliao.server.dto.*;
import com.qingliao.server.entity.*;
import com.qingliao.server.repository.SessionMemberRepository;
import com.qingliao.server.security.SecurityUtil;
import com.qingliao.server.service.SessionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final SecurityUtil securityUtil;
    private final SessionMemberRepository memberRepo;

    public SessionController(SessionService sessionService, SecurityUtil securityUtil,
                             SessionMemberRepository memberRepo) {
        this.sessionService = sessionService;
        this.securityUtil = securityUtil;
        this.memberRepo = memberRepo;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getSessions() {
        Long userId = securityUtil.getCurrentUserId();
        List<ChatSession> sessions = sessionService.getSessions(userId);
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (ChatSession s : sessions) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", s.getId());
            map.put("type", s.getType());
            map.put("name", s.getName());
            map.put("avatar", s.getAvatar());
            map.put("lastMessage", s.getLastMessage());
            map.put("lastMessageTime", s.getLastMessageTime());
            map.put("createdAt", s.getCreatedAt());

            // Add unread count for the current user
            SessionMember member = memberRepo.findBySessionIdAndUserId(s.getId(), userId).orElse(null);
            map.put("unreadCount", member != null ? member.getUnreadCount() : 0);

            // For single sessions, include the other user's info and avatar
            if (s.getType() == 0) {
                List<com.qingliao.server.entity.User> members = sessionService.getMembers(s.getId());
                for (com.qingliao.server.entity.User m : members) {
                    if (!m.getId().equals(userId)) {
                        map.put("otherUserId", m.getId());
                        map.put("otherUserName", m.getNickname());
                        map.put("avatar", m.getAvatar() != null ? m.getAvatar() : "");
                        break;
                    }
                }
            }
            result.add(map);
        }
        return ApiResponse.ok(result);
    }

    @DeleteMapping("/{sessionId}")
    public ApiResponse<?> deleteSession(@PathVariable Long sessionId) {
        sessionService.deleteSession(sessionId, securityUtil.getCurrentUserId());
        return ApiResponse.ok();
    }

    @PostMapping("/single/{otherUserId}")
    public ApiResponse<ChatSession> getOrCreateSingle(@PathVariable Long otherUserId) {
        return ApiResponse.ok(sessionService.getOrCreateSingleSession(
                securityUtil.getCurrentUserId(), otherUserId));
    }

    @PostMapping("/group")
    public ApiResponse<ChatSession> createGroup(@RequestBody CreateGroupRequest req) {
        return ApiResponse.ok(sessionService.createGroup(
                req.getName(), req.getMemberIds(), securityUtil.getCurrentUserId()));
    }

    @GetMapping("/{sessionId}/members")
    public ApiResponse<List<User>> getMembers(@PathVariable Long sessionId) {
        return ApiResponse.ok(sessionService.getMembers(sessionId));
    }

    @PostMapping("/{sessionId}/members/{userId}")
    public ApiResponse<?> addMember(@PathVariable Long sessionId, @PathVariable Long userId) {
        sessionService.addMember(sessionId, userId);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{sessionId}/members/{userId}")
    public ApiResponse<?> removeMember(@PathVariable Long sessionId, @PathVariable Long userId) {
        sessionService.removeMember(sessionId, userId);
        return ApiResponse.ok();
    }
}
