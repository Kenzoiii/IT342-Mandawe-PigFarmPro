# Task Checklist

## DONE
 - Repository setup and folder structure initialized (commit: 67641a5)
- Spring Boot backend setup (commit: cc0f526)
- Implemented: POST /api/auth/register (commit: cc0f526)
- Implemented: POST /api/auth/login (commit: cc0f526)
- Implemented: GET /api/user/me (protected) (commit: cc0f526)
- Password encryption using BCrypt (commit: cc0f526)
- MySQL database integration (commit: cc0f526)
- ReactJS web application setup (Register, Login, Dashboard, Logout) (commit: cc0f526)
- FRS documentation update (Web screenshots only) (commit: cc0f526)

- Added logout endpoint `/api/auth/logout` with token blacklist (commit: 2f7ce706)
- Tightened token validation and protected access checks (commit: 2f7ce706)
- Improved input validation and HTTP status codes for auth (commit: 2f7ce706)
 - Scaffolded Android Kotlin mobile app (Register, Login, Dashboard, Logout) (commit: c4ddfe6)
 - Updated README and FRS.md to include mobile and logout docs (commit: c4ddfe6)

 - Create Mobile App and connect to backend (commit: 6edc7d3)
 - Verify emulator and device connectivity to backend (commit: 6edc7d3)
 - Capture and insert mobile and updated web screenshots into `docs/FRS.pdf` (commit: 6edc7d3)

 - Web dashboard UI redesign with real data integration (commit: df734b4)
 - Added pens tab UI with create and edit flows (commit: df734b4)
 - Added pen details view with recently added pigs (commit: df734b4)
 - Added pigs table view with search and filters (commit: df734b4)
 - Implemented pig CRUD modals and wiring (commit: df734b4)
 - Backend: dashboard, pen, and pig controllers with DTOs (commit: df734b4)
 - Updated pig card sizing and layout (commit: df734b4)

## IN-PROGRESS
 - None

## TODO
 - Create clean and proper UI for WEB and MOBILE

## PHASE 2 UPDATE (Mobile Development)
### Completed
- Mobile registration implemented with required fields: Name, Email, Password
- Mobile login implemented with email/password authentication
- Input validation added on mobile screens
- Backend API integration completed using Retrofit
- Successful login redirection to dashboard implemented
- Invalid login handling and message display implemented
- Token persistence using SharedPreferences implemented
- Android build verification passed (`assembleDebug`)


 
