package com.qingliao.server.service;

import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.Notification;
import cn.jpush.api.push.model.notification.AndroidNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 极光推送服务
 */
@Service
public class JPushService {
    private static final Logger log = LoggerFactory.getLogger(JPushService.class);

    // JPush配置（替换为你的AppKey和MasterSecret）
    private static final String APP_KEY = "aa3ae4cb4690b34c57a4995f";
    private static final String MASTER_SECRET = "你的MasterSecret"; // 需要在极光后台获取

    private JPushClient jPushClient;

    public JPushService() {
        try {
            jPushClient = new JPushClient(MASTER_SECRET, APP_KEY);
            log.info("JPush client initialized");
        } catch (Exception e) {
            log.error("Failed to initialize JPush client", e);
        }
    }

    /**
     * 按别名发送推送（用于按用户推送）
     */
    public void sendPushByAlias(String alias, String title, String content, String extras) {
        if (jPushClient == null) {
            log.warn("JPush client not initialized");
            return;
        }

        try {
            PushPayload payload = PushPayload.newBuilder()
                    .setPlatform(cn.jpush.api.push.model.Platform.android())
                    .setAudience(Audience.alias(alias))
                    .setNotification(Notification.newBuilder()
                            .setAlert(content)
                            .addPlatformNotification(
                                    AndroidNotification.newBuilder()
                                            .setTitle(title)
                                            .setAlert(content)
                                            .addExtra("data", extras)
                                            .build())
                            .build())
                    .build();

            PushResult result = jPushClient.sendPush(payload);
            log.info("Push sent to alias={}, result: {}", alias, result);
        } catch (Exception e) {
            log.error("Failed to send push to alias=" + alias, e);
        }
    }

    /**
     * 发送聊天消息推送
     */
    public void sendChatPush(long userId, String senderName, String content, long sessionId, long senderId) {
        String alias = String.valueOf(userId);
        String title = senderName;
        String extras = String.format("{\"type\":\"chat\",\"senderId\":%d,\"sessionId\":%d,\"content\":\"%s\"}",
                senderId, sessionId, content);

        sendPushByAlias(alias, title, content, extras);
    }

    /**
     * 发送好友请求推送
     */
    public void sendFriendRequestPush(long userId, String senderName) {
        String alias = String.valueOf(userId);
        String title = "好友请求";
        String content = senderName + " 请求添加你为好友";
        String extras = String.format("{\"type\":\"friend_request\",\"senderName\":\"%s\"}", senderName);

        sendPushByAlias(alias, title, content, extras);
    }

    /**
     * 发送来电推送
     */
    public void sendCallPush(long userId, String callerName, boolean isVideo, long callerId) {
        String alias = String.valueOf(userId);
        String title = callerName;
        String content = isVideo ? "邀请你视频通话" : "邀请你语音通话";
        String extras = String.format("{\"type\":\"call\",\"callerId\":%d,\"callerName\":\"%s\",\"isVideo\":%b}",
                callerId, callerName, isVideo);

        sendPushByAlias(alias, title, content, extras);
    }
}
