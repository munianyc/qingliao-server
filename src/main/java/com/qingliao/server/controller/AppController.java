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
            "versionCode", 21,
            "versionName", "2.2.2",
            "downloadUrl", "http://47.108.172.100/download/qingliao-v2.2.2.apk",
            "updateDesc", "1. 全新UI设计，莫兰迪配色清新柔和，整体视觉风格统一\n2. 更换全新应用图标，更加简洁美观\n3. 聊天记录改为本地存储，打开聊天秒加载，不再等待服务器\n4. 服务器自动清理已读旧消息，节省存储空间\n5. 视频通话新增连接状态实时显示，方便排查通话问题\n6. 视频通话增加60秒超时保护，避免长时间等待\n7. 集成Firebase推送，后台消息到达率大幅提升\n8. 更新日志完整显示修复，支持滚动查看全部内容\n9. 群聊创建、联系人详情等页面配色统一优化"
        ));
    }
}
