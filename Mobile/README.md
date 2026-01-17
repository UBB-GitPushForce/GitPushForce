# 📱 GitPushForce — Mobile Application

This folder contains the **mobile client** of the GitPushForce project — a **cross-platform budgeting application** built using **Kotlin Multiplatform Mobile (KMM)**. The app provides native Android and iOS experiences while sharing core business logic between platforms.

The mobile app supports:

- User authentication (login/register)
- Expense tracking
- Budget and category management
- Group expense sharing
- Receipt scanning
- Analytics and statistics
- Profile management

---

## 🏗 Architecture Overview

The project follows a **clean layered architecture**:

UI Layer → ViewModels → Repositories → Network/Data Layer → Shared Logic
It uses:

- **Kotlin Multiplatform Mobile (KMM)** for shared logic
- **MVVM pattern** on Android
- **Retrofit + OkHttp** for networking
- **Jetpack Compose + XML layouts** on Android
- **SwiftUI** on iOS

---

## 📁 Project Structure
Mobile/  
├── androidApp/ # Native Android application module  
├── iosApp/ # Native iOS application module  
├── shared/ # Shared Kotlin Multiplatform business logic  
├── gradle/ # Gradle version catalog and wrapper configuration  
├── build.gradle.kts # Root Gradle configuration  
├── settings.gradle.kts # Multi-module project setup  
├── gradlew # Unix Gradle wrapper  
├── gradlew.bat # Windows Gradle wrapper  
├── config.properties # Environment-specific configuration values  
└── README.md # Project documentation

---

# 🤖 Android Application (`androidApp/`)

The Android module contains all native UI, networking, and state management logic.

---

## Core Configuration

| File | Description |
------|------------
`androidApp/build.gradle.kts` | Android module build configuration and dependencies
`AndroidManifest.xml` | App permissions, activities registration, and entry points

---

## 🚀 Application Entry Points

| File | Purpose |
------|--------
`MainActivity.kt` | Main launcher activity and navigation host
`SplashActivity.kt` | Splash screen displayed during startup
`LoginActivity.kt` | Login flow entry screen
`SignUpActivity.kt` | User registration activity
`MyApplicationTheme.kt` | App-wide theme and UI styling

---

## 📦 Data Layer

### Authentication & Local Storage

| File | Description |
------|------------
`TokenHolder.kt` | In-memory access token manager
`TokenDataStore.kt` | Persistent token storage using Android DataStore

---

### Network Models (API DTOs)

These files define how backend data is serialized/deserialized.

| File | Purpose |
------|--------
`LoginRequest.kt` | Login API request body
`LoginResponse.kt` | Login response mapping
`RegisterRequest.kt` | Registration payload
`RegisterResponse.kt` | Registration response mapping
`UserData.kt` | User profile data model
`BudgetResponse.kt` | Budget information response
`Expense.kt` | Expense entity representation
`Category.kt` | Expense category model
`Receipt.kt` | Receipt data structure
`Group.kt` | Group entity model
`GroupStatistics.kt` | Aggregated group analytics
`AnalyticsModels.kt` | Analytics chart data models
`ApiResponse.kt` | Generic API wrapper response

---

### Network Services

| File | Description |
------|------------
`RetrofitClient.kt` | Retrofit configuration and HTTP client setup
`TokenAuthInterceptor.kt` | Automatically injects auth tokens into requests
`AuthApiService.kt` | Authentication endpoints
`ExpenseApiService.kt` | Expense CRUD endpoints
`CategoryApiService.kt` | Category management API
`GroupApiService.kt` | Group-related endpoints
`ReceiptApiService.kt` | Receipt scanning endpoints
`UserApiService.kt` | Profile and user management API

---

### Repository Layer

Repositories handle business logic and API communication.

| File | Responsibility |
------|---------------
`AuthRepository.kt` | Authentication operations
`ExpenseRepository.kt` | Expense data handling
`CategoryRepository.kt` | Category logic
`GroupRepository.kt` | Group operations
`UserRepository.kt` | Profile management
`ExpensePaymentRepository.kt` | Group expense payment handling

---

## 🎨 UI Layer

### Reusable Components

| File | Description |
------|------------
`ExpenseItem.kt` | Expense list UI item
`BottomAddExpenseBar.kt` | Bottom action bar for adding expenses
`GroupRow.kt` | Group list UI row
`ExpenseBubble.kt` | Expense visualization bubble
`GroupPieChart.kt` | Group statistics chart
`TimelineItem.kt` | Timeline event item
`CreateGroupDialog.kt` | Group creation dialog
`JoinGroupDialog.kt` | Join group popup
`ExpensePickerDialog.kt` | Expense category picker

---

### Screens

| Screen | Function |
-------|---------
`MainScreen.kt` | Root navigation screen
`LoginScreen.kt` | Authentication UI
`SignUpScreen.kt` | Registration UI
`ExpensesScreen.kt` | Expense list and filters
`CategoriesScreen.kt` | Category management
`GroupsScreen.kt` | Group overview
`GroupDetailsScreen.kt` | Detailed group view
`AnalyticsScreen.kt` | Charts and analytics
`ReceiptScreen.kt` | Receipt scanning interface
`ProfileScreen.kt` | User profile management
`QrCaptureActivity.kt` | QR scanner for receipt/group sharing

---

### Utilities

| File | Purpose |
------|--------
`DateUtils.kt` | Date formatting helpers
`GroupUtils.kt` | Group calculation utilities

---

## 🧠 ViewModels

Implements the MVVM architecture for state management.

Each ViewModel has a corresponding Factory.

Examples:

| File | Responsibility |
------|---------------
`ExpenseViewModel.kt` | Expense screen state
`GroupsViewModel.kt` | Groups list logic
`AnalyticsViewModel.kt` | Statistics data management
`ProfileViewModel.kt` | Profile editing state
`ReceiptViewModel.kt` | Receipt scanning logic

---

## ⚠ Error Handling

| File | Purpose |
------|--------
`BaseErrorHandler.kt` | Shared error handling logic
`ErrorHandler.kt` | Generic error parser
`GroupsErrorHandler.kt` | Group-specific error mapping
`RegistrationErrorHandler.kt` | Signup validation errors

---

## 🎨 Resources

| File | Description |
------|------------
`main_layout.xml` | Root layout container
`strings.xml` | App string resources
`styles.xml` | App styling and themes
`file_paths.xml` | File provider configuration for camera and storage

---

# 🍎 iOS Application (`iosApp/`)

The iOS client uses **SwiftUI** and consumes shared Kotlin logic.

| File | Description |
------|------------
`iOSApp.swift` | Application entry point
`ContentView.swift` | Root SwiftUI view
`Info.plist` | iOS app configuration
`Assets.xcassets` | App icons and colors
`project.pbxproj` | Xcode project configuration

---

# 🔁 Shared Module (`shared/`)

This module contains logic shared between Android and iOS.

| File | Description |
------|------------
`Greeting.kt` | Sample shared logic example
`Platform.kt` | Platform abstraction interface
`Platform.android.kt` | Android-specific implementation
`Platform.ios.kt` | iOS-specific implementation
`shared/build.gradle.kts` | Shared module configuration

---

# ⚙ Setup Instructions

## Prerequisites

- Android Studio (Giraffe+)
- Xcode (latest)
- JDK 17+
- Kotlin Multiplatform plugin
- Gradle

---

## ▶ Run Android App
./gradlew androidApp:installDebug

Or run directly from Android Studio.

---

## ▶ Run iOS App

1. Open: iosApp/iosApp.xcodeproj
2. Select simulator/device  
3. Press ▶ Run

---

# 🧩 Tech Stack

- Kotlin Multiplatform Mobile
- Jetpack Compose
- Retrofit + OkHttp
- MVVM Architecture
- SwiftUI
- Gradle Version Catalog

---

# 🚀 Highlights

✅ Shared business logic  
✅ Native UI on both platforms  
✅ Clean architecture separation  
✅ Token-based authentication  
✅ Modular scalable structure  
✅ Production-ready API integration  

---

## FunFact :P
iOS part is the default generated code, the project uses the KMM structure and it allows for further developement of this mobile app
