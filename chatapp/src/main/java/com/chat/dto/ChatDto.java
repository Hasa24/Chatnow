package com.chat.dto;

import com.chat.entity.Message;
import com.chat.entity.User;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

public class ChatDto {

    // ── Auth ──────────────────────────────────────────────────────────────

    @Data
    public static class RegisterRequest {
        @NotBlank private String username;
        @Email @NotBlank private String email;
        @NotBlank @Size(min = 6) private String password;
    }

    @Data
    public static class LoginRequest {
        @Email @NotBlank private String email;
        @NotBlank private String password;
    }

    @Data @Builder
    public static class AuthResponse {
        private String token;
        private String tokenType;
        private Long userId;
        private String username;
        private String email;
        private String avatarColor;

        public static AuthResponse from(String token, User user) {
            return AuthResponse.builder()
                .token(token).tokenType("Bearer")
                .userId(user.getId())
                .username(user.getDisplayUsername())
                .email(user.getEmail())
                .avatarColor(user.getAvatarColor())
                .build();
        }
    }

    // ── User ──────────────────────────────────────────────────────────────

    @Data @Builder
    public static class UserResponse {
        private Long id;
        private String username;
        private String avatarColor;
        private String status;
        private LocalDateTime lastSeen;
        private long unreadCount;

        public static UserResponse from(User u) {
            return UserResponse.builder()
                .id(u.getId())
                .username(u.getDisplayUsername())
                .avatarColor(u.getAvatarColor())
                .status(u.getStatus() != null ? u.getStatus().name() : "OFFLINE")
                .lastSeen(u.getLastSeen())
                .build();
        }
    }

    // ── Message ───────────────────────────────────────────────────────────

    @Data
    public static class SendMessageRequest {
        @NotNull  private Long receiverId;
        @NotBlank private String content;
    }

    @Data @Builder
    public static class MessageResponse {
        private Long id;
        private Long senderId;
        private String senderUsername;
        private String senderAvatarColor;
        private Long receiverId;
        private String content;
        private String status;
        private LocalDateTime sentAt;
        private LocalDateTime readAt;

        public static MessageResponse from(Message m) {
            return MessageResponse.builder()
                .id(m.getId())
                .senderId(m.getSender().getId())
                .senderUsername(m.getSender().getDisplayUsername())
                .senderAvatarColor(m.getSender().getAvatarColor())
                .receiverId(m.getReceiver().getId())
                .content(m.getContent())
                .status(m.getStatus().name())
                .sentAt(m.getSentAt())
                .readAt(m.getReadAt())
                .build();
        }
    }

    // ── WebSocket ─────────────────────────────────────────────────────────

    @Data
    public static class WsMessage {
        private String type;       // MESSAGE, READ, TYPING, ONLINE, OFFLINE
        private Long senderId;
        private String senderUsername;
        private String senderAvatarColor;
        private Long receiverId;
        private String content;
        private Long messageId;
        private String status;
        private LocalDateTime sentAt;
    }
}
