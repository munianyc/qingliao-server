package com.qingliao.server.controller;

import com.qingliao.server.dto.*;
import com.qingliao.server.entity.*;
import com.qingliao.server.repository.SessionMemberRepository;
import com.qingliao.server.security.SecurityUtil;
import com.qingliao.server.service.SessionService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

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

            SessionMember member = memberRepo.findBySessionIdAndUserId(s.getId(), userId).orElse(null);
            map.put("unreadCount", member != null ? member.getUnreadCount() : 0);

            if (s.getType() == 0) {
                List<com.qingliao.server.entity.User> members = sessionService.getMembers(s.getId());
                for (com.qingliao.server.entity.User m : members) {
                    if (!m.getId().equals(userId)) {
                        map.put("otherUserId", m.getId());
                        map.put("otherUserName", m.getNickname());
                        map.put("avatar", m.getAvatar() != null ? m.getAvatar() : "");
                        map.put("onlineStatus", m.getOnlineStatus());
                        map.put("lastOnline", m.getLastOnline());
                        break;
                    }
                }
            } else {
                // Group: include member count
                map.put("memberCount", memberRepo.findBySessionId(s.getId()).size());
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

    @PutMapping("/{sessionId}")
    public ApiResponse<?> updateGroup(@PathVariable Long sessionId, @RequestBody Map<String, String> body) {
        ChatSession session = sessionService.updateGroup(sessionId,
                body.get("name"), body.get("avatar"));
        if (session == null) return ApiResponse.error(404, "群聊不存在");
        return ApiResponse.ok(session);
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
