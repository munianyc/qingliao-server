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
            "versionCode", 31,
            "versionName", "2.3.4",
            "downloadUrl", "http://47.108.172.100/download/qingliao-latest.apk",
            "updateDesc", "【v2.3.4 更新内容】\n1. 修复小米手机按返回键闪退问题（MIUI立即销毁Surface导致native crash）\n2. 恢复完整的ICE重连机制（offer/answer交换方式）\n3. 优化重连时间：5秒未连接自动尝试重连\n\n---\n【v2.3.3 更新内容】\n1. 修复悬浮窗模式下Activity被系统杀死导致通话断开\n2. WebRTC资源由Service管理，悬浮窗更稳定\n3. ICE重连优化：先用restartIce()快速重连，5秒无响应再完整重连\n4. 小米等MIUI系统悬浮窗不再闪退\n\n---\n【v2.3.2 更新内容】\n1. 修复视频通话返回闪退问题（SurfaceViewRenderer native crash）\n2. 修复ICE断开后等待15秒才重连，改为立即尝试ICE restart\n3. 悬浮窗大小调整为屏幕1/7\n4. 优化视频通话资源释放顺序\n\n---\n【v2.3.1 更新内容】\n1. 修复通话中按返回闪退问题\n2. 优化连接速度：3秒未连上自动重连\n3. 悬浮窗尺寸缩小\n4. 未连接时返回回到会话列表\n\n---\n【v2.3.0 更新内容】\n1. 视频通话全新界面，对标QQ通话体验\n2. 新增悬浮窗小窗模式（替代系统画中画）\n3. 新增摄像头开关按钮\n4. 点击屏幕可收起/显示通话控件\n5. 修复后置摄像头镜像问题\n6. 修复长时间通话画面卡死问题\n\n---\n【v2.2.7 更新内容】\n1. 修复已读回执：多条消息现在能正确全部标记已读\n2. 修复视频通话来电接听后黑屏无画面问题\n3. 修复通话计时只有一边显示的问题\n4. 修复消息发送偶尔失败（FCM推送阻塞）\n5. 修复TURN中继服务器配置，提升视频通话连接成功率\n\n---\n【v2.2.6 更新内容】\n1. 视频通话新增ICE断线重连机制\n2. 消息存储改为本地优先，聊天秒加载\n3. 服务器自动清理旧消息，节省存储空间\n\n---\n【v2.2.5 更新内容】\n1. 全新UI设计，莫兰迪配色清新柔和\n2. 更换全新应用图标\n3. 视频通话新增连接状态实时显示\n4. 视频通话增加60秒超时保护\n5. 集成Firebase推送\n6. 更新日志支持滚动查看\n7. 群聊创建、联系人详情等页面配色统一优化"
        ));
    }
}
