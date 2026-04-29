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
            "versionCode", 9,
            "versionName", "1.9",
            "downloadUrl", "http://47.108.172.100/download/qingliao-release.apk",
            "updateDesc", "1. 来电全局显示，任何界面都能接听，后台自动弹出\n2. 画中画模式，返回不中断通话\n3. 语音/视频通话自由选择，来电正确显示类型\n4. 通话信令委托修复，接听不再卡住\n5. 文件消息显示图标，点击下载\n6. WebSocket心跳保活，通话更稳定"
        ));
    }
}
