package com.qingliao.server.controller;

import com.qingliao.server.dto.ApiResponse;
import com.qingliao.server.entity.UserFcmToken;
import com.qingliao.server.repository.UserFcmTokenRepository;
import com.qingliao.server.security.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/push")
public class PushController {

    private final UserFcmTokenRepository fcmTokenRepo;
    private final JwtUtil jwtUtil;

    public PushController(UserFcmTokenRepository fcmTokenRepo, JwtUtil jwtUtil) {
        this.fcmTokenRepo = fcmTokenRepo;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 保存用户的FCM token
     */
    @PostMapping("/token")
    public ApiResponse<Void> saveFcmToken(@RequestHeader("Authorization") String auth,
                                          @RequestBody Map<String, String> body) {
        Long userId = jwtUtil.getUserIdFromToken(auth.replace("Bearer ", ""));
        String fcmToken = body.get("fcmToken");

        if (fcmToken == null || fcmToken.isEmpty()) {
            return ApiResponse.error(400, "FCM token is required");
        }

        // 检查是否已存在
        var existingTokens = fcmTokenRepo.findByUserId(userId);
        boolean tokenExists = existingTokens.stream()
                .anyMatch(t -> t.getFcmToken().equals(fcmToken));

        if (!tokenExists) {
            UserFcmToken token = new UserFcmToken();
            token.setUserId(userId);
            token.setFcmToken(fcmToken);
            fcmTokenRepo.save(token);
        }

        return ApiResponse.ok(null);
    }
}
