package com.chat.service;

import com.chat.dto.ChatDto;
import com.chat.entity.Message;
import com.chat.entity.User;
import com.chat.repository.MessageRepository;
import com.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository   messageRepository;
    private final UserRepository      userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // ── Send Message ──────────────────────────────────────────────────────
    @Transactional
    public ChatDto.MessageResponse sendMessage(Long senderId, ChatDto.SendMessageRequest req) {
        User sender   = userRepository.findById(senderId).orElseThrow();
        User receiver = userRepository.findById(req.getReceiverId()).orElseThrow();

        Message msg = Message.builder()
            .sender(sender)
            .receiver(receiver)
            .content(req.getContent())
            .status(Message.MessageStatus.SENT)
            .sentAt(LocalDateTime.now())
            .build();

        Message saved = messageRepository.save(msg);
        ChatDto.MessageResponse response = ChatDto.MessageResponse.from(saved);

        // Push via WebSocket to receiver in real time
        ChatDto.WsMessage ws = new ChatDto.WsMessage();
        ws.setType("MESSAGE");
        ws.setSenderId(sender.getId());
        ws.setSenderUsername(sender.getDisplayUsername());
        ws.setSenderAvatarColor(sender.getAvatarColor());
        ws.setReceiverId(receiver.getId());
        ws.setContent(req.getContent());
        ws.setMessageId(saved.getId());
        ws.setStatus("SENT");
        ws.setSentAt(saved.getSentAt());

        messagingTemplate.convertAndSendToUser(
            receiver.getEmail(), "/queue/messages", ws);

        return response;
    }

    // ── Get Conversation ──────────────────────────────────────────────────
    @Transactional
    public List<ChatDto.MessageResponse> getConversation(Long userId, Long otherUserId) {
        // Mark all messages from other user as read
        messageRepository.markAsRead(otherUserId, userId);

        // Notify sender that messages were read
        User reader = userRepository.findById(userId).orElseThrow();
        User other  = userRepository.findById(otherUserId).orElseThrow();

        ChatDto.WsMessage readNotif = new ChatDto.WsMessage();
        readNotif.setType("READ");
        readNotif.setSenderId(userId);
        readNotif.setReceiverId(otherUserId);
        messagingTemplate.convertAndSendToUser(other.getEmail(), "/queue/messages", readNotif);

        return messageRepository.findConversation(userId, otherUserId)
            .stream().map(ChatDto.MessageResponse::from).toList();
    }

    // ── Get Conversations List ─────────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<ChatDto.MessageResponse> getLatestConversations(Long userId) {
        return messageRepository.findLatestConversations(userId)
            .stream().map(ChatDto.MessageResponse::from).toList();
    }

    // ── Send Typing indicator ─────────────────────────────────────────────
    public void sendTyping(Long senderId, Long receiverId, boolean isTyping) {
        User sender = userRepository.findById(senderId).orElseThrow();
        User receiver = userRepository.findById(receiverId).orElseThrow();

        ChatDto.WsMessage typing = new ChatDto.WsMessage();
        typing.setType(isTyping ? "TYPING" : "STOP_TYPING");
        typing.setSenderId(senderId);
        typing.setSenderUsername(sender.getDisplayUsername());
        typing.setReceiverId(receiverId);
        messagingTemplate.convertAndSendToUser(receiver.getEmail(), "/queue/messages", typing);
    }

    // ── Online/Offline status ─────────────────────────────────────────────
    public void updateStatus(Long userId, User.OnlineStatus status) {
        userRepository.findById(userId).ifPresent(u -> {
            u.setStatus(status);
            u.setLastSeen(LocalDateTime.now());
            userRepository.save(u);
        });
    }
}
