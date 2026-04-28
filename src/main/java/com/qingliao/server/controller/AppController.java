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
            "versionCode", 6,
            "versionName", "1.6",
            "downloadUrl", "http://47.108.172.100/download/qingliao-release.apk",
            "updateDesc", "1. 修复Tab闪退问题\n2. 消息推送优化\n3. 轻聊号独立系统\n4. 视频通话铃声+计时\n5. 阿里云服务器部署"
        ));
    }
}
