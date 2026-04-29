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
            "versionCode", 7,
            "versionName", "1.7",
            "downloadUrl", "http://47.108.172.100/download/qingliao-release.apk",
            "updateDesc", "1. TURN服务器UDP/TCP双通道优化，修复跨网视频通话无画面声音\n2. 修复消息过长显示不全，自动换行\n3. 图片/文件发送限制10MB，防止上传失败\n4. 新增在线状态绿点标识\n5. 修复消息推送通知（适配Android 13+权限）"
        ));
    }
}
