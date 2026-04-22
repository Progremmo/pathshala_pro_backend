# PathshalaPro - SaaS School Management System

## 📁 Project Location
`d:\codes\progremmo\pathshalapro\`

---

## 🏗️ What Was Built

A complete, production-ready Spring Boot 3.2 backend with **17 modules**, **50+ files**, and full multi-tenant support.

### Project Structure

```
pathshalapro/
├── pom.xml
├── PathshalaPro.postman_collection.json
├── src/main/java/com/pathshalapro/
│   ├── PathshalaPro.java               ← Main entry point
│   ├── entity/
│   │   ├── BaseEntity.java             ← Audit fields + soft delete
│   │   ├── School.java                 ← Tenant root
│   │   ├── User.java                   ← All roles in one table
│   │   ├── Role.java
│   │   ├── ClassRoom.java
│   │   ├── Subject.java
│   │   ├── Timetable.java
│   │   ├── Attendance.java
│   │   ├── Exam.java / Marks.java
│   │   ├── Notes.java
│   │   ├── FeeStructure.java / FeeInvoice.java / Payment.java
│   │   ├── OnlineClass.java
│   │   ├── Notification.java / Announcement.java
│   │   ├── SubscriptionPlan.java / SchoolSubscription.java
│   │   └── enums/                      ← 7 enums
│   ├── repository/                     ← 14 repositories with custom JPQL queries
│   ├── dto/                            ← All request/response DTOs
│   ├── service/impl/                   ← 9 service implementations
│   ├── controller/                     ← 10 REST controllers
│   ├── security/
│   │   ├── JwtTokenProvider.java       ← Token create/validate
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── CustomUserDetailsService.java
│   │   └── SecurityUtils.java
│   ├── config/
│   │   ├── SecurityConfig.java         ← Spring Security + CORS
│   │   ├── SwaggerConfig.java          ← OpenAPI 3.0
│   │   ├── RazorpayConfig.java
│   │   ├── AuditConfig.java
│   │   └── DataSeeder.java             ← Seeds roles + admin on startup
│   └── exception/
│       ├── ApiException.java
│       ├── ErrorResponse.java
│       └── GlobalExceptionHandler.java
└── src/main/resources/
    ├── application.properties
    └── schema.sql                      ← Full MySQL DDL
```

---

## 🚀 Setup Steps

### 1. Prerequisites
- Java 17+
- Maven 3.8+ (`JAVA_HOME` must be set)
- MySQL 8.0+

### 2. Database Setup
```sql
CREATE DATABASE pathshalapro_db CHARACTER SET utf8mb4;
```
Or run `schema.sql` directly.

### 3. Configure `application.properties`
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pathshalapro_db?...
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

razorpay.key.id=YOUR_KEY_ID
razorpay.key.secret=YOUR_KEY_SECRET
```

### 4. Build & Run
```bash
mvn clean install
mvn spring-boot:run
```

### 5. First Login
On startup, `DataSeeder` automatically creates:
- All 5 roles
- 3 subscription plans (STARTER/PRO/ENTERPRISE)
- Super admin: `admin@pathshalapro.com` / `Admin@123`
- Demo school: code `DEMO001`

---

## 🔑 API Quick Start

### Login
```http
POST /api/v1/auth/login
{
  "email": "admin@pathshalapro.com",
  "password": "Admin@123"
}
```
→ Receive `accessToken`, add to `Authorization: Bearer <token>` header.

### Access Swagger UI
```
http://localhost:8080/api/v1/swagger-ui.html
```

---

## 📋 All API Endpoints

| Module | Method | URL |
|--------|--------|-----|
| Auth | POST | `/auth/login` |
| Auth | POST | `/auth/register` |
| Auth | POST | `/auth/refresh` |
| Auth | POST | `/auth/change-password` |
| Schools | POST | `/schools` |
| Schools | GET | `/schools` |
| Schools | GET/PUT/DELETE | `/schools/{id}` |
| Fee Structures | POST/GET | `/schools/{id}/fees/structures` |
| Fee Invoices | POST/GET | `/schools/{id}/fees/invoices` |
| Razorpay | POST | `/schools/{id}/fees/payment/create-order` |
| Razorpay | POST | `/schools/{id}/fees/payment/verify` |
| Timetable | POST | `/schools/{id}/timetable` |
| Timetable | GET | `/schools/{id}/timetable/class/{classId}` |
| Timetable | GET | `/schools/{id}/timetable/teacher/{teacherId}` |
| Exams | POST/GET | `/schools/{id}/exams` |
| Marks | POST | `/schools/{id}/exams/{examId}/marks` |
| Results | PATCH | `/schools/{id}/exams/{examId}/publish` |
| Attendance | POST | `/schools/{id}/attendance` |
| Attendance | GET | `/schools/{id}/attendance/student/{id}/stats` |
| Notes | POST/GET | `/schools/{id}/notes` |
| Online Classes | POST/GET | `/schools/{id}/online-classes` |
| Online Classes | GET | `/schools/{id}/online-classes/upcoming` |
| Notifications | POST | `/schools/{id}/communication/notifications` |
| Announcements | POST/GET | `/schools/{id}/communication/announcements` |
| Reports | GET | `/schools/{id}/reports/student/{id}/performance` |
| Reports | GET | `/schools/{id}/reports/fees` |
| Reports | GET | `/schools/{id}/reports/attendance/class/{id}` |

---

## 🔒 RBAC Matrix

| API | PROJECT_ADMIN | SCHOOL_ADMIN | TEACHER | STUDENT | PARENT |
|-----|:-------------:|:------------:|:-------:|:-------:|:------:|
| Create School | ✅ | ❌ | ❌ | ❌ | ❌ |
| View School | ✅ | ✅ | ❌ | ❌ | ❌ |
| Create Fee | ✅ | ✅ | ❌ | ❌ | ❌ |
| Pay Fee | ✅ | ✅ | ❌ | ✅ | ✅ |
| Mark Attendance | ✅ | ✅ | ✅ | ❌ | ❌ |
| Enter Marks | ✅ | ✅ | ✅ | ❌ | ❌ |
| View Results | ✅ | ✅ | ✅ | ✅ | ✅ |
| Upload Notes | ✅ | ✅ | ✅ | ❌ | ❌ |
| View Notes | ✅ | ✅ | ✅ | ✅ | ✅ |

---

## ⭐ Key Technical Features

| Feature | Implementation |
|---------|---------------|
| Multi-tenancy | `school_id` in all tables + repository-level filtering |
| JWT Auth | HS256, 24h access + 7d refresh tokens |
| Password | BCrypt strength 12 |
| Conflict detection | JPQL overlap queries for teacher/classroom slots |
| Razorpay | Order creation + HMAC SHA256 signature verification |
| Soft delete | `is_deleted` flag on all entities |
| Audit | `created_at`, `updated_at`, `created_by`, `updated_by` |
| Validation | Jakarta Validation on all DTOs |
| Error handling | Global `@RestControllerAdvice` with typed errors |
| API docs | Springdoc OpenAPI 3.0 / Swagger UI |
| Pagination | Spring Data `Page<T>` on all list endpoints |

---

## 📦 Postman Collection
Import `PathshalaPro.postman_collection.json` into Postman.
Set collection variable `baseUrl = http://localhost:8080/api/v1`.
Run "Login (PROJECT_ADMIN)" first — the token is auto-saved.
