# Vertical Slice Architecture - PigFarmPro

## Overview
This document describes the refactoring from a layer-based architecture to a vertical slice architecture for the PigFarmPro system.

## Why Vertical Slice Architecture?
- **Better Modularity**: Each feature is self-contained
- **Improved Maintainability**: Changes to a feature are isolated
- **Easier Testing**: Each slice can be tested independently
- **Faster Development**: Teams can work on slices in parallel
- **Clear Responsibility**: Each slice has well-defined boundaries

## Architecture Overview

### Current (Layer-Based)
```
Backend:
├── Controllers/
├── Services/
├── Repositories/
├── DTOs/
└── Models/

Web:
├── Pages/
├── Components/
└── API/

Mobile:
├── Activities/
├── Fragments/
└── ViewModels/
```

### New (Vertical Slice-Based)
```
Backend:
├── authentication/
│   ├── AuthController.java
│   ├── AuthService.java
│   ├── User.java
│   ├── UserRepository.java
│   └── AuthResponse.java, LoginRequest.java, RegisterRequest.java
│
├── userManagement/
│   ├── UserController.java
│   ├── UserService.java
│   └── User.java (shared with auth)
│
├── pigManagement/
│   ├── PigController.java
│   ├── PigService.java
│   ├── Pig.java
│   ├── PigRepository.java
│   ├── CreatePigRequest.java
│   └── UpdatePigRequest.java
│
├── penManagement/
│   ├── PenController.java
│   ├── PenService.java
│   ├── Pen.java
│   ├── PenRepository.java
│   ├── CreatePenRequest.java
│   └── UpdatePenRequest.java
│
├── feedingManagement/
│   ├── FeedingController.java
│   ├── FeedingService.java
│   ├── Feeding.java
│   ├── FeedingRepository.java
│   ├── CreateFeedingRequest.java
│   └── UpdateFeedingRequest.java
│
├── healthRecords/
│   ├── HealthRecordController.java
│   ├── HealthRecordService.java
│   ├── HealthRecord.java
│   ├── HealthRecordRepository.java
│   └── CreateHealthRecordRequest.java
│
├── mortalityRecords/
│   ├── MortalityRecordController.java
│   ├── MortalityRecordService.java
│   ├── MortalityRecord.java
│   ├── MortalityRecordRepository.java
│   └── CreateMortalityRecordRequest.java
│
├── salesManagement/
│   ├── SaleController.java
│   ├── SaleService.java
│   ├── Sale.java
│   ├── SaleRepository.java
│   └── CreateSaleRequest.java
│
├── dashboard/
│   ├── DashboardController.java
│   ├── DashboardService.java
│   └── DashboardResponse.java
│
├── config/
│   ├── SecurityConfig.java
│   ├── TokenProvider.java
│   └── TokenBlacklist.java
│
└── common/
    ├── ApiResponse.java
    └── PublicController.java

Web:
├── src/features/
│   ├── authentication/
│   │   ├── pages/
│   │   │   ├── Login.jsx
│   │   │   └── Register.jsx
│   │   └── api.js
│   │
│   ├── dashboard/
│   │   ├── pages/Dashboard.jsx
│   │   ├── components/
│   │   └── api.js
│   │
│   ├── pigManagement/
│   │   ├── pages/Pigs.jsx
│   │   ├── components/
│   │   └── api.js
│   │
│   ├── penManagement/
│   │   ├── pages/Pens.jsx
│   │   ├── components/PenDetails.jsx
│   │   └── api.js
│   │
│   ├── feedingManagement/
│   │   ├── pages/Feeding.jsx
│   │   └── api.js
│   │
│   ├── shared/
│   │   └── styles.css
│   │
│   └── App.jsx

Mobile:
├── app/src/main/java/com/pigfarmpro/
│   ├── features/
│   │   ├── authentication/
│   │   │   ├── LoginActivity.kt
│   │   │   ├── RegisterActivity.kt
│   │   │   └── AuthViewModel.kt
│   │   │
│   │   ├── dashboard/
│   │   │   ├── DashboardActivity.kt
│   │   │   └── DashboardViewModel.kt
│   │   │
│   │   └── ...
│   │
│   └── common/
│       ├── RetrofitClient.kt
│       └── TokenManager.kt
```

## Vertical Slices Defined

### 1. Authentication Slice
**Responsibility**: User registration, login, logout, and token management
- Backend: `AuthController`, `AuthService`
- Web: `Login.jsx`, `Register.jsx`
- Mobile: `LoginActivity`, `RegisterActivity`
- Database: `User` table
- API: `/api/auth/register`, `/api/auth/login`, `/api/auth/logout`

### 2. User Management Slice
**Responsibility**: User profile, information retrieval
- Backend: `UserController`, `UserService`
- Web: Dashboard profile section
- Mobile: Profile screen (future)
- API: `/api/user/me`, `/api/user/profile`

### 3. Pig Management Slice
**Responsibility**: CRUD operations for pigs
- Backend: `PigController`, `PigService`, `Pig` entity
- Web: `Pigs.jsx`, pig modals
- Mobile: Pig list screen
- API: `/api/pigs/*`

### 4. Pen Management Slice
**Responsibility**: CRUD operations for pens
- Backend: `PenController`, `PenService`, `Pen` entity
- Web: `Pens.jsx`, `PenDetails.jsx`
- Mobile: Pen list and details
- API: `/api/pens/*`

### 5. Feeding Management Slice
**Responsibility**: Feeding schedule and records
- Backend: `FeedingController`, `FeedingService`, `Feeding` entity
- Web: `Feeding.jsx`
- Mobile: Feeding schedule
- API: `/api/feedings/*`

### 6. Health Records Slice
**Responsibility**: Health monitoring and records
- Backend: `HealthRecordController`, `HealthRecordService`, `HealthRecord` entity
- Web: Health tab
- Mobile: Health records
- API: `/api/health-records/*`

### 7. Mortality Records Slice
**Responsibility**: Mortality tracking
- Backend: `MortalityRecordController`, `MortalityRecordService`, `MortalityRecord` entity
- Web: Mortality tab
- Mobile: Mortality records
- API: `/api/mortality-records/*`

### 8. Sales Management Slice
**Responsibility**: Sales tracking
- Backend: `SaleController`, `SaleService`, `Sale` entity
- Web: Sales tab
- Mobile: Sales records
- API: `/api/sales/*`

### 9. Dashboard Slice
**Responsibility**: Aggregated user data and statistics
- Backend: `DashboardController`, `DashboardService`
- Web: `Dashboard.jsx`
- Mobile: Dashboard screen
- API: `/api/user/dashboard`

## Cross-Cutting Concerns (Shared)
- **Security**: `SecurityConfig`, `TokenProvider`, `TokenBlacklist`
- **Common DTOs**: `ApiResponse`
- **Global Configuration**: `BackendApplication`

## Benefits Achieved

| Aspect | Layer-Based | Vertical Slice |
|--------|-------------|----------------|
| **Modularity** | Low - features scattered | High - feature contained |
| **Maintainability** | Difficult - changes span layers | Easy - isolated changes |
| **Testing** | Complex - many dependencies | Simple - clear boundaries |
| **Development Speed** | Slower - layer navigation | Faster - feature focus |
| **Onboarding** | Steep - understand full stack | Easy - understand one slice |
| **Scaling** | Hard - entangled | Easy - add new slices |

## Migration Path
1. Create package structure for each slice
2. Move controllers, services, entities, and DTOs to respective slices
3. Update imports and references
4. Verify compilation and tests pass
5. Commit and push to branch
6. Run regression tests
