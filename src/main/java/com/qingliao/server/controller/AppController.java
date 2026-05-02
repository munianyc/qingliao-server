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
            "versionCode", 60,
            "versionName", "3.0.0",
            "downloadUrl", "http://47.108.172.100/download/yuliao-latest.apk",
            "updateDesc", "【v3.0.0 更新内容】\n1. 全新UI设计：紫蓝渐变主色调，年轻活力\n2. 全面使用ConstraintLayout优化布局性能\n3. 统一样式管理：新建styles.xml、dimens.xml\n4. 毛玻璃效果登录卡片\n5. 现代化输入框和按钮样式\n6. 消息气泡全新设计\n7. 架构优化：拆分CallActivity、ApiService\n8. 完善字符串资源提取"
        ));
    }
}
