# Web Application

A modern React + TypeScript web interface for the expense sharing and group management application. This is the frontend component of the GitPushForce project.

## 📋 Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Features](#features)
- [Configuration](#configuration)
- [Development](#development)
- [Building](#building)

## 🎯 Overview

The Web application provides a user-friendly interface for managing shared expenses within groups. Users can:

- Create and manage groups
- Add expenses and split costs with group members
- Upload and process receipts
- Track spending and payments
- View expense history and analytics
- Manage user profiles and group memberships

The application uses **React** for the UI, **TypeScript** for type safety, **Vite** for fast development and building, and communicates with the backend API through service layers.

## 🛠️ Technology Stack

- **Framework**: React 18.2.0 with TypeScript
- **Build Tool**: Vite 4.5.14
- **HTTP Client**: Axios 1.13.1
- **UI Framework**: Bootstrap 5.3.8
- **Charts**: Recharts 3.4.1
- **Language**: TypeScript 4.9.3

## 📁 Project Structure

```
React/
├── src/
│   ├── components/          # React components
│   │   ├── LoginForm.tsx       — Formular de autentificare utilizator
│   │   ├── RegisterForm.tsx    — Formular de înregistrare cont nou
│   │   ├── Dashboard.tsx       — Pagina principală cu sumar buget și navigație
│   │   ├── Groups.tsx          — Listă grupuri utilizator, creare și alăturare grup
│   │   ├── GroupDetail.tsx     — Detalii grup: membri, cheltuieli, cod invitație
│   │   ├── Categories.tsx      — Gestionare categorii și keywords pentru auto-tagging
│   │   ├── Receipts.tsx        — Hub pentru încărcare/scanare/adăugare bonuri
│   │   ├── ReceiptsView.tsx    — Listă cheltuieli cu filtre, editare și ștergere
│   │   ├── ReceiptsManual.tsx  — Formular adăugare manuală cheltuială
│   │   ├── ReceiptsUpload.tsx  — Upload imagine bon pentru procesare OCR
│   │   ├── ReceiptsCamera.tsx  — Captură bon cu camera dispozitivului
│   │   ├── Profile.tsx         — Setări profil: nume, email, buget, monedă
│   │   ├── Data.tsx            — Vizualizări grafice și statistici cheltuieli
│   │   ├── ThemeToggle.tsx     — Buton comutare temă light/dark
│   │   └── ChatBot.tsx         — Asistent AI pentru întrebări și sugestii
│   ├── contexts/            # React Context providers
│   │   ├── AuthContext.tsx     — Stare autentificare: user, login, logout, token
│   │   ├── CurrencyContext.tsx — Preferință monedă (RON/EUR) și formatare sume
│   │   └── ThemeContext.tsx    — Preferință temă și toggle dark mode
│   ├── hooks/               # Custom React hooks
│   │   └── useAuth.ts          — Hook pentru acces rapid la AuthContext
│   ├── services/            # API service layer
│   │   ├── api-client.ts       — Instanță Axios configurată cu baseURL și cookies
│   │   ├── auth-service.ts     — Apeluri API: login, register, logout, getMe
│   │   ├── category-service.ts — CRUD categorii: create, update, delete, list
│   │   ├── group-service.ts    — Operații grupuri: create, join, members
│   │   ├── receipt-service.ts  — Upload și procesare bonuri
│   │   ├── exchange-rate.ts    — Curs valutar RON/EUR cu cache local
│   │   └── http-service.ts     — Serviciu HTTP generic cu interceptori
│   ├── App.tsx              # Root component — rutare principală și layout
│   ├── App.css              # Global styles — teme, variabile CSS, componente
│   ├── main.tsx             # Entry point — render React și providers
│   └── vite-env.d.ts        # Vite types — declarații TypeScript pentru Vite
├── package.json             # Dependențe și scripturi npm
├── vite.config.ts           # Configurare Vite: plugins, server, build
├── tsconfig.json            # Configurare TypeScript
└── index.html               # HTML template cu root div
```

### Key Directories

- **components/**: Reusable React components for different parts of the application
- **contexts/**: Context API providers for managing global state (authentication, theme, currency)
- **services/**: API service layer for communicating with the backend
- **hooks/**: Custom React hooks for shared logic

## 🚀 Getting Started

### Prerequisites

- Node.js (14.0 or higher)
- npm or yarn

### Installation

1. Navigate to the React directory:
```bash
cd Web/React
```

2. Install dependencies:
```bash
npm install
```

3. Configure your environment:
   - Ensure the API backend is running (typically on `localhost:8000` or as configured in `api-client.ts`)
   - Verify the `services/api-client.ts` has the correct backend URL

### Running the Development Server

Start the development server with hot module replacement:

```bash
npm run dev
```

The application will be available at `http://localhost:5173` (or another port if 5173 is in use).

## ✨ Features

### Authentication
- User registration and login
- Session management with JWT tokens and cookies
- Automatic logout on session expiry

### Group Management
- Create and manage expense sharing groups
- Add/remove members from groups
- Invitation codes for easy group joining
- View group details and member lists

### Expense Management
- Create expenses and categorize them
- Split expenses among group members
- Track payments and payment status
- View expense history

### Receipt Management
- Upload receipts for expenses
- Process receipts (potentially with OCR via receipt service)
- Manual receipt entry
- View receipt details

### Analytics & Reports
- Dashboard with recent transactions
- Total spending overview
- Data visualization with charts
- Category-based spending analysis

### User Features
- Profile management
- Currency preference
- Theme selection (light/dark mode)
- Category customization

## ⚙️ Configuration

### Environment Variables

Create a `.env` file in the `React/` directory if needed for API configuration. Example:

```env
VITE_API_URL=http://localhost:8000
VITE_API_TIMEOUT=30000
```

### API Client Configuration

The `services/api-client.ts` file handles API communication:

```typescript
import httpService from './http-service';

const apiClient = httpService.create({
    baseURL: 'http://localhost:8000/api',
    timeout: 30000,
});

export default apiClient;
```

Update the `baseURL` to match your backend server address.

### TypeScript Configuration

The project uses two TypeScript configurations:
- `tsconfig.json`: Main configuration for the application
- `tsconfig.node.json`: Configuration for Vite build files

## 👨‍💻 Development

### Project Scripts

```bash
# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

### Code Organization

**Components**: Each component typically handles a specific feature or section of the UI. Components use hooks for state management and context for global state.

**Services**: Service files abstract API calls and business logic, keeping components clean and focused on presentation.

**Contexts**: Global state is managed through React Context:
- `AuthContext`: User authentication state
- `CurrencyContext`: Currency preference and formatting
- `ThemeContext`: Dark/light mode preference

**Hooks**: Custom hooks encapsulate reusable logic, such as `useAuth()` for accessing authentication state.

### Component Communication

- **Props**: Components receive data through props
- **Context**: Global state (auth, theme, currency) is shared via Context API
- **Services**: API calls are made through service modules

## 🏗️ Building

### Production Build

Build the application for production:

```bash
npm run build
```

This generates optimized and minified files in the `dist/` directory.

### Preview Production Build

Test the production build locally:

```bash
npm run preview
```

## 🔗 API Integration

The application communicates with the backend API. Key service modules:

- **auth-service.ts**: Authentication endpoints (login, register, logout)
- **group-service.ts**: Group management endpoints
- **category-service.ts**: Category management
- **receipt-service.ts**: Receipt processing
- **expense-service.ts**: Expense management (referenced in Dashboard)

Each service module exports functions that correspond to API endpoints defined in the backend's route handlers.

## 📦 Dependencies

### Main Dependencies

- **react**: UI library
- **react-dom**: React DOM renderer
- **axios**: HTTP client for API calls
- **bootstrap**: CSS framework for styling
- **recharts**: Charting library for data visualization

### Development Dependencies

- **typescript**: Type checking
- **vite**: Build tool and dev server
- **@vitejs/plugin-react**: React support for Vite
- **@types/react**: TypeScript types for React

## 🤝 Contributing

When adding new features:

1. Create components in the `components/` directory
2. Use services for API calls
3. Manage state with hooks or context
4. Follow TypeScript best practices
5. Use Bootstrap classes for consistent styling

## 📝 Notes

- The application automatically handles authentication state and redirects to login if not authenticated
- The dashboard provides a central hub for navigating between different features
- Currency formatting and theme preferences are persisted across sessions
- The application uses Bootstrap 5 for responsive design
