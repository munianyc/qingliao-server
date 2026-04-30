# 轻聊 - 后端服务器

轻聊即时通讯应用的后端服务器，基于Spring Boot构建。

## 技术栈

- **框架**: Spring Boot 3.x
- **语言**: Java 17
- **数据库**: MySQL 8.0
- **ORM**: Spring Data JPA + Hibernate
- **实时通讯**: WebSocket
- **推送**: Firebase Cloud Messaging
- **构建工具**: Gradle

## 功能模块

### 用户管理
- 用户注册/登录
- JWT Token 认证
- 个人资料管理
- 头像上传

### 消息系统
- 私聊消息
- 群聊消息
- 图片消息
- 消息已读回执
- 离线消息存储
- 消息自动清理（定时任务）

### 好友系统
- 好友请求发送/接受/拒绝
- 好友列表管理
- 好友删除

### 群组管理
- 群组创建
- 成员管理
- 群信息修改

### 视频通话信令
- WebSocket 信令服务器
- Offer/Answer 交换
- ICE Candidate 交换
- 通话状态管理
- 挂断/拒绝/忙线处理

### 文件服务
- 图片上传
- 文件下载
- 自动缩略图生成

## 项目结构

```
src/main/java/com/qingliao/server/
├── config/           # 配置类
│   ├── WebSocketConfig.java
│   └── SecurityConfig.java
├── controller/       # REST API控制器
│   ├── AppController.java
│   ├── UserController.java
│   ├── MessageController.java
│   └── FriendshipController.java
├── entity/          # 数据库实体
│   ├── User.java
│   ├── Message.java
│   └── Friendship.java
├── repository/      # 数据访问层
├── service/         # 业务逻辑层
├── websocket/       # WebSocket处理器
│   ├── ChatWebSocketHandler.java
│   └── CallSignalingHandler.java
└── schedule/        # 定时任务
    └── MessageCleanupTask.java
```

## API 接口

### 认证相关
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/refresh` - 刷新Token

### 用户相关
- `GET /api/user/profile` - 获取个人信息
- `PUT /api/user/profile` - 更新个人信息
- `POST /api/user/avatar` - 上传头像

### 消息相关
- `GET /api/messages/{sessionId}` - 获取历史消息
- `POST /api/messages` - 发送消息
- `PUT /api/messages/{id}/read` - 标记已读

### 好友相关
- `GET /api/friends` - 好友列表
- `POST /api/friends/request` - 发送好友请求
- `PUT /api/friends/request/{id}/accept` - 接受请求
- `DELETE /api/friends/{id}` - 删除好友

### 群组相关
- `POST /api/groups` - 创建群组
- `GET /api/groups/{id}` - 群信息
- `PUT /api/groups/{id}` - 更新群信息

### 应用相关
- `GET /api/app/version` - 检查版本更新
- `GET /api/app/config` - 获取应用配置

## WebSocket 接口

### 聊天 WebSocket
连接地址: `ws://server:8080/ws/chat?token={jwt_token}`

消息格式:
```json
{
  "type": "chat_message",
  "sessionId": 123,
  "content": "Hello",
  "contentType": 1
}
```

### 通话信令 WebSocket
连接地址: `ws://server:8080/ws/call?token={jwt_token}`

消息类型:
- `offer` - 发送通话邀请
- `answer` - 接听通话
- `ice_candidate` - ICE候选
- `hangup` - 挂断
- `reject` - 拒绝
- `busy` - 忙线

## 数据库设计

### 用户表 (users)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| username | VARCHAR | 用户名 |
| password | VARCHAR | 密码(BCrypt) |
| nickname | VARCHAR | 昵称 |
| avatar | VARCHAR | 头像URL |
| created_at | DATETIME | 创建时间 |

### 消息表 (messages)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| session_id | BIGINT | 会话ID |
| sender_id | BIGINT | 发送者ID |
| content | TEXT | 消息内容 |
| content_type | INT | 内容类型 |
| created_at | DATETIME | 发送时间 |

### 会话表 (sessions)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| type | INT | 会话类型(私聊/群聊) |
| created_at | DATETIME | 创建时间 |

## 配置说明

### application.yml
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/qingliao
    username: qingliao
    password: your_password

app:
  jwt-secret: your-secret-key
  upload-path: /opt/qingliao/uploads
```

### 环境变量
- `SPRING_DATASOURCE_URL` - 数据库连接URL
- `SPRING_DATASOURCE_USERNAME` - 数据库用户名
- `SPRING_DATASOURCE_PASSWORD` - 数据库密码
- `APP_JWT_SECRET` - JWT密钥

## 部署

### 系统要求
- Java 17+
- MySQL 8.0+
- 2GB+ RAM
- 2核+ CPU

### 构建
```bash
# 克隆项目
git clone https://github.com/munianyc/qingliao-server.git

# 构建
./gradlew build -x test

# 生成的JAR包
ls build/libs/qingliao-server.jar
```

### 运行
```bash
# 直接运行
java -jar build/libs/qingliao-server.jar

# 指定配置文件
java -jar build/libs/qingliao-server.jar --spring.config.location=/path/to/application.yml

# 后台运行
nohup java -jar qingliao-server.jar > app.log 2>&1 &
```

### Systemd 服务
创建 `/etc/systemd/system/qingliao.service`:
```ini
[Unit]
Description=QingLiao Server
After=network.target mysql.service

[Service]
Type=simple
User=root
ExecStart=/usr/bin/java -Xmx384m -jar /opt/qingliao/qingliao-server.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启动服务:
```bash
sudo systemctl daemon-reload
sudo systemctl enable qingliao
sudo systemctl start qingliao
sudo systemctl status qingliao
```

## 监控

### 日志查看
```bash
# 实时日志
journalctl -u qingliao -f

# 最近日志
journalctl -u qingliao -n 100
```

### 健康检查
```bash
curl http://localhost:8080/actuator/health
```

## 开发

### 开发环境
1. 安装 JDK 17
2. 安装 MySQL 8.0
3. 创建数据库: `CREATE DATABASE qingliao CHARACTER SET utf8mb4;`
4. 修改 `application.yml` 配置
5. 运行 `QingLiaoApplication.java`

### 测试
```bash
./gradlew test
```

## 更新日志

### v2.3.2 (2026-05-01)
- 更新版本检查接口
- 添加v2.3.2更新说明

### v2.3.1 (2026-04-30)
- 优化WebSocket连接处理
- 修复通话信令问题

### v2.3.0 (2026-04-30)
- 新增通话信令WebSocket
- 支持视频通话信令交换

[查看完整更新日志](CHANGELOG.md)

## 相关项目

- **Android客户端**: [qingliao-android](https://github.com/munianyc/qingliao-android)

## 开发者

- **munian** - [GitHub](https://github.com/munianyc)

## 许可证

本项目仅供学习交流使用。
