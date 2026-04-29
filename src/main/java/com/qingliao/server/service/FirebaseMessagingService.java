package com.qingliao.server.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firebase Cloud Messaging 推送服务
 */
@Service
public class FirebaseMessagingService {
    private static final Logger log = LoggerFactory.getLogger(FirebaseMessagingService.class);
    private FirebaseApp firebaseApp;

    @PostConstruct
    public void init() {
        try {
            // 从文件加载服务账号密钥
            InputStream serviceAccount = new FileInputStream("/opt/qingliao/firebase-service-account.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            firebaseApp = FirebaseApp.initializeApp(options);
            log.info("Firebase initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Firebase", e);
        }
    }

    /**
     * 按token发送推送
     */
    public void sendPushByToken(String fcmToken, String title, String body, Map<String, String> data) {
        if (firebaseApp == null) {
            log.warn("Firebase not initialized");
            return;
        }

        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .setSound("default")
                                    .setPriority(AndroidNotification.Priority.HIGH)
                                    .build())
                            .build());

            // 添加自定义数据
            if (data != null) {
                messageBuilder.putAllData(data);
            }

            Message message = messageBuilder.build();
            String response = FirebaseMessaging.getInstance(firebaseApp).send(message);
            log.info("FCM sent successfully: {}", response);
        } catch (Exception e) {
            log.error("Failed to send FCM to token=" + fcmToken, e);
        }
    }

    /**
     * 按用户ID发送推送（需要先将FCM token与用户ID关联）
     */
    public void sendChatPush(String fcmToken, String senderName, String content, long sessionId, long senderId) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "chat");
        data.put("senderId", String.valueOf(senderId));
        data.put("sessionId", String.valueOf(sessionId));
        data.put("content", content);

        sendPushByToken(fcmToken, senderName, content, data);
    }

    /**
     * 发送好友请求推送
     */
    public void sendFriendRequestPush(String fcmToken, String senderName) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "friend_request");
        data.put("senderName", senderName);

        sendPushByToken(fcmToken, "好友请求", senderName + " 请求添加你为好友", data);
    }

    /**
     * 发送来电推送
     */
    public void sendCallPush(String fcmToken, String callerName, boolean isVideo, long callerId) {
        String content = isVideo ? "邀请你视频通话" : "邀请你语音通话";

        Map<String, String> data = new HashMap<>();
        data.put("type", "call");
        data.put("callerId", String.valueOf(callerId));
        data.put("callerName", callerName);
        data.put("isVideo", String.valueOf(isVideo));

        sendPushByToken(fcmToken, callerName, content, data);
    }

    /**
     * 发送给多个用户
     */
    public void sendMulticastPush(List<String> fcmTokens, String title, String body, Map<String, String> data) {
        if (firebaseApp == null || fcmTokens == null || fcmTokens.isEmpty()) {
            return;
        }

        try {
            MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                    .addAllTokens(fcmTokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setTitle(title)
                                    .setBody(body)
                                    .setSound("default")
                                    .setPriority(AndroidNotification.Priority.HIGH)
                                    .build())
                            .build());

            if (data != null) {
                messageBuilder.putAllData(data);
            }

            MulticastMessage message = messageBuilder.build();
            BatchResponse response = FirebaseMessaging.getInstance(firebaseApp).sendEachForMulticast(message);
            log.info("FCM multicast sent: success={}, failure={}", response.getSuccessCount(), response.getFailureCount());
        } catch (Exception e) {
            log.error("Failed to send FCM multicast", e);
        }
    }
}
