# PigFarmPro - Pig Farm Management System

**IT342 Phase 1 and Phase 2 - Authentication (Web + Mobile)**

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
- `/mobile` - Android Kotlin mobile application (Phase 2 implemented)
- `/docs` - Documentation (FRS, diagrams, screenshots)


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

## How to Run Mobile App (Phase 2)
**Prerequisites:**
- Android Studio (latest stable)
- Android Emulator or physical Android device

**Steps:**
1. Open `mobile/` in Android Studio
2. Let Gradle sync and finish indexing
3. Start backend first (`http://localhost:8081`)
4. Run the app on emulator/device

**Mobile Features Implemented:**
- Registration screen (Name, Email, Password)
- Login screen (Email, Password)
- Input validation and backend error handling
- Dashboard screen after successful login
- Token storage using `SharedPreferences`
- Logout and redirect to login

**API Integration:**
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/user/me`
- `POST /api/auth/logout`

**Note for Emulator Networking:**
- Mobile uses `http://10.0.2.2:8081` to access local backend

## Maven Coordinates
```xml
<groupId>edu.cit.mandawe</groupId>
<artifactId>pigfarmpro</artifactId>
<version>1.0.0</version>
```

## Phase 2 Submission Notes
- Final commit message format:
  `IT342 Phase 2 - Mobile Development Completed`
- Required screenshots:
  - Registration screen
  - Successful registration
  - Login screen
  - Successful login
  - After login screen (dashboard)
  - Database record in Supabase

