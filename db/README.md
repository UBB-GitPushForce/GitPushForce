# Database Schema & Object Relationships

## Visual Table Structure

### USER
```
┌──────────────────────────────────┐
│ USER                             │
├──────────────────────────────────┤
│ id (PK)                          │
│ first_name                       │
│ last_name                        │
│ email (UNIQUE)                   │
│ hashed_password                  │
│ phone_number                     │
│ budget (nullable)                │
│ created_at                       │
│ updated_at                       │
└──────────────────────────────────┘
         ▲             ▲
         │             │
    1:M  │             │  M:N
         │             │
  ┌──────┴─────┐  ┌────┴──────────┐
  │ EXPENSE    │  │ USERGROUP     │
  │ CATEGORY   │  │ (join table)  │
  │ GROUPLOG   │  └───────┬───────┘
  └────────────┘          │
                          ▼
                      GROUP
```

### GROUP
```
┌──────────────────────────────────┐
│ GROUP                            │
├──────────────────────────────────┤
│ id (PK)                          │
│ name                             │
│ description (nullable)           │
│ invitation_code (UNIQUE)         │
│ created_at                       │
└──────────────────────────────────┘
         │          │
      1:M │          │ M:N
         │          │
    ┌────▼───┐  ┌───┴──────────┐
    │EXPENSE │  │ USERGROUP    │
    │GROUPLOG│  │ (join table) │
    └────────┘  └──────────────┘
```

### EXPENSE
```
┌──────────────────────────────────┐
│ EXPENSE                          │
├──────────────────────────────────┤
│ id (PK)                          │
│ user_id (FK) NOT NULL            │
│ group_id (FK) nullable           │
│ title                            │
│ amount (> 0)                     │
│ description                      │
│ category_id (FK) NOT NULL        │
│ created_at                       │
└──────────────────────────────────┘
         │              │
      1:M │              │ 1:M
         │              │
    ┌────▼──────┐   ┌───┴────────────┐
    │ USER      │   │ CATEGORY       │
    │ GROUP     │   │ EXPENSEPAYMENT │
    └───────────┘   └────────────────┘
```

### CATEGORY
```
┌──────────────────────────────────┐
│ CATEGORY                         │
├──────────────────────────────────┤
│ id (PK)                          │
│ user_id (FK) nullable            │
│ title (max 30 chars)             │
│ keywords (array)                 │
└──────────────────────────────────┘
         │
      1:M │
         │
    ┌────▼───────┐
    │ EXPENSE    │
    └────────────┘
```

### EXPENSEPAYMENT
```
┌──────────────────────────────────┐
│ EXPENSEPAYMENT                   │
├──────────────────────────────────┤
│ expense_id (FK, PK)              │
│ user_id (FK, PK)                 │
│ paid_at                          │
│ [Composite PK: (exp_id,user_id)] │
└──────────────────────────────────┘
    FK──┤      │───FK
       │       │
    ┌──▼──┐ ┌──▼──┐
    │EXP  │ │USER │
    └─────┘ └─────┘
```

### USERGROUP (Join Table)
```
┌──────────────────────────────────┐
│ USERGROUP                        │
├──────────────────────────────────┤
│ user_id (FK, PK)                 │
│ group_id (FK, PK)                │
│ [Composite PK: (user_id,group_id)]
└──────────────────────────────────┘
    FK──┤      │───FK
       │       │
    ┌──▼───┐ ┌──▼────┐
    │USER  │ │GROUP  │
    └──────┘ └───────┘
```

### GROUPLOG
```
┌──────────────────────────────────┐
│ GROUPLOG                         │
├──────────────────────────────────┤
│ id (PK)                          │
│ group_id (FK)                    │
│ user_id (FK)                     │
│ action ('JOIN' or 'LEAVE')       │
│ created_at                       │
└──────────────────────────────────┘
    FK──┤      │───FK
       │       │
    ┌──▼────┐ ┌──▼──┐
    │GROUP  │ │USER │
    └───────┘ └─────┘
```

---

## Complete Schema Diagram

```
                               ┌─────────────┐
                               │   CATEGORY  │
                               │  (user_id)  │
                               └──────┬──────┘
                                      │
                                   1:M │
                                      │
    ┌─────────────────────────────────▼──────────────────────┐
    │                                                         │
    │                         EXPENSE                         │
    │                    (user_id, group_id FK)              │
    │                    (nullable: group_id)               │
    │                                                        │
    └─────────────────────────────────┬──────────────────────┘
              │                       │
           1:M │                      │ 1:M
              │                       │
    ┌─────────▼──────┐       ┌────────▼──────────────┐
    │      USER      │       │  EXPENSEPAYMENT      │
    │   (8 columns)  │       │  (composite PK)      │
    └────────┬───────┘       └──────────────────────┘
             │                       │
          M:N │                    1:M │
             │                        │
    ┌────────▼──────────────┐  ┌──────▼────┐
    │    USERGROUP          │  │    USER   │
    │  (join table, M:N)    │  │           │
    └────────┬──────────────┘  └───────────┘
             │
          M:N │
             │
    ┌────────▼──────────────┐
    │       GROUP           │
    │   (invitation_code)   │
    │                       │
    └────────┬──────────────┘
             │
          1:M │
             │
    ┌────────▼──────────────┐
    │    GROUPLOG           │
    │  (JOIN/LEAVE events)  │
    └───────────────────────┘
```

---

## Relationship Legend

```
(1) ──── (M)    One-to-Many relationship
         (direct FK)

(1) ◄──► (M)    Many-to-Many relationship
         (via join table)

FK              Foreign Key reference
PK              Primary Key
Composite PK    Multiple columns as primary key
```

---

## Data Type Summary

| Column Type | Used In | Example |
|-----------|---------|---------|
| INT (PK) | All tables | User.id, Group.id |
| INT (FK) | All relations | Expense.user_id |
| VARCHAR(n) | Strings | User.email, Group.name |
| TEXT | Long text | Expense.description |
| FLOAT | Numbers | Expense.amount, User.budget |
| ARRAY | Lists | Category.keywords |
| DATETIME TZ | Timestamps | created_at, updated_at |
| STRING(20) | Enums | GroupLog.action |
| BOOLEAN | Flags | (not used currently) |

---

## Cascade Behavior

```
USER deleted → CASCADE
├── Expense (user_id FK)
├── Category (user_id FK)
├── UserGroup (user_id FK)
└── GroupLog (user_id FK)

GROUP deleted → CASCADE
├── Expense (group_id FK)
├── UserGroup (group_id FK)
└── GroupLog (group_id FK)

EXPENSE deleted → CASCADE
├── ExpensePayment (expense_id FK)
└── (Expense can reference Category, but not vice versa)

CATEGORY deleted → CASCADE
└── Expense (category_id FK)
```

---

## NULL Handling

| Column | Nullable | Reason |
|--------|----------|--------|
| User.budget | YES | User can have no budget |
| Group.description | YES | Groups can be unnamed |
| Expense.group_id | YES | Expenses can be personal |
| Expense.description | YES | Expenses can have no details |
| Category.user_id | YES | System categories exist |

---

## Uniqueness Constraints

| Column | Unique? | Indexed? | Reason |
|--------|---------|----------|--------|
| User.email | YES | YES | Login identifier |
| Group.invitation_code | YES | YES | Join code must be unique |
| UserGroup.(user_id, group_id) | YES | YES | Prevent duplicate memberships |
| ExpensePayment.(expense_id, user_id) | YES | YES | One payment status per user per expense |

---

## Validation Constraints

| Column | Constraint | Type |
|--------|-----------|------|
| User.email | Format regex | `^[^\s@]+@[^\s@]+\.[^\s@]+$` |
| User.phone_number | Numeric only | `^[0-9]+$` |
| Expense.amount | Must be > 0 | CHECK amount > 0 |
| GroupLog.action | Enum values | CHECK action IN ('JOIN', 'LEAVE') |

---

## Index Strategy (Recommended)

```sql
-- Auth
CREATE INDEX idx_user_email ON users(email);

-- Expense queries
CREATE INDEX idx_expense_user_id ON expenses(user_id);
CREATE INDEX idx_expense_group_id ON expenses(group_id);
CREATE INDEX idx_expense_created_at ON expenses(created_at);

-- Membership queries
CREATE INDEX idx_usergroup_user_id ON users_groups(user_id);
CREATE INDEX idx_usergroup_group_id ON users_groups(group_id);

-- Payments
CREATE INDEX idx_expensepayment_expense_id ON expense_payments(expense_id);

-- Activity logs
CREATE INDEX idx_grouplog_group_id ON group_logs(group_id);
```

---

## Example Data State

```
USER (1001: john@example.com)
├── Categories:
│   ├── 1: "Food"
│   ├── 2: "Transport"
│   └── 3: "Entertainment"
│
├── Personal Expenses:
│   ├── E100: Coffee (category: Food)
│   └── E101: Gas (category: Transport)
│
├── Group Expenses:
│   ├── E200: Rent (group: Roommates, category: Housing)
│   └── E201: Utilities (group: Roommates, category: Housing)
│
└── Group Memberships:
    ├── Roommates (4 members)
    └── Vacation (2 members)


GROUP (G1: Roommates)
├── Members:
│   ├── User 1001
│   ├── User 1002
│   ├── User 1003
│   └── User 1004
│
├── Expenses:
│   ├── E200: Rent $1000
│   │   ├── Payment: User 1001 (paid_at: 2026-01-15)
│   │   ├── Payment: User 1002 (paid_at: 2026-01-15)
│   │   ├── Payment: User 1003 (paid_at: null)
│   │   └── Payment: User 1004 (paid_at: null)
│   │
│   └── E201: Utilities $80
│       ├── Payment: User 1001 (paid_at: 2026-01-16)
│       ├── Payment: User 1002 (paid_at: null)
│       ├── Payment: User 1003 (paid_at: null)
│       └── Payment: User 1004 (paid_at: null)
│
└── Activity Log:
    ├── 2026-01-10: User 1001 JOIN
    ├── 2026-01-11: User 1002 JOIN
    ├── 2026-01-12: User 1003 JOIN
    └── 2026-01-14: User 1004 JOIN
```

---

## Key Points Summary

✓ **7 interconnected tables** - User, Group, Category, Expense, ExpensePayment, UserGroup, GroupLog
✓ **2 composite primary keys** - UserGroup (M:N), ExpensePayment (payment tracking)
✓ **1 nullable FK** - Expense.group_id (personal vs. group)
✓ **Cascading deletes** - Protect referential integrity
✓ **Smart constraints** - Regex validation, amount checks, enum values
✓ **Audit trail** - GroupLog tracks all membership changes
✓ **Budget tracking** - User.budget for spending limits
✓ **Category organization** - User and system categories