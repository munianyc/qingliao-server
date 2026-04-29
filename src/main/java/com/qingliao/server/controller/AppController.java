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
            "versionCode", 8,
            "versionName", "1.8",
            "downloadUrl", "http://47.108.172.100/download/qingliao-release.apk",
            "updateDesc", "1. 来电全局显示，任何界面都能接听\n2. 画中画模式，返回不中断通话\n3. 语音/视频通话自由选择\n4. WebSocket心跳保活，通话更稳定\n5. 图片查看器修复+消息换行修复"
        ));
    }
}
