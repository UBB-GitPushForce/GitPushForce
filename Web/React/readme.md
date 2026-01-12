# React - Web Application Frontend

A React + TypeScript web frontend for the GitPushForce expense sharing application.

## Quick Start

### Installation

```bash
npm install
```

### Development

```bash
npm run dev
```

Runs on `http://localhost:5173` with hot module replacement enabled.

### Production Build

```bash
npm run build
```

Compiles TypeScript and creates an optimized production build in the `dist/` folder.

### Preview Build

```bash
npm run preview
```

Locally preview the production build.

## Project Overview

This is a single-page application (SPA) built with:
- **React 18**: Modern UI framework
- **TypeScript**: Type-safe development
- **Vite**: Fast build tool with instant HMR
- **Bootstrap 5**: Responsive UI components
- **Axios**: HTTP client for API communication
- **Recharts**: Data visualization library

## Key Features

- User authentication and session management
- Group creation and management
- Expense tracking and splitting
- Receipt upload and processing
- Category management
- Dark/light theme support
- Multi-currency support
- Real-time dashboard with recent transactions

## Architecture

The application follows a layered architecture:

1. **Components Layer**: Reusable UI components
2. **Services Layer**: API communication and business logic
3. **Context Layer**: Global state management (Auth, Currency, Theme)
4. **Hooks Layer**: Reusable logic with custom hooks

## Main Components

- `App`: Root component with authentication routing
- `Dashboard`: Main application hub after login
- `LoginMenu`: Login/register entry point
- `Groups`: Group management interface
- `Receipts`: Receipt handling interface
- `Categories`: Category management
- `Profile`: User profile settings

## Services

- `api-client`: Configured Axios instance
- `auth-service`: Authentication operations
- `group-service`: Group management
- `receipt-service`: Receipt processing
- `category-service`: Category operations
- `exchange-rate`: Currency conversion

## State Management

Global state is managed through React Context:
- `AuthContext`: User and authentication state
- `CurrencyContext`: Currency preferences
- `ThemeContext`: Dark/light mode

For more detailed documentation, see [Web/README.md](../README.md).
