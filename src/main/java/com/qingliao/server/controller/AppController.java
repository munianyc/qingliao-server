package com.qingliao.server.controller;

import com.qingliao.server.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/app")
public class AppController {

    @GetMapping("/version")
    public ApiResponse<Map<String, Object>> getVersion() {
        return ApiResponse.ok(Map.of(
            "versionCode", 12,
            "versionName", "2.0.1",
            "downloadUrl", "http://47.108.172.100/download/qingliao-v2.0.1.apk",
            "updateDesc", "1. 修复视频通话接通后无声音画面\n2. 修复底部导航栏红点显示不全\n3. 修复退出应用后消息通知丢失\n4. 优化WebSocket后台连接保持"
        ));
    }
}
