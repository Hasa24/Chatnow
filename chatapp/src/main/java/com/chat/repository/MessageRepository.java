package com.chat.repository;

import com.chat.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Get full conversation between two users
    @Query("""
        SELECT m FROM Message m
        WHERE (m.sender.id = :userId1 AND m.receiver.id = :userId2)
           OR (m.sender.id = :userId2 AND m.receiver.id = :userId1)
        ORDER BY m.sentAt ASC
    """)
    List<Message> findConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    // Get latest message for each conversation (for sidebar)
    @Query("""
        SELECT m FROM Message m
        WHERE m.id IN (
            SELECT MAX(m2.id) FROM Message m2
            WHERE m2.sender.id = :userId OR m2.receiver.id = :userId
            GROUP BY CASE
                WHEN m2.sender.id = :userId THEN m2.receiver.id
                ELSE m2.sender.id
            END
        )
        ORDER BY m.sentAt DESC
    """)
    List<Message> findLatestConversations(@Param("userId") Long userId);

    // Count unread messages from a specific sender
    @Query("""
        SELECT COUNT(m) FROM Message m
        WHERE m.sender.id = :senderId
          AND m.receiver.id = :receiverId
          AND m.status != 'READ'
    """)
    long countUnread(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    // Mark all messages from sender to receiver as READ
    @Modifying
    @Query("""
        UPDATE Message m SET m.status = 'READ'
        WHERE m.sender.id = :senderId
          AND m.receiver.id = :receiverId
          AND m.status != 'READ'
    """)
    void markAsRead(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
}
