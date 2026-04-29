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
            "versionCode", 17,
            "versionName", "2.0.6",
            "downloadUrl", "http://47.108.172.100/download/qingliao-v2.0.6.apk",
            "updateDesc", "1. 集成Firebase推送，后台消息到达率提升\n2. 支持小米/OPPO/vivo/荣耀等品牌\n3. 优化推送通知显示"
        ));
    }
}
