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
            "versionCode", 19,
            "versionName", "2.2.0",
            "downloadUrl", "http://47.108.172.100/download/qingliao-v2.2.0.apk",
            "updateDesc", "1. 全新UI设计，莫兰迪配色清新柔和\n2. 更换应用图标\n3. 聊天记录本地存储，打开秒加载\n4. 视频通话连接状态实时显示\n5. 集成Firebase推送，后台消息到达率提升\n6. 更新日志完整显示修复"
        ));
    }
}
