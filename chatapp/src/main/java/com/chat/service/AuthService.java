package com.chat.service;

import com.chat.dto.ChatDto;
import com.chat.entity.User;
import com.chat.repository.UserRepository;
import com.chat.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository    userRepository;
    private final PasswordEncoder   passwordEncoder;
    private final JwtUtil           jwtUtil;
    private final AuthenticationManager authManager;

    private static final String[] AVATAR_COLORS = {
        "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4",
        "#FECA57", "#FF9FF3", "#54A0FF", "#5F27CD"
    };

    public ChatDto.AuthResponse register(ChatDto.RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email already registered");
        if (userRepository.existsByUsername(req.getUsername()))
            throw new RuntimeException("Username already taken");

        String color = AVATAR_COLORS[new Random().nextInt(AVATAR_COLORS.length)];
        User user = User.builder()
            .username(req.getUsername())
            .email(req.getEmail())
            .password(passwordEncoder.encode(req.getPassword()))
            .avatarColor(color)
            .status(User.OnlineStatus.OFFLINE)
            .createdAt(LocalDateTime.now())
            .build();

        userRepository.save(user);
        return ChatDto.AuthResponse.from(jwtUtil.generateToken(user), user);
    }

    public ChatDto.AuthResponse login(ChatDto.LoginRequest req) {
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        User user = userRepository.findByEmail(req.getEmail()).orElseThrow();
        user.setStatus(User.OnlineStatus.ONLINE);
        userRepository.save(user);
        return ChatDto.AuthResponse.from(jwtUtil.generateToken(user), user);
    }

    public List<ChatDto.UserResponse> searchUsers(String query, Long currentUserId) {
        return userRepository.searchUsers(query, currentUserId)
            .stream().map(ChatDto.UserResponse::from).toList();
    }

    public List<ChatDto.UserResponse> getAllUsers(Long currentUserId) {
        return userRepository.findAll().stream()
            .filter(u -> !u.getId().equals(currentUserId))
            .map(ChatDto.UserResponse::from).toList();
    }
}
