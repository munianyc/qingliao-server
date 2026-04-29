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
            "versionCode", 10,
            "versionName", "1.9.1",
            "downloadUrl", "http://47.108.172.100/download/qingliao-release.apk",
            "updateDesc", "1. 群聊功能（创建/群信息/成员管理/群消息）\n2. 前置镜头镜像显示\n3. 通话ICE增强（多TURN+STUN+TCP穿透）\n4. 通话结束后聊天记录\n5. 接听/拒绝UI+计时器修复\n6. 来电全局显示+画中画+语音/视频选择"
        ));
    }
}
