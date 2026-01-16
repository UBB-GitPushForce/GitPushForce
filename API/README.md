# API Documentation

## Overview
This is a group expense tracking backend built with FastAPI. 
It allows users to create groups, track shared expenses, manage payments, and view statistics.

## Core Functionality

**What the backend DOES:**
- User authentication (register, login, logout)
- Group creation and management
- Expense tracking within groups
- Payment status management
- Receipt processing with image upload
- User budget tracking
- Category management for expenses
- Group activity logging
- Expense filtering and sorting

---

## Architecture

Client → Routes (HTTP Entry) → Services (Business Logic) → Repositories (Database Access) → Database

**Routes** - Parse HTTP requests, extract JWT tokens, delegate to services. No business logic.

**Services** - Validate inputs, apply business rules, enforce permissions, orchestrate repositories.

**Repositories** - Pure database operations (CRUD). No logic, only data access.

---

## API Routes

### Authentication

**POST /auth/register**
- Register a new user
- Body: email, password, name
- Returns: user data + access token

**POST /auth/login**
- Login with credentials
- Body: email, password
- Returns: user data + access token (also sets httponly cookie)
- Cookie max_age: 3 days

**GET /auth/me**
- Get current authenticated user
- Requires: JWT token
- Returns: user data

**POST /auth/logout**
- Clear authentication cookie
- Returns: logout confirmation

**POST /auth/password-reset/request**
- Request password reset (disabled in current code)

**POST /auth/password-reset/confirm**
- Confirm password reset with token
- Body: token, new_password
- Returns: reset confirmation

### Users

**GET /**
- Get all users
- Requires: JWT token
- Returns: list of all users

**GET /{user_id}**
- Get specific user by ID
- Requires: JWT token
- Returns: user data

**PUT /{user_id}**
- Update user profile
- Requires: JWT token
- Body: UserUpdate schema
- Returns: updated user data

**PUT /password/change**
- Change password for authenticated user
- Requires: JWT token + old password verification
- Body: old_password, new_password
- Returns: password change confirmation

**DELETE /{user_id}**
- Delete user account
- Requires: JWT token
- Returns: deletion confirmation

**POST /join-group/{invitation_code}**
- Join a group using invitation code
- Requires: JWT token
- Path param: invitation_code
- Returns: group join confirmation

**GET /{user_id}/budget**
- Get user's budget limit
- Requires: JWT token
- Returns: budget amount

**PUT /{user_id}/budget**
- Update user's budget limit
- Requires: JWT token
- Body: new_budget (integer)
- Returns: updated budget

**GET /{user_id}/spent-this-month**
- Get amount spent this month
- Requires: JWT token
- Returns: spending amount

**GET /{user_id}/remaining-budget**
- Get remaining budget (budget - spent)
- Requires: JWT token
- Returns: remaining amount

### Groups

**POST /**
- Create new group
- Body: GroupCreate schema
- Returns: created group data

**GET /**
- Get all groups with pagination
- Query params: offset (default 0), limit (default 100)
- Returns: list of groups

**GET /{group_id}**
- Get specific group by ID
- Returns: group data

**PUT /{group_id}**
- Update group information
- Body: GroupUpdate schema
- Returns: updated group data

**DELETE /{group_id}**
- Delete a group
- Returns: deletion confirmation

**POST /{group_id}/users/{user_id}**
- Add user to group
- Path params: group_id, user_id
- Returns: confirmation + triggers group log

**DELETE /{group_id}/leave**
- Leave a group (authenticated user removes themselves)
- Requires: JWT token
- Returns: confirmation + triggers group log

**DELETE /{group_id}/users/{user_id}**
- Remove user from group (admin action)
- Path params: group_id, user_id
- Returns: confirmation + triggers group log

**GET /user/{user_id}**
- Get all groups a user belongs to
- Returns: list of user's groups

**GET /{group_id}/users**
- Get all members of a group
- Returns: list of users in group

**GET /{group_id}/users/nr**
- Get count of users in group
- Returns: number of users

**GET /{group_id}/expenses**
- Get expenses for a group with pagination and sorting
- Query params: offset, limit, sort_by (default created_at), order (asc/desc)
- Returns: list of group expenses

**GET /{group_id}/invite-qr**
- Generate QR code for group invitation
- Returns: QR code data

**GET /{group_id}/statistics/user-summary**
- Get statistics for authenticated user within specific group
- Requires: JWT token
- Returns: user expense summary for that group

### Expenses

**POST /**
- Create new expense
- Requires: JWT token (user_id extracted from token, not body)
- Body: ExpenseCreate schema
- Returns: created expense data

**GET /all**
- Get all expenses system-wide with advanced filtering
- Query params:
  - offset (default 0, min 0)
  - limit (default 100, range 1-1000)
  - sort_by (default created_at)
  - order (asc or desc, default desc)
  - min_price (optional, min 0)
  - max_price (optional, min 0)
  - date_from (optional, parsed datetime string)
  - date_to (optional, parsed datetime string)
  - category (optional, string)
- Returns: paginated list of expenses

**GET /{expense_id}**
- Get specific expense by ID
- Returns: expense data

**GET /**
- Get expenses for authenticated user with filtering
- Requires: JWT token
- Query params: same as /all, plus group_ids (optional list of group IDs)
- Returns: user's expenses across groups

**GET /group/{group_id}**
- Get expenses for specific group with filtering
- Query params: same as /all
- Returns: group expenses

**PUT /{expense_id}**
- Update expense (only allowed if requester is creator)
- Requires: JWT token
- Body: ExpenseUpdate schema
- Returns: updated expense data

**DELETE /{expense_id}**
- Delete expense (only allowed if requester is creator)
- Requires: JWT token
- Returns: deletion confirmation

### Expense Payments

**POST /{expense_id}/pay/{payer_id}**
- Mark a user as paid for an expense
- Requires: JWT token (requester must be expense creator)
- Path params: expense_id, payer_id
- Returns: payment confirmation

**DELETE /{expense_id}/pay/{payer_id}**
- Unmark a user as paid (remove payment status)
- Requires: JWT token (requester must be expense creator)
- Path params: expense_id, payer_id
- Returns: unmark confirmation

**GET /{expense_id}/payments**
- Get all payment statuses for an expense
- Requires: JWT token
- Path param: expense_id
- Returns: list of who has paid

### Categories

**POST /**
- Create custom expense category
- Requires: JWT token
- Body: CategoryCreate schema
- Returns: created category

**GET /**
- Get all categories (system-wide)
- Requires: JWT token
- Query params: sort_by (default title), order (asc/desc, default asc)
- Returns: list of categories

**GET /{user_id}**
- Get categories for specific user
- Requires: JWT token
- Query params: sort_by, order
- Returns: user's categories

**PUT /{category_id}**
- Update category (only if requester is creator)
- Requires: JWT token
- Body: CategoryUpdate schema
- Returns: updated category

**DELETE /{category_id}**
- Delete category (only if requester is creator)
- Requires: JWT token
- Returns: deletion confirmation

### Receipts

**POST /process-receipt**
- Upload and process receipt image
- Requires: JWT token
- Body: multipart form-data with image file
- Returns: extracted expense data from receipt

### Group Logs

**GET /{group_id}**
- Get activity log for a group (join/leave events)
- Requires: JWT token (user must be group member)
- Path param: group_id
- Returns: list of group activity logs

---

## Setup & Running

1. Navigate to project directory:
```
cd GitPushForce/API
```

2. Create virtual environment:
```
python -m venv .venv
```

3. Activate virtual environment:

Windows (PowerShell):
```
.venv\Scripts\Activate.ps1
```

Windows (cmd):
```
.venv\Scripts\activate
```

Linux/macOS:
```
source .venv/bin/activate
```

4. Install dependencies:
```
pip install --upgrade pip
pip install -r requirements.txt
```

5. Configure database:
Follow instructions in database.py file

6. Review examples:
Check example.py for repository usage patterns

7. Run the API:
```
uvicorn main:app --reload
```

The API will be available at http://localhost:8000
Swagger docs at http://localhost:8000/docs
ReDoc docs at http://localhost:8000/redoc