# 🚀 GitPushForce - Comprehensive Project Overview

> A sophisticated **group expense tracking and management platform** developed collaboratively by 10 aspiring young developers over one semester. GitPushForce demonstrates enterprise-grade architecture, cloud-native deployment, and modern full-stack development practices spanning backend APIs, mobile applications, web interfaces, and AI integration.

## 🍿 Demos

☁️ [AWS Deployment](https://www.youtube.com/watch?v=gEzzmqddzkM)

🌐 [Web application](https://www.youtube.com/watch?v=DO9jlpzVwhs)

📱 [Mobile application](https://youtu.be/oyi1c3QhoVc)

---

## 📋 Table of Contents

1. [Executive Summary](#executive-summary)
2. [Project Architecture](#project-architecture)
3. [Technology Stack](#technology-stack)
4. [Core Modules](#core-modules)
5. [Database Design](#database-design)
6. [Backend API](#backend-api)
7. [Frontend Web Application](#frontend-web-application)
8. [Mobile Application](#mobile-application)
9. [AI & Receipt Processing](#ai--receipt-processing)
10. [CI/CD Pipeline](#cicd-pipeline)
11. [Cloud Infrastructure](#cloud-infrastructure)
12. [Getting Started](#getting-started)
13. [Team & Contribution](#team--contribution)

---

## Executive Summary

GitPushForce Team developed a **full-stack group expense management application** that enables users to create shared expense groups, track spending, split costs intelligently, and manage group finances seamlessly. The platform is built with modern technologies and deployed on AWS with enterprise-grade security, scalability, and reliability.

### Key Features

✅ **User Management** - Secure authentication, profile management, budget tracking  
✅ **Group Expense Tracking** - Create groups, manage memberships, track shared expenses  
✅ **Expense Splitting** - Intelligent cost distribution and payment status tracking  
✅ **Receipt Processing** - Upload receipts with OCR and AI extraction  
✅ **Analytics & Reporting** - Real-time dashboards, spending analytics, payment summaries  
✅ **Multi-Platform Support** - Web, Android, and iOS applications  
✅ **Production Ready** - AWS deployment, CI/CD automation, comprehensive testing  

---

## Project Architecture

The GitPushForce architecture follows a **microservices-oriented, cloud-native design** with clear separation of concerns:

<img width="1193" height="911" alt="Image" src="https://github.com/user-attachments/assets/94721196-ab06-4f0e-9246-b9633c427abf" />

### Architectural Tiers

**API Tier**: FastAPI backend running on EC2 Auto Scaling Group with Application Load Balancer  
**Serverless Tier**: AWS Lambda for receipt processing and AI analysis  
**Data Tier**: PostgreSQL RDS with automated backups and replication  
**Edge Tier**: CloudFront CDN for static asset distribution  

---

## Technology Stack

### Backend

| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| **Framework** | FastAPI | Latest | High-performance async API framework |
| **Language** | Python | 3.9+ | Server-side logic and business rules |
| **Database** | PostgreSQL | 14+ | Primary relational data store |
| **ORM** | SQLAlchemy | 2.x | Database abstraction and query building |
| **Auth** | JWT + Cookies | - | Token-based authentication |
| **Validation** | Pydantic | - | Request/response validation |
| **Testing** | pytest | - | Unit and integration test framework |
| **Linting** | Ruff | Latest | Fast Python linter for code quality |

### Frontend (Web)

| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| **Framework** | React | 18.2.0 | Component-based UI library |
| **Language** | TypeScript | 4.9.3 | Type-safe JavaScript |
| **Build Tool** | Vite | 4.5.14 | Lightning-fast development server |
| **HTTP Client** | Axios | 1.13.1 | API communication |
| **UI Framework** | Bootstrap | 5.3.8 | Responsive CSS framework |
| **Charts** | Recharts | 3.4.1 | Data visualization library |
| **State Management** | Context API | - | Global state with React Context |

### Mobile

| Platform | Technology | Purpose |
|----------|-----------|---------|
| **Shared Logic** | Kotlin Multiplatform | Code sharing across iOS/Android |
| **Android** | Jetpack Compose, XML | Native Android UI |
| **iOS** | SwiftUI | Native iOS UI |
| **Networking** | Retrofit + OkHttp | HTTP client for mobile |
| **Architecture** | MVVM | Model-View-ViewModel pattern |

### Cloud & DevOps

| Service | Component | Purpose |
|---------|-----------|---------|
| **Compute** | EC2 + Auto Scaling Group | Application server infrastructure |
| **Load Balancing** | Application Load Balancer | Traffic distribution and high availability |
| **Database** | RDS (PostgreSQL) | Managed relational database |
| **CDN** | CloudFront | Edge caching and content delivery |
| **Serverless** | Lambda + API Gateway | AI receipt processing |
| **Storage** | S3 | Static web assets hosting |
| **DNS** | Route53 | Domain registration and DNS management |
| **Security** | ACM, WAF, Shield | SSL/TLS, DDoS, and attack protection |
| **Secrets** | Parameter Store + KMS | Secure credential management |
| **Monitoring** | CloudWatch | Logging and performance metrics |
| **CI/CD** | GitHub Actions | Automated testing and deployment |

---

## Core Modules

### 1. API Backend (`/API`)

The FastAPI backend serves as the core business logic engine for the entire platform.

**Key Responsibilities:**
- User authentication and session management
- Group creation and membership management
- Expense tracking and payment status updates
- Category management and expense filtering
- Group activity logging and audit trails
- Budget tracking and spending analytics

**Architecture Pattern**: Routes → Services → Repositories → Database

- **Routes** parse HTTP requests, extract JWT tokens, and delegate to services
- **Services** contain business logic, validation, and permission enforcement
- **Repositories** handle pure database CRUD operations
- **Database** layer uses SQLAlchemy ORM for PostgreSQL access

**Key Endpoints:**
- `POST /auth/register` - User registration
- `POST /auth/login` - User authentication
- `GET /auth/me` - Get authenticated user
- `POST /groups/` - Create expense group
- `POST /expenses/` - Log new expense
- `POST /expenses/{id}/pay/{user_id}` - Mark payment status
- `GET /expenses/all` - Retrieve expenses with advanced filtering
- `POST /categories/` - Create expense categories
- `POST /receipts/process-receipt` - Upload and analyze receipt image

**Testing:**
- Unit test coverage requirement: 70% minimum
- Integration tests with Docker Compose for full application stack
- Automated testing on every commit via GitHub Actions

---

### 2. Web Frontend (`/Web/React`)

A modern, responsive single-page application built with React and TypeScript for desktop and tablet users.

**Architecture Overview:**
- **UI Components** - Reusable React components for every feature
- **Context API** - Global state management for auth, theme, currency
- **Service Layer** - Encapsulated API communication
- **Custom Hooks** - Shared logic utilities

**Core Features:**

| Feature | Components | Purpose |
|---------|-----------|---------|
| **Authentication** | LoginForm, RegisterForm | Secure user login and registration |
| **Dashboard** | Dashboard | Central hub with budget summary and quick access |
| **Group Management** | Groups, GroupDetail | Create, join, and manage expense groups |
| **Expense Tracking** | ReceiptsView, ReceiptsManual, ReceiptsUpload | Log and manage expenses |
| **Receipt Processing** | ReceiptsCamera, ReceiptsUpload | Capture or upload receipt images |
| **Analytics** | Data, Charts | Visualize spending patterns with Recharts |
| **Categories** | Categories | Organize expenses with custom categories |
| **Profile** | Profile | User settings, currency preferences, budget limits |
| **AI Assistant** | ChatBot | Get expense suggestions and financial advice |

**Global State Management:**
- `AuthContext` - User authentication and session state
- `CurrencyContext` - Currency preference (RON/EUR) with exchange rate caching
- `ThemeContext` - Light/dark mode preferences

**Service Layer:**
- `auth-service.ts` - Login, register, logout, session management
- `group-service.ts` - Group CRUD operations and membership
- `receipt-service.ts` - Receipt upload and OCR processing
- `category-service.ts` - Custom category management
- `expense-service.ts` - Expense filtering and analytics
- `exchange-rate.ts` - Real-time RON/EUR conversion

**Responsive Design:**
- Bootstrap 5.3.8 for responsive grid and components
- CSS variables for theming (light/dark modes)
- Mobile-optimized layouts for tablets

---

### 3. Mobile Application (`/Mobile`)

A cross-platform mobile solution using **Kotlin Multiplatform Mobile** for code sharing while maintaining native UIs on both platforms.

**Architecture:**
```
UI Layer (Native: Compose/SwiftUI)
         ↓
ViewModels (MVVM Pattern)
         ↓
Repositories
         ↓
Network Services (Retrofit/OkHttp)
         ↓
Shared Business Logic (KMM)
         ↓
Data Models & Validation
```

**Android Application (`androidApp/`):**

Native Android experience with **Jetpack Compose** and XML layouts.

**Key Activities & Screens:**
- `SplashActivity` - Initial loading screen
- `LoginActivity` - User authentication UI
- `MainActivity` - Primary navigation hub
- `ExpensesScreen` - Expense list with filtering
- `GroupsScreen` - Group overview and management
- `AnalyticsScreen` - Spending visualizations
- `ReceiptScreen` - Receipt scanning and upload
- `ProfileScreen` - User settings and preferences

**Data Layer:**
- `TokenDataStore` - Secure token persistence using Android DataStore
- `RetrofitClient` - HTTP configuration with OkHttp interceptors
- `TokenAuthInterceptor` - Automatic JWT token injection
- Network models for all API responses (LoginRequest, Expense, Category, etc.)

**ViewModels:**
- `ExpenseViewModel` - Expense list state and filtering
- `GroupsViewModel` - Group management logic
- `AnalyticsViewModel` - Statistics and chart data
- `ProfileViewModel` - User settings state

**iOS Application (`iosApp/`):**

Native iOS experience with **SwiftUI** framework, sharing business logic via KMM.

- `iOSApp.swift` - SwiftUI application entry point
- `ContentView.swift` - Root navigation view
- Native integration with shared Kotlin logic
- Planned for further development with rich feature parity

---

### 4. Database Schema (`/Database`)

A well-designed relational schema supporting complex expense sharing scenarios.

**Core Tables:**

| Table | Purpose | Key Columns |
|-------|---------|------------|
| **USER** | User accounts and authentication | id, email, hashed_password, budget, created_at |
| **GROUP** | Expense sharing groups | id, name, invitation_code (unique) |
| **EXPENSE** | Individual expense records | id, user_id, group_id (nullable), amount, category_id, created_at |
| **CATEGORY** | Expense categories (user + system) | id, user_id (nullable), title, keywords (array) |
| **EXPENSEPAYMENT** | Payment status tracking | expense_id, user_id (composite PK), paid_at |
| **USERGROUP** | Group membership join table | user_id, group_id (composite PK, M:N) |
| **GROUPLOG** | Activity audit trail | id, group_id, user_id, action (JOIN/LEAVE), created_at |

**Relationship Diagram:**
```
USER (1) ──── (M) EXPENSE
         ├──── (M) CATEGORY
         ├──── (M) USERGROUP ──── (M) GROUP
         └──── (M) GROUPLOG

GROUP (1) ──── (M) GROUPLOG
       ├──── (M) EXPENSE
       └──── (M) USERGROUP

EXPENSE (1) ──── (M) EXPENSEPAYMENT
```

**Key Design Features:**
- **Composite Primary Keys** - UserGroup and ExpensePayment use composite PKs for uniqueness
- **Nullable Foreign Keys** - Expense.group_id is nullable (supports personal and shared expenses)
- **Cascading Deletes** - User/Group deletion cascades to related records
- **Unique Constraints** - Email (login identifier), invitation_code (join codes)
- **Audit Trail** - GROUPLOG captures all membership changes
- **Array Types** - Category.keywords stored as PostgreSQL arrays for tagging

**Validation Constraints:**
- Email format validation (regex)
- Expense amount > 0 check
- GroupLog.action enum (JOIN/LEAVE only)
- Phone number numeric validation

---

## Backend API

The FastAPI backend provides a comprehensive REST API for all platform operations.

### Authentication Endpoints

| Method | Endpoint | Purpose | Returns |
|--------|----------|---------|---------|
| POST | `/auth/register` | Register new user | User data + JWT token |
| POST | `/auth/login` | Authenticate user | User data + JWT token (httponly cookie) |
| GET | `/auth/me` | Get current user | User profile |
| POST | `/auth/logout` | Clear session | Logout confirmation |

Authentication uses **JWT tokens** stored in httponly cookies with a 3-day expiration.

### User Management Endpoints

| Method | Endpoint | Purpose | Auth Required |
|--------|----------|---------|-------------|
| GET | `/users/` | Get all users | JWT |
| GET | `/users/{user_id}` | Get user details | JWT |
| PUT | `/users/{user_id}` | Update profile | JWT |
| DELETE | `/users/{user_id}` | Delete account | JWT |
| GET | `/users/{user_id}/budget` | Get budget limit | JWT |
| PUT | `/users/{user_id}/budget` | Set budget limit | JWT |
| GET | `/users/{user_id}/spent-this-month` | Current spending | JWT |
| GET | `/users/{user_id}/remaining-budget` | Budget remaining | JWT |

### Group Management Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/groups/` | Create new group |
| GET | `/groups/` | List all groups (paginated) |
| GET | `/groups/{group_id}` | Get group details |
| PUT | `/groups/{group_id}` | Update group |
| DELETE | `/groups/{group_id}` | Delete group |
| POST | `/groups/{group_id}/users/{user_id}` | Add user to group |
| DELETE | `/groups/{group_id}/leave` | Leave group (authenticated) |
| GET | `/groups/{group_id}/users` | Get group members |
| GET | `/groups/{group_id}/expenses` | Get group expenses |
| GET | `/groups/{group_id}/invite-qr` | Generate QR invite code |
| GET | `/groups/{group_id}/statistics/user-summary` | User's group stats |

### Expense Management Endpoints

| Method | Endpoint | Purpose | Filters |
|--------|----------|---------|---------|
| POST | `/expenses/` | Create expense | - |
| GET | `/expenses/all` | List all expenses | offset, limit, sort_by, order, min_price, max_price, date_from, date_to, category |
| GET | `/expenses/` | User's expenses | group_ids (filter by groups) |
| GET | `/expenses/group/{group_id}` | Group expenses | Same filters |
| PUT | `/expenses/{expense_id}` | Update (creator only) | - |
| DELETE | `/expenses/{expense_id}` | Delete (creator only) | - |

### Payment Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/expenses/{id}/pay/{user_id}` | Mark user as paid |
| DELETE | `/expenses/{id}/pay/{user_id}` | Unmark payment |
| GET | `/expenses/{id}/payments` | Get all payment statuses |

### Category Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/categories/` | Create custom category |
| GET | `/categories/` | List all categories |
| GET | `/categories/{user_id}` | User's categories |
| PUT | `/categories/{id}` | Update category |
| DELETE | `/categories/{id}` | Delete category |

### Receipt Processing

| Method | Endpoint | Purpose | Technology |
|--------|----------|---------|-----------|
| POST | `/receipts/process-receipt` | Upload and extract receipt | AWS Lambda + OCR |

Processes receipt images to automatically extract expense data using AI and computer vision.

### Group Activity Logging

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/group-logs/{group_id}` | Activity history (JOIN/LEAVE events) |

---

## Frontend Web Application

### Component Hierarchy

```
App.tsx (Root Router)
├── LoginForm (Auth)
├── RegisterForm (Auth)
├── Dashboard (Home)
│   ├── Budget Summary
│   ├── Recent Transactions
│   └── Navigation Menu
├── Groups
│   ├── GroupList
│   └── GroupDetail
│       ├── Members List
│       ├── Expenses List
│       └── Invite Code
├── Receipts (Hub)
│   ├── ReceiptsView
│   ├── ReceiptsManual
│   ├── ReceiptsUpload
│   └── ReceiptsCamera
├── Categories
│   └── CustomCategories
├── Data (Analytics)
│   └── Charts & Statistics
├── Profile
│   ├── Settings
│   ├── Currency Preference
│   └── Budget Limits
├── ChatBot (AI)
│   └── Expense Advice
└── ThemeToggle (Dark/Light)
```

### Service Layer Details

**Authentication Flow:**
1. User enters credentials
2. `auth-service.login()` calls `/auth/login`
3. JWT token stored in httponly cookie automatically
4. `AuthContext` updated with user data
5. Protected routes accessible

**Expense Creation Flow:**
1. User selects expense type (upload, manual, camera)
2. Image/data processed by `receipt-service` if needed
3. Categories auto-tagged via `category-service` with AI keywords
4. Expense submitted via `expense-service`
5. Dashboard automatically refreshed

**Group Joining:**
1. User provides invitation code
2. `group-service.joinGroup(code)` validates
3. User added to group membership
4. Group activity logged
5. Access to group expenses granted

### Styling & Theming

- **Bootstrap 5.3.8** for responsive layouts
- **CSS Variables** in `App.css` for theme switching
- **Dark Mode** toggle via `ThemeContext`
- **Currency Formatting** with `CurrencyContext` (RON/EUR)
- **Real-time Exchange Rates** cached locally

---

## Mobile Application

### Kotlin Multiplatform Structure

The mobile app shares core business logic while maintaining native UIs:

```
Mobile/
├── androidApp/          # Native Android (Jetpack Compose)
│   ├── Activities
│   ├── ViewModels
│   ├── UI Components
│   ├── Network Models
│   ├── Repositories
│   └── Interceptors
├── iosApp/              # Native iOS (SwiftUI)
│   └── [Native Implementation]
└── shared/              # Shared Kotlin Logic
    ├── Platform.kt
    ├── Domain Models
    └── Validation
```

### Android Features

**Authentication:**
- Login/Register screens
- Token persistence via DataStore
- Automatic token injection via OkHttp interceptor

**Expense Management:**
- Create/edit/delete expenses
- Filter by date, category, group
- Real-time synchronization

**Group Features:**
- Create groups with unique codes
- Join via invitation code or QR scanner
- View group members and statistics
- Track shared expenses

**Receipt Scanning:**
- Camera integration for receipt capture
- Image upload with OCR processing
- Automatic field extraction

**Analytics:**
- Pie charts for category breakdown
- Timeline view of transactions
- Group spending summary

**User Experience:**
- MVVM architecture for clean code
- Repository pattern for data access
- Factory pattern for ViewModel creation
- Error handling with specific error classes
- Theme support (light/dark mode)

### iOS Development

Currently built on the KMM generated structure, providing:
- SwiftUI interface
- Shared business logic from KMM
- Native iOS navigation patterns
- Ready for feature expansion

---

## AI & Receipt Processing

### Receipt Analyzer Lambda Function

The receipt processing service is built as an **AWS Lambda function** triggered via **API Gateway**.

**Architecture:**

```
Mobile App / Web
     ↓
API Gateway (HTTP endpoint)
     ↓
AWS Lambda (Serverless)
     ↓
AWS Textract / Vision API
     ↓
Response: Extracted Data
```

**Processing Pipeline:**
1. Receipt image uploaded to Lambda via API Gateway
2. AWS Textract analyzes image for text extraction
3. ML models extract expense details (amount, date, vendor, items)
4. Data validated and normalized
5. JSON response returned to client

**Extracted Fields:**
- Merchant/vendor name
- Transaction date
- Total amount
- Line items with prices
- Payment method
- Tax information

**Integration Points:**
- Mobile app calls `/receipts/process-receipt` endpoint
- Web app integrates via `receipt-service.ts`
- Backend stores extracted data in expense records

**Serverless Benefits:**
- No infrastructure to manage
- Automatic scaling for spikes
- Pay-per-invocation pricing
- CloudWatch logging integration

---

## CI/CD Pipeline

The project implements a **multi-stage automated CI/CD pipeline** using **GitHub Actions** to ensure code quality on every commit and pull request.

### Pipeline Architecture

```
Code Push / Pull Request
        ↓
┌───────────────────────┐
│  Stage 1: LINT       │
│  Ruff Linting        │
│  (Code Quality)      │
└───────────┬───────────┘
            ↓ (Pass)
┌───────────────────────┐
│  Stage 2: UNIT TESTS │
│  pytest Suite        │
│  (Functionality)     │
└───────────┬───────────┘
            ↓ (Pass)
┌───────────────────────┐
│  Stage 3: COVERAGE   │
│  Coverage Check      │
│  (Min 70% Threshold) │
└───────────┬───────────┘
            ↓ (Pass)
┌───────────────────────┐
│  Stage 4: INTEGRATION│
│  Docker Compose      │
│  Full Stack Test     │
│  Health Checks       │
└───────────┬───────────┘
            ↓ (Pass)
    ✅ MERGE APPROVED
```

### Stage 1: Linting (Ruff)

**Tool:** Ruff - A Rust-based Python linter for speed and accuracy

**Rules Enforced:**
- **E101-E115** - Indentation consistency
- **E225-E228** - Operator spacing
- **E301-E303** - Blank line consistency
- **E401-E402** - Import organization
- **E701-E743** - Code clarity (no lambda assignments, ambiguous names)
- **E902** - IO error handling
- **F** - PyFlakes rules (undefined variables, unused imports)
- **N801/N806** - Naming conventions
- **I001** - Import sorting

**Sample Violations Caught:**
```python
# ❌ FAIL: Ambiguous variable
i = 10

# ❌ FAIL: Missing spaces around operator
x=10+5

# ❌ FAIL: Multiple statements on one line
x = 1; y = 2

# ✅ PASS: Proper formatting
my_variable = 10 + 5
```

### Stage 2: Unit Tests

**Framework:** pytest

**Test Coverage:**
- Service layer business logic
- Repository database operations
- Request/response validation
- Error handling and edge cases
- Authentication and authorization

**Running Tests Locally:**
```bash
pytest --cov=src --cov-report=html
```

### Stage 3: Coverage Check

**Requirement:** Minimum 70% code coverage

- Ensures critical paths tested
- Prevents regression through untested code
- Calculated on changed files + important modules

### Stage 4: Integration Tests

**Setup:** Docker Compose with full stack

**Components Tested:**
- FastAPI backend on port 8000
- PostgreSQL database connection
- Redis cache (if used)
- Health checks on all services
- End-to-end API workflows

**Example Workflow Test:**
```
1. Start docker-compose stack
2. Wait for service health checks
3. Register test user
4. Create group
5. Add expense
6. Verify database entry
7. Shutdown services
```

### Continuous Feedback

- **Pull Requests** - All checks must pass before merge
- **Branch Protection** - Main branch requires passing checks
- **Status Badges** - Display CI status in README
- **Failure Notifications** - Email/Slack on failure

---

## Cloud Infrastructure

GitPushForce is deployed on **Amazon AWS** with a production-grade architecture emphasizing reliability, security, and scalability.

### AWS Architecture Overview

```
Internet Users
       │
       ↓
   Route53 (DNS)
   └─→ Domain Resolution
       │
       ↓
    CloudFront (CDN)
    ├─→ Static Assets from S3
    └─→ API requests to ALB
       │
       ├───────────────────────┐
       │                       │
       ↓                       ↓
   ALB (Load Balancer)    API Gateway
   └─→ EC2 ASG          └─→ Lambda
       └─→ FastAPI        └─→ AI Receipt
           └─→ RDS            Processor
                             └─→ KMS
       │
       ↓
   CloudWatch (Monitoring)
   SNS (Notifications)
   WAF (Protection)
   Shield (DDoS)
```

### Service Descriptions

#### 1. **Route53 (DNS & Domain Registrar)**
- Domain registration and management
- Hosted Zone DNS configuration
- Failover and health-check routing
- Low-latency routing policies

#### 2. **CloudFront (CDN)**
- Edge caching closer to users
- Reduced latency for global users
- Origin Access Control for S3 bucket
- HTTPS enforcement

#### 3. **Application Load Balancer (ALB)**
- Distributes traffic across EC2 instances
- Stable DNS endpoint (never changes)
- Health check monitoring
- SSL/TLS termination

#### 4. **EC2 Auto Scaling Group (ASG)**
- Automatically scales based on demand
- Health-check-based instance replacement
- CloudWatch metric-based scaling policies
- Maintains desired instance count

**Launch Template Configuration:**
- FastAPI application
- Environment variables from Parameter Store
- IAM role for S3/Database access
- CloudWatch agent for log collection

#### 5. **RDS PostgreSQL**
- Managed relational database
- Automatic patching and minor version upgrades
- Multi-AZ replication for high availability
- Automated backups (35-day retention)
- Encryption at rest (KMS)
- VPC security group isolation

#### 6. **S3 (Static Asset Storage)**
- Stores compiled React web app (dist folder)
- Static CSS, JavaScript, HTML files
- Cheap and durable object storage
- Access only through CloudFront OAC
- Versioning and lifecycle policies enabled

#### 7. **API Gateway + AWS Lambda**
- Serverless receipt processing service
- Triggered by HTTP POST requests
- Integrates with AWS Textract/Vision APIs
- Returns JSON response to clients
- Scales automatically

**Benefits:**
- No infrastructure management
- Pay only for invocations
- Built-in throttling and quotas
- CloudWatch integration for monitoring

#### 8. **Parameter Store (Secrets Management)**
- Stores database credentials securely
- Stores API keys and tokens
- Encryption with KMS
- EC2 instances assume IAM role for access
- No secrets in code or environment files

#### 9. **Key Management Service (KMS)**
- Manages encryption keys
- Automatic key rotation
- Encrypts Parameter Store values
- Encrypts RDS database at rest
- Audit trail of all key usage

#### 10. **CloudWatch**
- **Logs** - Application and system logs from EC2 and Lambda
- **Metrics** - CPU usage, memory, network, custom metrics
- **Alarms** - Triggers SNS notifications for issues
- **Dashboards** - Real-time visualization of health

**Monitored Metrics:**
- EC2 CPU utilization (target: avg < 50%)
- ALB request count and latency
- RDS connections and query performance
- Lambda invocation count and duration

#### 11. **SNS (Simple Notification Service)**
- Sends notifications on ASG events (instance launch/termination)
- Notifies admins on CloudWatch alarms
- Email notifications for critical events
- Can integrate with Slack/Teams

#### 12. **Web Application Firewall (WAF)**
- Layer 7 (Application) protection
- Rules against:
  - XSS (Cross-Site Scripting)
  - SQL Injection
  - CSRF attacks
  - Malformed requests
- IP whitelisting/blacklisting

#### 13. **Shield**
- Layer 3/4 (Network) DDoS protection
- Handles volumetric attacks
- Stateless protection
- Standard (automatic) + Advanced (optional)

#### 14. **Certificate Manager (ACM)**
- Manages SSL/TLS certificates
- HTTPS encryption in flight
- Free certificates for AWS domains
- Auto-renewal before expiration
- Used by CloudFront, ALB, API Gateway

#### 15. **GuardDuty (Currently Disabled)**
- Machine learning-based threat detection
- Analyzes VPC DNS traffic
- Reviews CloudTrail logs
- Identifies suspicious activity patterns
- Can be enabled for enhanced security monitoring

### Scalability & High Availability

**Auto Scaling Configuration:**
- Desired capacity: Adjustable based on demand
- Scale-out trigger: CPU > 70% or custom metrics
- Scale-in trigger: CPU < 30% for sustained period
- Cool-down period: Prevents rapid scaling oscillation

**Database High Availability:**
- Multi-AZ replication (synchronous)
- Automatic failover to standby in different AZ
- RTO (Recovery Time Objective): < 5 minutes
- RPO (Recovery Point Objective): < 1 second

**Backup Strategy:**
- Automated daily snapshots
- 35-day retention period
- Cross-region replication available
- Point-in-time recovery capability

### Security Posture

**Network Security:**
- VPC isolation
- Security Groups (stateful firewall)
- Network ACLs (stateless firewall)
- Private subnets for database
- Public subnets for ALB/NAT

**Data Security:**
- Encryption at rest (RDS, S3, KMS)
- Encryption in transit (TLS/HTTPS)
- Credentials never in code
- Parameter Store for secrets
- IAM roles for service-to-service auth

**Access Control:**
- IAM roles and policies (least privilege)
- VPC endpoints for private AWS service access
- SSL certificate for domain
- WAF for application-layer attacks

---

## Getting Started

### Prerequisites

- **Node.js** 14+ (for web frontend)
- **Python** 3.9+ (for backend API)
- **Docker & Docker Compose** (for local development)
- **JDK 17+** (for mobile development)
- **Xcode** (for iOS development)
- **Android Studio** (for Android development)
- **AWS Account** (for cloud deployment)
- **Git** (for version control)

### Local Development Setup

#### 1. Backend API

```bash
# Navigate to API directory
cd API

# Create virtual environment
python -m venv .venv
source .venv/bin/activate  # Linux/macOS
# or
.venv\Scripts\activate  # Windows

# Install dependencies
pip install -r requirements.txt

# Configure database
# Follow instructions in database.py

# Run the API
uvicorn main:app --reload
```

API available at: `http://localhost:8000`  
Swagger docs: `http://localhost:8000/docs`

#### 2. Web Frontend

```bash
# Navigate to web directory
cd Web/React

# Install dependencies
npm install

# Start development server
npm run dev
```

Web app available at: `http://localhost:5173`

#### 3. Mobile Development

**Android:**
```bash
cd Mobile

# Run on emulator
./gradlew androidApp:installDebug

# Or open in Android Studio and run
```

**iOS:**
```bash
# Open in Xcode
open iosApp/iosApp.xcodeproj

# Select simulator/device and run
```

#### 4. Using Docker Compose

```bash
# Build and start all services
docker-compose up -d

# Services available:
# - API: http://localhost:8000
# - Web: http://localhost:5173
# - Database: localhost:5432
```

---

## Team & Contribution

### Project Organization

**Development Team:** 10 developers  
**Project Duration:** One semester  
**Collaboration:** GitHub, Discord, Agile meetings  

### Repository Structure

```
GitPushForce/
├── API/                 # FastAPI backend
├── Web/React/           # React web application
├── Mobile/              # Kotlin Multiplatform mobile app
├── Database/            # Schema and migrations
├── .github/workflows/   # CI/CD pipelines
├── docs/                # Documentation
└── README.md            # Main project documentation
```

### Contributing Guidelines

1. **Branching** - Create feature branches from `main`
2. **Naming** - Use `feature/`, `bugfix/`, `hotfix/` prefixes
3. **Code Quality** - Pass all CI/CD checks before merging
4. **Testing** - Add tests for new features
5. **Documentation** - Update README for significant changes
6. **Commits** - Use descriptive commit messages
7. **Pull Requests** - Get approval from at least 1 reviewer

### Development Workflow

```
1. Create feature branch
   git checkout -b feature/my-feature

2. Make changes and commit
   git add .
   git commit -m "description"

3. Push to repository
   git push origin feature/my-feature

4. Create Pull Request
   - Description of changes
   - Link related issues
   - Request reviewers

5. CI/CD Validation
   - Linting (Ruff)
   - Unit tests (pytest)
   - Coverage check (70%)
   - Integration tests

6. Approval & Merge
   - Code review feedback
   - Approve after discussion
   - Squash merge to main

7. Auto-Deployment
   - Deployment triggered on main branch
   - Services updated automatically
```

---

## Project Highlights

### ✨ Technical Excellence

- **Clean Architecture** - Clear separation of concerns across layers
- **Type Safety** - TypeScript frontend, Python type hints, Kotlin types
- **Comprehensive Testing** - 70% minimum test coverage with automated checks
- **Scalable Design** - Auto-scaling infrastructure, modular code organization
- **Security First** - Encryption, authentication, input validation, audit trails
- **Cloud Native** - AWS best practices, serverless where appropriate, managed services

### 🎯 Feature Completeness

- **Full User Lifecycle** - Registration, authentication, profile management
- **Complex Business Logic** - Group management, expense splitting, payment tracking
- **Multiple Interfaces** - Web, Android, iOS with shared business logic
- **Analytics & Insights** - Spending patterns, category breakdown, budget tracking
- **AI Integration** - Receipt analysis, automatic expense extraction
- **Real-time Sync** - Expenses instantly visible across devices

### 📊 Production Readiness

- **Automated CI/CD** - 4-stage pipeline ensuring quality
- **High Availability** - Multi-AZ deployment, automatic failover
- **Monitoring & Alerts** - CloudWatch metrics, SNS notifications
- **Disaster Recovery** - Automated backups, cross-region replication
- **Performance** - CDN caching, database optimization, efficient APIs
- **Compliance** - Data encryption, audit logs, secure credential management

### 🚀 Enterprise Architecture

- **Microservices Ready** - Modular components that can scale independently
- **API-First Design** - Multiple clients (web, mobile) consume single API
- **Containerized** - Docker support for consistent environments
- **Infrastructure as Code** - Cloud resources defined and versioned
- **Observability** - Comprehensive logging, metrics, and tracing

---

## Conclusion

GitPushForce represents a sophisticated, production-grade full-stack application demonstrating expertise across:

- **Backend Development** - FastAPI, PostgreSQL, API design
- **Frontend Engineering** - React, TypeScript, UI/UX design
- **Mobile Development** - Kotlin Multiplatform, native iOS/Android
- **Cloud Architecture** - AWS services, high availability, security
- **DevOps & Automation** - CI/CD pipelines, containerization, monitoring
- **Software Engineering** - Clean code, testing, documentation

The project showcases the ability to take a concept from design to production deployment, managing complexity across multiple platforms and infrastructure layers while maintaining code quality, security, and scalability.

---

## Quick Links

- **GitHub Repository** - https://github.com/UBB-GitPushForce/GitPushForce
- **API Documentation** - `/API/README.md`
- **Database Schema** - `/Database/README.md`
- **Web Frontend** - `/Web/React/README.md`
- **Mobile App** - `/Mobile/README.md`
- **Cloud Deployment** - `/Cloud-deployment.docx`
- **CI/CD Pipeline** - `.github/workflows/`

---

**Last Updated:** January 2026  
**Project Status:** Production Ready ✅  
**Maintenance:** Active Development
