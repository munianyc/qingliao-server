package com.qingliao.server.controller;

import com.qingliao.server.dto.ApiResponse;
import com.qingliao.server.entity.User;
import com.qingliao.server.security.SecurityUtil;
import com.qingliao.server.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final SecurityUtil securityUtil;

    public UserController(UserService userService, SecurityUtil securityUtil) {
        this.userService = userService;
        this.securityUtil = securityUtil;
    }

    @GetMapping("/me")
    public ApiResponse<User> me() {
        return ApiResponse.ok(userService.getById(securityUtil.getCurrentUserId()));
    }

    @PutMapping("/me")
    public ApiResponse<?> updateProfile(@RequestBody Map<String, String> fields) {
        User user = userService.updateProfile(securityUtil.getCurrentUserId(), fields);
        if (user == null) {
            // Check which validation failed
            if (fields.containsKey("qingliaoId")) {
                String newId = fields.get("qingliaoId");
                if (newId != null && !newId.isEmpty()) {
                    User current = userService.getById(securityUtil.getCurrentUserId());
                    if (current != null && !newId.equals(current.getUsername())) {
                        if (newId.length() < 3) return ApiResponse.error(400, "轻聊号至少3个字符");
                        long now = System.currentTimeMillis();
                        if (current.getQidModifiedAt() > 0 && (now - current.getQidModifiedAt()) < 86400000L)
                            return ApiResponse.error(400, "轻聊号每天只能修改一次");
                        return ApiResponse.error(400, "轻聊号已被占用");
                    }
                }
            }
            return ApiResponse.error(404, "用户不存在");
        }
        return ApiResponse.ok(user);
    }

    @GetMapping("/check-qid")
    public ApiResponse<?> checkQingliaoId(@RequestParam String qid) {
        if (qid.length() < 3) return ApiResponse.error(400, "轻聊号至少3个字符");
        boolean exists = userService.isUsernameTaken(qid);
        return ApiResponse.ok(Map.of("available", !exists));
    }

    @PostMapping("/avatar")
    public ApiResponse<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String url = userService.uploadAvatar(securityUtil.getCurrentUserId(), file);
        if (url == null) return ApiResponse.error(404, "用户不存在");
        return ApiResponse.ok(Map.of("url", url));
    }

    @GetMapping("/search")
    public ApiResponse<List<User>> search(@RequestParam String keyword) {
        return ApiResponse.ok(userService.search(keyword, securityUtil.getCurrentUserId()));
    }

    @GetMapping("/{id}")
    public ApiResponse<User> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        return user != null ? ApiResponse.ok(user) : ApiResponse.error(404, "用户不存在");
    }

    @GetMapping("/friends")
    public ApiResponse<List<User>> friends() {
        return ApiResponse.ok(userService.getFriends(securityUtil.getCurrentUserId()));
    }

    @PutMapping("/online")
    public ApiResponse<?> updateOnline(@RequestParam int status) {
        userService.updateOnlineStatus(securityUtil.getCurrentUserId(), status);
        return ApiResponse.ok();
    }

    @DeleteMapping("/me")
    public ApiResponse<?> deleteAccount(@RequestBody Map<String, String> body) {
        Long userId = securityUtil.getCurrentUserId();
        userService.deleteAccount(userId);
        return ApiResponse.ok(Map.of("message", "账号已注销"));
    }
}
