# 💬 ChatNow — Real-Time Private Chat

A production-ready real-time private messaging app built with **Java 17**, **Spring Boot 3**, **WebSocket (STOMP)**, and **JWT Authentication

## 📡 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/auth/register | Register |
| POST | /api/auth/login | Login |
| GET | /api/users | Get all users |
| GET | /api/users/search | Search users |
| POST | /api/messages | Send message |
| GET | /api/messages/conversation/{id} | Get conversation |
| GET | /api/messages/conversations | Get all conversations |

## WebSocket
- **Endpoint:** `/ws`
- **Subscribe:** `/user/{email}/queue/messages`
- **Send message:** `/app/chat.send`
- **Typing:** `/app/chat.typing`

---


