package com.qingliao.server.controller;

import com.qingliao.server.dto.ApiResponse;
import com.qingliao.server.entity.FriendRequest;
import com.qingliao.server.security.SecurityUtil;
import com.qingliao.server.service.FriendService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;
    private final SecurityUtil securityUtil;

    public FriendController(FriendService friendService, SecurityUtil securityUtil) {
        this.friendService = friendService;
        this.securityUtil = securityUtil;
    }

    @PostMapping("/request")
    public ApiResponse<?> sendRequest(@RequestBody Map<String, Object> body) {
        Long receiverId = ((Number) body.get("receiverId")).longValue();
        String message = (String) body.getOrDefault("message", "");
        String error = friendService.sendRequest(securityUtil.getCurrentUserId(), receiverId, message);
        if (error != null) return ApiResponse.error(400, error);
        return ApiResponse.ok();
    }

    @GetMapping("/requests")
    public ApiResponse<List<FriendRequest>> getRequests() {
        return ApiResponse.ok(friendService.getPendingRequests(securityUtil.getCurrentUserId()));
    }

    @GetMapping("/requests/count")
    public ApiResponse<Map<String, Long>> getRequestCount() {
        return ApiResponse.ok(Map.of("count", friendService.getPendingCount(securityUtil.getCurrentUserId())));
    }

    @PostMapping("/accept/{requestId}")
    public ApiResponse<?> accept(@PathVariable Long requestId) {
        friendService.acceptRequest(requestId, securityUtil.getCurrentUserId());
        return ApiResponse.ok();
    }

    @PostMapping("/reject/{requestId}")
    public ApiResponse<?> reject(@PathVariable Long requestId) {
        friendService.rejectRequest(requestId, securityUtil.getCurrentUserId());
        return ApiResponse.ok();
    }

    @DeleteMapping("/{friendId}")
    public ApiResponse<?> deleteFriend(@PathVariable Long friendId) {
        friendService.deleteFriend(securityUtil.getCurrentUserId(), friendId);
        return ApiResponse.ok();
    }
}
