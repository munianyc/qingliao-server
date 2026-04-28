package com.qingliao.server.controller;

import com.qingliao.server.dto.*;
import com.qingliao.server.entity.*;
import com.qingliao.server.security.SecurityUtil;
import com.qingliao.server.service.MomentService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@RestController
@RequestMapping("/api/moments")
public class MomentController {

    private final MomentService momentService;
    private final SecurityUtil securityUtil;

    public MomentController(MomentService momentService, SecurityUtil securityUtil) {
        this.momentService = momentService;
        this.securityUtil = securityUtil;
    }

    @PostMapping
    public ApiResponse<Moment> create(@RequestParam(value = "content", defaultValue = "") String content,
                                      @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        return ApiResponse.ok(momentService.createMoment(securityUtil.getCurrentUserId(), content, images));
    }

    @GetMapping("/timeline")
    public ApiResponse<List<Moment>> timeline() {
        return ApiResponse.ok(momentService.getFriendMoments(securityUtil.getCurrentUserId()));
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<Moment>> userMoments(@PathVariable Long userId) {
        return ApiResponse.ok(momentService.getUserMoments(userId));
    }

    @PostMapping("/{momentId}/like")
    public ApiResponse<Map<String, Object>> toggleLike(@PathVariable Long momentId) {
        boolean liked = momentService.toggleLike(momentId, securityUtil.getCurrentUserId());
        return ApiResponse.ok(Map.of("liked", liked));
    }

    @GetMapping("/{momentId}/likes")
    public ApiResponse<List<MomentLike>> getLikes(@PathVariable Long momentId) {
        return ApiResponse.ok(momentService.getLikes(momentId));
    }

    @PostMapping("/{momentId}/comment")
    public ApiResponse<MomentComment> addComment(@PathVariable Long momentId,
                                                  @RequestBody Map<String, String> body) {
        return ApiResponse.ok(momentService.addComment(momentId,
                securityUtil.getCurrentUserId(), body.get("content")));
    }

    @GetMapping("/{momentId}/comments")
    public ApiResponse<List<MomentComment>> getComments(@PathVariable Long momentId) {
        return ApiResponse.ok(momentService.getComments(momentId));
    }
}
