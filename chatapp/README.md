# 💬 ChatNow — Real-Time Private Chat

A production-ready real-time private messaging app built with **Java 17**, **Spring Boot 3**, **WebSocket (STOMP)**, **PostgreSQL**, and **JWT Authentication**.

---

## 🚀 Quick Start

```bash
git clone https://github.com/YOUR_USERNAME/chatnow
cd chatnow
docker-compose up --build
```

Open: **http://localhost:8081**

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2 |
| Real-time | WebSocket + STOMP |
| Security | Spring Security + JWT |
| Database | PostgreSQL + Spring Data JPA |
| Frontend | HTML/CSS/JS (served by Spring Boot) |
| Container | Docker + Docker Compose |

---

## ✨ Features

- 🔐 JWT Authentication (register/login)
- 💬 Real-time private messaging via WebSocket
- ✓✓ Read receipts
- ⌨️ Typing indicators
- 🟢 Online/Offline status
- 🔍 User search
- 📱 Clean modern UI

---

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

## 🔒 Key Technical Highlights

- **WebSocket + STOMP** for real-time bidirectional communication
- **JWT stateless auth** — token passed in WebSocket headers
- **User-specific queues** — private messages delivered only to the recipient
- **Typing indicators** — debounced, auto-cancel after 2 seconds
- **Read receipts** — messages marked as READ when conversation is opened
