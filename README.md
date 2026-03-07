# PigFarmPro - Pig Farm Management System

**IT342 Phase 1 - User Registration and Authentication**

PigFarmPro is a comprehensive pig farm management system designed to help farmers track pigs, pens, feeding schedules, health records, sales, and mortality data. This Phase 1 implementation focuses on user registration and authentication functionality.

## Technologies Used
- **Backend**: Spring Boot 3.4.1, Spring Security, Spring Data JPA
- **Database**: PostgreSQL (Supabase Cloud)
- **Frontend**: React + Vite
- **Security**: BCrypt password hashing, JWT authentication
- **Build Tool**: Maven
- **Version Control**: Git & GitHub

## Project Structure
- `/backend` - Spring Boot REST API (Maven project: edu.cit.mandawe:pigfarmpro)
- `/web` - React + Vite web application
- `/mobile` - Android Kotlin mobile application (future implementation)
- `/docs` - Documentation (FRS, diagrams, screenshots)

## Database Schema (Phase 1)
**Users Table:**
- id (Primary Key)
- username (Unique)
- email (Unique)
- password_hash (BCrypt)
- full_name
- role (default: "USER")
- created_at

**Additional Tables (ERD Complete):**
- pens, pigs, feedings, health_records, sales, mortality_records

## API Endpoints (Phase 1)
**Authentication:**
- `POST /api/auth/register` - Register new user (returns 201 Created)
- `POST /api/auth/login` - Login user (returns 200 OK with JWT token)
- `POST /api/auth/logout` - Logout user (returns 200 OK)

**Response Format:**
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation successful",
  "timestamp": "2026-03-07T10:30:00.000Z"
}
```

## How to Run Backend
**Prerequisites:**
- Java 17 or higher
- Maven 3.6+

**Steps:**
1. The project is configured to use Supabase PostgreSQL (cloud database)
2. From the repo root:

```powershell
cd backend
mvnw.cmd spring-boot:run
```

Backend runs on `http://localhost:8081`.

**Database Connection:**
- Host: db.vtgcxynqvkrlfztdpsga.supabase.co
- Port: 5432
- Database: postgres
- All credentials are in `application.properties`

## How to Run Web App
**Prerequisites:**
- Node.js (LTS version)

**Steps:**
1. From the repo root:

```powershell
cd web
npm install
npm run dev
```

2. Open `http://localhost:5173` in your browser

**Features:**
- User Registration (username, email, password, full name)
- User Login (email, password)
- Dashboard (displays user info after login)
- Input validation and duplicate prevention

## Phase 1 Implementation Details
**User Registration:**
- Fields: username, email, password, full name
- Validation: username (3-50 chars), email format, password (8+ chars)
- Duplicate Prevention: Returns 409 Conflict if email/username exists
- Password Security: BCrypt hashing with 10 salt rounds

**User Login:**
- Credentials: email and password
- Verification: BCrypt password matching against stored hash
- Post-login: JWT token generation, redirect to dashboard

**Security Features:**
- BCrypt password hashing (never stores plain text)
- JWT token-based authentication
- CORS configuration for localhost development
- Input validation on both frontend and backend

## Maven Coordinates
```xml
<groupId>edu.cit.mandawe</groupId>
<artifactId>pigfarmpro</artifactId>
<version>1.0.0</version>
```

## Git Commit (Phase 1)
**Commit Message:** "IT342 Phase 1 – User Registration and Login Completed"  
**Commit Hash:** 98c4364a44f58d805577182a3c482234266af3c7

## Future Phases
- Phase 2: Pen and Pig Management
- Phase 3: Feeding and Health Records
- Phase 4: Sales and Mortality Tracking
- Phase 5: Mobile Application Integration
