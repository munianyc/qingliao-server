package com.qingliao.server.service;

import com.qingliao.server.dto.*;
import com.qingliao.server.entity.User;
import com.qingliao.server.repository.UserRepository;
import com.qingliao.server.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepo, PasswordEncoder encoder, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    public String register(RegisterRequest req) {
        if (req.getUsername().length() < 3) return "用户名至少3个字符";
        if (req.getPassword().length() < 6) return "密码至少6个字符";
        if (userRepo.findByUsername(req.getUsername()).isPresent()) return "用户名已存在";

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPasswordHash(encoder.encode(req.getPassword()));
        user.setNickname(req.getNickname() != null ? req.getNickname() : req.getUsername());
        userRepo.save(user);
        return null;
    }

    public LoginResponse login(LoginRequest req) {
        User user = userRepo.findByUsername(req.getUsername()).orElse(null);
        if (user == null || !encoder.matches(req.getPassword(), user.getPasswordHash())) return null;

        user.setOnlineStatus(1);
        user.setLastOnline(System.currentTimeMillis());
        userRepo.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginResponse(token, user.getId(), user.getNickname(), user.getAvatar());
    }

    public void logout(Long userId) {
        userRepo.findById(userId).ifPresent(user -> {
            user.setOnlineStatus(0);
            user.setLastOnline(System.currentTimeMillis());
            userRepo.save(user);
        });
    }
}
