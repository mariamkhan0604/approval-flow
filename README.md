# 🔐 Approval Flow System

A beginner-friendly Spring Boot REST API with MongoDB Atlas and Spring Security Basic Authentication.

---

## 📁 Project Structure

```
approval-flow/
├── pom.xml
└── src/
    └── main/
        ├── java/com/approvalflow/
        │   ├── ApprovalFlowApplication.java      ← Entry point
        │   ├── model/
        │   │   ├── User.java                     ← MongoDB document (users collection)
        │   │   ├── ApprovalRequest.java           ← MongoDB document (requests collection)
        │   │   ├── Role.java                      ← Enum: EMPLOYEE | MANAGER
        │   │   └── RequestStatus.java             ← Enum: PENDING | APPROVED | REJECTED
        │   ├── dto/
        │   │   ├── CreateRequestDTO.java          ← Input: create a request
        │   │   ├── UpdateStatusDTO.java           ← Input: update request status
        │   │   └── RequestResponseDTO.java        ← Output: returned to client
        │   ├── repository/
        │   │   ├── UserRepository.java            ← DB access for users
        │   │   └── ApprovalRequestRepository.java ← DB access for requests
        │   ├── service/
        │   │   └── ApprovalRequestService.java    ← Business logic
        │   ├── controller/
        │   │   └── ApprovalRequestController.java ← REST endpoints
        │   ├── security/
        │   │   └── SecurityConfig.java            ← Basic Auth + role rules
        │   └── exception/
        │       ├── ResourceNotFoundException.java ← 404 errors
        │       ├── BadRequestException.java       ← 400 errors
        │       └── GlobalExceptionHandler.java    ← Centralised error handling
        └── resources/
            └── application.properties            ← MongoDB URI + config
```

---

## 🛠️ Prerequisites

| Tool | Version |
|------|---------|
| Java | 17 or higher |
| Maven | 3.8+ |
| MongoDB Atlas | Free tier (M0) is enough |

---

## ☁️ MongoDB Atlas Setup (Step-by-Step)

1. **Create account** → https://cloud.mongodb.com
2. **Create a free cluster** (M0 Free Tier, any region)
3. **Create a database user**:
   - Go to **Security → Database Access**
   - Click **Add New Database User**
   - Set username + password (e.g. `appuser` / `StrongPass123`)
   - Role: **Atlas admin** (for development)
4. **Allow your IP**:
   - Go to **Security → Network Access**
   - Click **Add IP Address → Allow Access from Anywhere** (for dev)
   - In production: add only your server's IP
5. **Get connection string**:
   - Go to your cluster → **Connect → Drivers**
   - Copy the URI, it looks like:
     ```
     mongodb+srv://appuser:StrongPass123@cluster0.abcde.mongodb.net/
     ```
6. **Update `application.properties`**:
   ```properties
   spring.data.mongodb.uri=mongodb+srv://appuser:StrongPass123@cluster0.abcde.mongodb.net/approval_flow_db?retryWrites=true&w=majority
   ```

---

## ▶️ Running the Application

```bash
# Clone / navigate to project root
cd approval-flow

# Build and run
mvn spring-boot:run

# Or build a JAR first, then run it
mvn clean package
java -jar target/approval-flow-0.0.1-SNAPSHOT.jar
```

The server starts on **http://localhost:8080**

---

## 🔑 Sample Users (In-Memory)

| Username | Password | Role |
|----------|----------|------|
| employee | password | EMPLOYEE |
| manager  | password | MANAGER  |

---

## 📡 API Reference

### 1. POST /requests — Create a Request (EMPLOYEE only)

**cURL:**
```bash
curl -X POST http://localhost:8080/requests \
  -u employee:password \
  -H "Content-Type: application/json" \
  -d '{"description": "Request for 3 days leave"}'
```

**Success Response (201 Created):**
```json
{
  "requestId": "6641abc123def456789",
  "description": "Request for 3 days leave",
  "status": "PENDING",
  "employeeId": "employee",
  "managerId": null,
  "createdAt": "2024-05-15T10:30:00"
}
```

**Error — Missing description (400):**
```json
{
  "timestamp": "2024-05-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": {
    "description": "Description must not be blank"
  }
}
```

**Error — Manager tries to create (403):**
```json
HTTP 403 Forbidden
```

---

### 2. PUT /requests/{requestId}/status — Update Status (MANAGER only)

**cURL:**
```bash
curl -X PUT http://localhost:8080/requests/6641abc123def456789/status \
  -u manager:password \
  -H "Content-Type: application/json" \
  -d '{"status": "APPROVED"}'
```

**Success Response (200 OK):**
```json
{
  "requestId": "6641abc123def456789",
  "description": "Request for 3 days leave",
  "status": "APPROVED",
  "employeeId": "employee",
  "managerId": "manager",
  "createdAt": "2024-05-15T10:30:00"
}
```

**Error — Request not found (404):**
```json
{
  "timestamp": "2024-05-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Request not found with id: bad-id"
}
```

**Error — Setting status to PENDING (400):**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Status cannot be set back to PENDING. Use APPROVED or REJECTED."
}
```

---

## 🏗️ Architecture Overview

```
HTTP Request
     │
     ▼
┌─────────────────────────┐
│   Spring Security       │  ← Checks Basic Auth credentials
│   (SecurityConfig)      │  ← Enforces role-based access
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│   Controller Layer      │  ← Receives HTTP, calls service
│   (REST endpoints)      │  ← Returns HTTP response
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│   Service Layer         │  ← Business rules & logic
│   (ApprovalRequest      │  ← DTO ↔ Model mapping
│    Service)             │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│   Repository Layer      │  ← Spring Data MongoDB
│   (MongoRepository)     │  ← Auto-generated queries
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│   MongoDB Atlas         │  ← Cloud database
│   (users, requests)     │
└─────────────────────────┘
```

---

## 🧯 Common Issues & Fixes

| Problem | Fix |
|---------|-----|
| `Connection refused` to MongoDB | Check your Atlas IP whitelist + connection string |
| `403 Forbidden` | Wrong user role for that endpoint |
| `401 Unauthorized` | Missing or wrong Basic Auth credentials |
| `400 Bad Request` on POST | `description` field is missing or blank |
| `404 Not Found` on PUT | The `requestId` doesn't exist in MongoDB |
| Port 8080 already in use | Change `server.port=8081` in application.properties |

---

## 🔮 Possible Enhancements

- Store users in MongoDB instead of in-memory (implement `UserDetailsService`)
- Add `GET /requests` for employees to list their own requests
- Add JWT authentication instead of Basic Auth
- Add pagination for large result sets
- Add request timestamps for audit trails
- Secure the connection string with environment variables or Vault
