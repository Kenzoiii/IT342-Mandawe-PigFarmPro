# Functional Requirements Specification (FRS)
## PigFarmPro - Pig Farm Management System

**Project:** IT342 Phase 1 - User Registration and Authentication  
**Date:** March 7, 2026  
**Version:** 1.0.0

## Overview
PigFarmPro is a comprehensive pig farm management system designed to streamline farm operations including pig tracking, feeding schedules, health monitoring, sales records, and mortality tracking. This FRS documents the Phase 1 implementation focusing on user registration and authentication functionality.

## System Architecture
- **Backend:** Spring Boot 3.4.1 REST API (Maven: edu.cit.mandawe:pigfarmpro)
- **Database:** PostgreSQL (Supabase Cloud)
- **Frontend:** React + Vite web application
- **Security:** BCrypt password hashing, JWT token authentication
- **Deployment:** Development mode (localhost)

## Phase 1 Scope
### Functional Requirements
1. **User Registration**
   - Users can register with username, email, password, and full name
   - System validates all input fields
   - System prevents duplicate usernames and emails
   - Passwords are hashed using BCrypt before storage

2. **User Login**
   - Users can login with email and password
   - System verifies credentials against stored BCrypt hash
   - System generates JWT token upon successful authentication
   - System redirects authenticated users to dashboard

3. **User Dashboard**
   - Displays logged-in user information (username, email, role)
   - Requires valid authentication token
   - Provides logout functionality

4. **Error Handling**
   - Validation errors return 400 Bad Request with error details
   - Duplicate registration returns 409 Conflict
   - Invalid login returns 401 Unauthorized
   - All errors follow standardized API response format

## Backend API Specification

### Base URL
```
http://localhost:8081/api
```

### Endpoints

#### 1. User Registration
**Endpoint:** `POST /api/auth/register`

**Request Body:**
```json
{
  "username": "string (3-50 chars, required)",
  "email": "string (valid email format, required)",
  "password": "string (min 8 chars, required)",
  "fullName": "string (required)"
}
```

**Success Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "username": "farmer_maria",
    "email": "maria@pigfarm.com",
    "fullName": "Maria Santos",
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  },
  "message": "Registration successful",
  "timestamp": "2026-03-07T10:30:00.000Z"
}
```

**Error Response (409 Conflict):**
```json
{
  "success": false,
  "data": null,
  "message": "Registration failed",
  "error": {
    "code": "DB-002",
    "details": "Email already exists"
  },
  "timestamp": "2026-03-07T10:30:00.000Z"
}
```

#### 2. User Login
**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "email": "string (required)",
  "password": "string (required)"
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "username": "farmer_maria",
    "email": "maria@pigfarm.com",
    "fullName": "Maria Santos",
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  },
  "message": "Login successful",
  "timestamp": "2026-03-07T10:30:00.000Z"
}
```

**Error Response (401 Unauthorized):**
```json
{
  "success": false,
  "data": null,
  "message": "Authentication failed",
  "error": {
    "code": "AUTH-001",
    "details": "Invalid credentials"
  },
  "timestamp": "2026-03-07T10:30:00.000Z"
}
```

#### 3. User Logout
**Endpoint:** `POST /api/auth/logout`

**Success Response (200 OK):**
```json
{
  "success": true,
  "data": null,
  "message": "Logged out successfully",
  "timestamp": "2026-03-07T10:30:00.000Z"
}
```

### Error Codes
- **AUTH-001:** Invalid credentials
- **DB-002:** Database constraint violation (duplicate email/username)
- **VALID-001:** Validation error (missing required fields)
- **VALID-002:** Validation error (invalid field format)
- **SYSTEM-001:** Unexpected server error

## Database Schema (Phase 1)

### Users Table
```sql
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(100) UNIQUE NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  full_name VARCHAR(150) NOT NULL,
  role VARCHAR(50) DEFAULT 'USER',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Additional Tables (Schema Complete, Implementation in Future Phases)
- `pens` - Pen/enclosure management
- `pigs` - Individual pig tracking
- `feedings` - Feeding schedule records
- `health_records` - Health checkup and treatment records
- `sales` - Sales transaction records
- `mortality_records` - Death records

## Frontend Specification

### Pages
1. **Registration Page** (`/register`)
   - Form fields: username, email, password, full name
   - Client-side validation
   - Success message and auto-redirect to login

2. **Login Page** (`/login`)
   - Form fields: email, password
   - Stores JWT token in localStorage
   - Redirects to dashboard on success

3. **Dashboard** (`/dashboard`)
   - Displays user information
   - Protected route (requires authentication)
   - Logout button

### Frontend Base URL
```
http://localhost:5173
```

## Security Requirements
1. **Password Storage:** All passwords hashed with BCrypt (10 salt rounds)
2. **Authentication:** JWT token-based authentication
3. **CORS:** Configured for localhost development
4. **Input Validation:** Both client-side and server-side validation
5. **Error Messages:** Generic error messages to prevent information leakage

## Non-Functional Requirements
1. **Performance:** API response time < 500ms
2. **Scalability:** Cloud database (Supabase) for easy scaling
3. **Maintainability:** Clean code structure, proper layering (Controller → Service → Repository)
4. **Security:** Industry-standard password hashing and token-based auth

## Future Phases Roadmap
- **Phase 2:** Pen and Pig Management
- **Phase 3:** Feeding Schedule and Health Records
- **Phase 4:** Sales and Mortality Tracking
- **Phase 5:** Mobile Application (Android)
- **Phase 6:** Reports and Analytics

## Screenshots Location
All Phase 1 screenshots are stored in:
```
docs/screenshots/WEB/
docs/screenshots/MOBILE/
```

**Required Screenshots:**
- Registration page
- Successful registration
- Login page
- Successful login/dashboard
- Supabase database showing user records

---

**Document Control:**
- Last Updated: March 7, 2026
- Phase: 1 (User Registration and Authentication)
- Status: Complete and Tested
