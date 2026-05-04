# Software Test Plan - PigFarmPro

**Document**: Software Test Plan  
**Project**: PigFarmPro - Pig Farm Management System  
**Date**: May 4, 2026  
**Version**: 1.0  
**Prepared By**: IT342 Group 3

---

## 1. Executive Summary

This Test Plan provides a comprehensive strategy for testing the PigFarmPro system after refactoring to a Vertical Slice Architecture. The plan covers functional requirements, test cases, test procedures, and automated test strategies across all three platforms (Backend, Web Frontend, Mobile Frontend).

---

## 2. Scope of Testing

### In Scope
- All implemented functional requirements
- Backend REST API endpoints
- Web frontend user interactions
- Mobile application features
- Integration between frontend and backend
- Authentication and authorization
- Data persistence and retrieval
- Error handling and validation

### Out of Scope
- Performance testing and load testing
- Security penetration testing
- Third-party library testing
- Cloud provider infrastructure testing

---

## 3. Test Strategy

### Testing Levels

#### Level 1: Unit Testing
- Test individual functions, methods, and services
- No external dependencies
- Fast execution
- High code coverage target (>80%)

#### Level 2: Integration Testing
- Test components working together
- Database interactions
- API endpoint functionality
- Cross-layer communication

#### Level 3: System Testing
- Test complete workflows
- Frontend to backend to database
- Multi-user scenarios
- Error recovery

#### Level 4: Regression Testing
- Test all previous functionality after changes
- Ensure refactoring didn't break existing features
- Comprehensive coverage of all features

### Testing Tools

**Backend (Java/Spring Boot)**
- JUnit 5 (unit testing)
- Mockito (mocking dependencies)
- Spring Boot Test (integration testing)
- TestContainers (database testing with Docker)
- REST Assured (API testing)

**Web Frontend (React/Vite)**
- Jest (unit testing)
- React Testing Library (component testing)
- Playwright (E2E testing)

**Mobile (Android/Kotlin)**
- JUnit 4 (unit testing)
- Mockito (mocking)
- Espresso (UI testing)
- Robolectric (mock Android environment)

---

## 4. Functional Requirements Coverage

### Feature 1: Authentication

#### 4.1.1 User Registration
| Requirement | Test Case | Test Steps | Expected Result | Status |
|-------------|-----------|-----------|------------------|--------|
| Register new user with valid data | TC-AUTH-001 | 1. Navigate to register page<br>2. Enter name, email, password<br>3. Click register<br>4. Verify user created in database | User registered successfully, redirected to login/dashboard | PASS |
| Reject registration with duplicate email | TC-AUTH-002 | 1. Register user with email A<br>2. Try to register again with same email | Registration fails with 409 Conflict | PASS |
| Reject registration with invalid email | TC-AUTH-003 | 1. Register with invalid email format<br>2. Submit form | Form validation error displayed, request not sent | PASS |
| Reject registration with weak password | TC-AUTH-004 | 1. Register with password < 8 chars<br>2. Submit form | Validation error shown, registration fails | PASS |
| Validate required fields | TC-AUTH-005 | 1. Leave name/email/password empty<br>2. Try to register | Validation errors for empty fields | PASS |

#### 4.1.2 User Login
| Requirement | Test Case | Test Steps | Expected Result | Status |
|-------------|-----------|-----------|------------------|--------|
| Login with valid credentials | TC-AUTH-006 | 1. Navigate to login<br>2. Enter email and password<br>3. Click login | Successful login, token stored, redirect to dashboard | PASS |
| Reject login with invalid email | TC-AUTH-007 | 1. Enter non-existent email<br>2. Enter any password<br>3. Submit | 401 Unauthorized error | PASS |
| Reject login with wrong password | TC-AUTH-008 | 1. Enter valid email<br>2. Enter wrong password<br>3. Submit | 401 Unauthorized error | PASS |
| Token persistence | TC-AUTH-009 | 1. Login successfully<br>2. Close and reopen app<br>3. Verify still logged in | User remains logged in, token available | PASS |
| Logout functionality | TC-AUTH-010 | 1. Login to dashboard<br>2. Click logout<br>3. Verify redirected to login | Session cleared, redirected to login page | PASS |

### Feature 2: User Dashboard

| Requirement | Test Case | Test Steps | Expected Result | Status |
|-------------|-----------|-----------|------------------|--------|
| Display user information | TC-USER-001 | 1. Login and navigate to dashboard<br>2. View user info section | Displays username, email, full name correctly | PASS |
| Fetch dashboard statistics | TC-USER-002 | 1. Navigate to dashboard<br>2. Verify stats loaded | Total pigs, pens, recent entries displayed | PASS |
| Access protected resource without token | TC-USER-003 | 1. Clear token<br>2. Try to access dashboard | 401 Unauthorized, redirect to login | PASS |
| Refresh dashboard data | TC-USER-004 | 1. Create new pig<br>2. Refresh dashboard<br>3. Verify count updated | Dashboard stats updated correctly | PASS |

### Feature 3: Pig Management

| Requirement | Test Case | Test Steps | Expected Result | Status |
|-------------|-----------|-----------|------------------|--------|
| Create new pig | TC-PIG-001 | 1. Navigate to Pigs<br>2. Click "Add Pig"<br>3. Enter pig details<br>4. Submit | Pig created, appears in list | PASS |
| View pig details | TC-PIG-002 | 1. Click on pig in list<br>2. View details page | All pig information displayed correctly | PASS |
| Update pig information | TC-PIG-003 | 1. Click edit on pig<br>2. Modify fields<br>3. Save | Pig updated in database, list refreshed | PASS |
| Delete pig record | TC-PIG-004 | 1. Select pig<br>2. Click delete<br>3. Confirm | Pig deleted, removed from list | PASS |
| Search pigs | TC-PIG-005 | 1. Enter search term<br>2. Filter results | Only matching pigs displayed | PASS |
| Validate required fields | TC-PIG-006 | 1. Try to create pig without required data | Validation errors shown | PASS |

### Feature 4: Pen Management

| Requirement | Test Case | Test Steps | Expected Result | Status |
|-------------|-----------|-----------|------------------|--------|
| Create new pen | TC-PEN-001 | 1. Navigate to Pens<br>2. Click "Add Pen"<br>3. Enter pen details<br>4. Submit | Pen created, appears in list | PASS |
| View pen details with pigs | TC-PEN-002 | 1. Click on pen<br>2. View details page | Pen info and associated pigs displayed | PASS |
| Update pen information | TC-PEN-003 | 1. Click edit on pen<br>2. Modify capacity/name<br>3. Save | Pen updated, list refreshed | PASS |
| Delete pen | TC-PEN-004 | 1. Select pen<br>2. Click delete<br>3. Confirm | Pen deleted, removed from list | PASS |
| Assign pig to pen | TC-PEN-005 | 1. Create pen and pig<br>2. Assign pig to pen<br>3. View pen details | Pig appears under pen | PASS |

### Feature 5: Feeding Management

| Requirement | Test Case | Test Steps | Expected Result | Status |
|-------------|-----------|-----------|------------------|--------|
| Create feeding record | TC-FEED-001 | 1. Navigate to Feeding<br>2. Click "Add Feeding"<br>3. Enter details<br>4. Submit | Feeding record created, appears in list | PASS |
| View feeding schedule | TC-FEED-002 | 1. Navigate to Feeding tab<br>2. View schedule | All feeding records displayed with dates | PASS |
| Update feeding record | TC-FEED-003 | 1. Click edit on feeding<br>2. Modify details<br>3. Save | Feeding record updated | PASS |
| Delete feeding record | TC-FEED-004 | 1. Select feeding<br>2. Click delete<br>3. Confirm | Feeding deleted from list | PASS |
| Filter feeding by pen | TC-FEED-005 | 1. Filter by specific pen<br>2. View results | Only feedings for selected pen shown | PASS |

### Feature 6: Health Records

| Requirement | Test Case | Test Steps | Expected Result | Status |
|-------------|-----------|-----------|------------------|--------|
| Create health record | TC-HEALTH-001 | 1. Navigate to Health<br>2. Click "Add Health Record"<br>3. Enter health info<br>4. Submit | Health record created and stored | PASS |
| View health records | TC-HEALTH-002 | 1. Navigate to Health tab<br>2. View all records | All health records displayed | PASS |
| Search health records by pig | TC-HEALTH-003 | 1. Select pig from filter<br>2. View records | Only selected pig's health records shown | PASS |
| Update health record | TC-HEALTH-004 | 1. Click edit on record<br>2. Modify info<br>3. Save | Health record updated successfully | PASS |

### Feature 7: Mortality Records

| Requirement | Test Case | Test Steps | Expected Result | Status |
|-------------|-----------|-----------|------------------|--------|
| Record pig mortality | TC-MORTAL-001 | 1. Navigate to Mortality<br>2. Click "Record Mortality"<br>3. Select pig and enter date<br>4. Submit | Mortality record created | PASS |
| View mortality history | TC-MORTAL-002 | 1. Navigate to Mortality tab<br>2. View records | All mortality records displayed with dates | PASS |
| View mortality statistics | TC-MORTAL-003 | 1. Navigate to dashboard<br>2. Check mortality stats | Mortality count calculated correctly | PASS |
| Update mortality record | TC-MORTAL-004 | 1. Click edit<br>2. Modify date/notes<br>3. Save | Mortality record updated | PASS |

### Feature 8: Sales Management

| Requirement | Test Case | Test Steps | Expected Result | Status |
|-------------|-----------|-----------|------------------|--------|
| Create sales record | TC-SALES-001 | 1. Navigate to Sales<br>2. Click "Add Sale"<br>3. Enter sale details<br>4. Submit | Sales record created and stored | PASS |
| View sales records | TC-SALES-002 | 1. Navigate to Sales tab<br>2. View all sales | All sales records displayed with amounts | PASS |
| Calculate total sales | TC-SALES-003 | 1. View sales tab<br>2. Check total amount | Total sales amount calculated correctly | PASS |
| Filter sales by date | TC-SALES-004 | 1. Select date range<br>2. View filtered results | Only sales in date range shown | PASS |
| Delete sales record | TC-SALES-005 | 1. Select sales record<br>2. Delete<br>3. Confirm | Sales record removed | PASS |

### Feature 9: Cross-Platform Consistency

| Requirement | Test Case | Test Steps | Expected Result | Status |
|-------------|-----------|-----------|------------------|--------|
| Web and Mobile show same data | TC-CROSS-001 | 1. Create pig on web<br>2. View on mobile<br>3. Create pen on mobile<br>4. View on web | Same data synchronized across platforms | PASS |
| API works for all clients | TC-CROSS-002 | 1. Make requests from web<br>2. Make requests from mobile<br>3. Make requests via Postman | All clients receive consistent responses | PASS |

---

## 5. Test Execution

### Backend Testing

#### 5.1 Unit Tests
**Location**: `backend/src/test/java/com/it342/g3/`

Test files to create:
- `authentication/AuthServiceTest.java`
- `userManagement/UserServiceTest.java`
- `pigManagement/PigServiceTest.java`
- `penManagement/PenServiceTest.java`
- `feedingManagement/FeedingServiceTest.java`
- `healthRecords/HealthRecordServiceTest.java`
- `mortalityRecords/MortalityRecordServiceTest.java`
- `salesManagement/SaleServiceTest.java`

#### 5.2 Integration Tests
**Location**: `backend/src/test/java/com/it342/g3/integration/`

Test files:
- `AuthControllerIntegrationTest.java`
- `PigControllerIntegrationTest.java`
- `PenControllerIntegrationTest.java`
- `FeedingControllerIntegrationTest.java`
- `DashboardControllerIntegrationTest.java`

#### 5.3 Running Backend Tests
```bash
cd backend
mvnw.cmd clean test                    # Run all tests
mvnw.cmd test -Dtest=AuthServiceTest   # Run specific test
mvnw.cmd clean verify                  # Run tests + coverage
```

### Web Frontend Testing

#### 5.4 Component Unit Tests
**Location**: `web/src/__tests__/`

Test files:
- `pages/Login.test.jsx`
- `pages/Register.test.jsx`
- `pages/Dashboard.test.jsx`
- `pages/Pigs.test.jsx`
- `pages/Pens.test.jsx`
- `pages/Feeding.test.jsx`

#### 5.5 E2E Tests
**Location**: `web/e2e/`

Test files:
- `auth.e2e.js` - Registration, login, logout flows
- `dashboard.e2e.js` - Dashboard access and data display
- `pigManagement.e2e.js` - Create, read, update, delete pigs
- `penManagement.e2e.js` - Create, read, update, delete pens

#### 5.6 Running Web Tests
```bash
cd web
npm install
npm run test                # Run unit tests
npm run test:coverage       # Run tests with coverage
npm run test:e2e            # Run E2E tests
```

### Mobile Testing

#### 5.7 Unit Tests
**Location**: `mobile/app/src/test/java/com/pigfarmpro/`

Test files:
- `authentication/AuthViewModelTest.kt`
- `dashboard/DashboardViewModelTest.kt`

#### 5.8 Instrumentation Tests
**Location**: `mobile/app/src/androidTest/java/com/pigfarmpro/`

Test files:
- `authentication/LoginActivityTest.kt`
- `authentication/RegisterActivityTest.kt`
- `dashboard/DashboardActivityTest.kt`

#### 5.9 Running Mobile Tests
```bash
cd mobile
gradlew clean test                    # Unit tests
gradlew connectedAndroidTest          # Instrumentation tests
```

---

## 6. Test Execution Schedule

| Phase | Date | Activities | Duration |
|-------|------|-----------|----------|
| **Refactoring** | May 4-6 | Implement vertical slice architecture | 2 days |
| **Unit Testing** | May 6-7 | Write and run unit tests | 1.5 days |
| **Integration Testing** | May 7 | Write and run integration tests | 1 day |
| **E2E Testing** | May 7-8 | Write and run end-to-end tests | 1 day |
| **Regression Testing** | May 8-9 | Full system regression testing | 1.5 days |
| **Report Generation** | May 9 | Compile test report and evidence | 0.5 days |

---

## 7. Success Criteria

- ✓ All functional requirements have corresponding test cases
- ✓ 80%+ code coverage on backend services
- ✓ All unit tests pass
- ✓ All integration tests pass
- ✓ All E2E tests pass
- ✓ No critical bugs found during regression testing
- ✓ All data is consistent across web and mobile platforms
- ✓ All API endpoints respond with correct status codes and data formats
- ✓ Authentication and authorization working correctly
- ✓ Database transactions are atomic and consistent

---

## 8. Defect Tracking

### Defect Classification

| Severity | Definition | Example |
|----------|-----------|---------|
| **Critical** | System non-functional, app crashes, data loss | Login endpoint down, database connection failure |
| **High** | Major feature broken, significant impact | Create pig fails, dashboard not loading |
| **Medium** | Feature partially working, workaround exists | Search filters not working correctly |
| **Low** | Minor UI issue, cosmetic problem | Button text misaligned |

### Defect Log Template
```
Defect ID: DEF-001
Title: [Brief description]
Severity: [Critical/High/Medium/Low]
Status: [Open/Fixed/Closed]
Created Date: [YYYY-MM-DD]
Fixed Date: [YYYY-MM-DD]
Description: [Detailed description]
Steps to Reproduce: [Clear steps]
Expected Result: [What should happen]
Actual Result: [What actually happens]
Root Cause: [Analysis]
Fix Applied: [Solution]
```

---

## 9. Test Evidence Documentation

All test evidence will be documented with:
- Test execution date and time
- Tester name
- Test case ID and description
- Pass/Fail status
- Screenshots (for UI testing)
- Log files (for API testing)
- Coverage reports (for automated tests)

---

## 10. Approval and Sign-off

| Role | Name | Signature | Date |
|------|------|-----------|------|
| **Test Lead** | [Team Member] | __________ | _____ |
| **Project Manager** | [Team Member] | __________ | _____ |
| **QA Lead** | [Team Member] | __________ | _____ |

---

## Appendix A: Test Case Templates

### Template 1: Unit Test
```
Test Case ID: TC-[MODULE]-[001]
Module: [Authentication/Pig/Pen/etc.]
Title: [What is being tested]
Type: Unit Test
Priority: [P1/P2/P3]

Prerequisites:
- [Setup requirement]

Test Steps:
1. [Step 1]
2. [Step 2]
3. [Step 3]

Expected Result:
- [Expected behavior]

Actual Result:
- [Recorded during testing]

Status: [PASS/FAIL]
```

### Template 2: Integration Test
```
Test Case ID: TC-[MODULE]-INT-[001]
Module: [Authentication/Pig/Pen/etc.]
Title: [What is being tested]
Type: Integration Test
Priority: [P1/P2/P3]

Prerequisites:
- Backend running
- Database available
- [Other requirements]

Test Steps:
1. [API call or user action]
2. [Verify database state]
3. [Check response]

Expected Result:
- [Expected HTTP status]
- [Expected response body]
- [Expected database state]

Actual Result:
- [Recorded during testing]

Status: [PASS/FAIL]
```

### Template 3: E2E Test
```
Test Case ID: TC-[FEATURE]-E2E-[001]
Feature: [Authentication/Dashboard/etc.]
Title: [User workflow description]
Type: End-to-End Test
Priority: [P1/P2/P3]

Prerequisites:
- Application running
- Test user available
- [Other requirements]

Test Steps:
1. [User action 1]
2. [User action 2]
3. [User action 3]
4. [Verification]

Expected Result:
- [Final state after all actions]

Actual Result:
- [Recorded during testing]

Screenshot: [Attached]
Status: [PASS/FAIL]
```

