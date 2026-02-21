package com.chat.controller;

import com.chat.dto.ChatDto;
import com.chat.entity.User;
import com.chat.repository.UserRepository;
import com.chat.security.JwtUtil;
import com.chat.service.AuthService;
import com.chat.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// ── Auth Controller ───────────────────────────────────────────────────────────

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth")
class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<ChatDto.AuthResponse> register(@Valid @RequestBody ChatDto.RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token")
    public ResponseEntity<ChatDto.AuthResponse> login(@Valid @RequestBody ChatDto.LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}

// ── User Controller ───────────────────────────────────────────────────────────

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users")
class UserController {

    private final AuthService     authService;
    private final UserRepository  userRepository;
    private final JwtUtil         jwtUtil;

    @GetMapping
    @Operation(summary = "Get all users except current")
    public ResponseEntity<List<ChatDto.UserResponse>> getUsers(
            @AuthenticationPrincipal UserDetails currentUser) {
        User user = userRepository.findByEmail(currentUser.getUsername()).orElseThrow();
        return ResponseEntity.ok(authService.getAllUsers(user.getId()));
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by username")
    public ResponseEntity<List<ChatDto.UserResponse>> search(
            @RequestParam String query,
            @AuthenticationPrincipal UserDetails currentUser) {
        User user = userRepository.findByEmail(currentUser.getUsername()).orElseThrow();
        return ResponseEntity.ok(authService.searchUsers(query, user.getId()));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user info")
    public ResponseEntity<ChatDto.UserResponse> me(
            @AuthenticationPrincipal UserDetails currentUser) {
        User user = userRepository.findByEmail(currentUser.getUsername()).orElseThrow();
        return ResponseEntity.ok(ChatDto.UserResponse.from(user));
    }
}

// ── Message Controller ────────────────────────────────────────────────────────

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Messages")
class MessageController {

    private final MessageService  messageService;
    private final UserRepository  userRepository;

    @PostMapping
    @Operation(summary = "Send a message")
    public ResponseEntity<ChatDto.MessageResponse> send(
            @Valid @RequestBody ChatDto.SendMessageRequest req,
            @AuthenticationPrincipal UserDetails currentUser) {
        User user = userRepository.findByEmail(currentUser.getUsername()).orElseThrow();
        return ResponseEntity.status(HttpStatus.CREATED).body(messageService.sendMessage(user.getId(), req));
    }

    @GetMapping("/conversation/{otherUserId}")
    @Operation(summary = "Get conversation with a user")
    public ResponseEntity<List<ChatDto.MessageResponse>> getConversation(
            @PathVariable Long otherUserId,
            @AuthenticationPrincipal UserDetails currentUser) {
        User user = userRepository.findByEmail(currentUser.getUsername()).orElseThrow();
        return ResponseEntity.ok(messageService.getConversation(user.getId(), otherUserId));
    }

    @GetMapping("/conversations")
    @Operation(summary = "Get all my conversations (latest message each)")
    public ResponseEntity<List<ChatDto.MessageResponse>> getConversations(
            @AuthenticationPrincipal UserDetails currentUser) {
        User user = userRepository.findByEmail(currentUser.getUsername()).orElseThrow();
        return ResponseEntity.ok(messageService.getLatestConversations(user.getId()));
    }
}

// ── WebSocket Controller ──────────────────────────────────────────────────────

@org.springframework.stereotype.Controller
@RequiredArgsConstructor
class WsController {

    private final MessageService messageService;
    private final UserRepository userRepository;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatDto.WsMessage message) {
        ChatDto.SendMessageRequest req = new ChatDto.SendMessageRequest();
        req.setReceiverId(message.getReceiverId());
        req.setContent(message.getContent());
        messageService.sendMessage(message.getSenderId(), req);
    }

    @MessageMapping("/chat.typing")
    public void typing(@Payload Map<String, Object> payload) {
        Long senderId   = Long.valueOf(payload.get("senderId").toString());
        Long receiverId = Long.valueOf(payload.get("receiverId").toString());
        boolean isTyping = Boolean.parseBoolean(payload.get("typing").toString());
        messageService.sendTyping(senderId, receiverId, isTyping);
    }
}
