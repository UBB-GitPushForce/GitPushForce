// src/App.tsx
import LoginMenu from './components/LoginMenu';
import { useAuth } from "./hooks/useAuth";
import { AuthProvider } from "./contexts/AuthContext";
import Dashboard from "./components/Dashboard";
import { ThemeProvider } from './contexts/ThemeContext'; // <-- import

const AppContent = () => {
    const { isAuthenticated, isChecking } = useAuth();

    if (isChecking) {
        return <div>Loading...</div>;
    }

    // TO DO: REPLACE THIS AFTER BACKEND LOGING WORKS
    return <Dashboard />
    return isAuthenticated ? <Dashboard /> : <LoginMenu />;
}

const App = () => {
    return (
        <ThemeProvider> {/* <-- adăugat */}
            <AuthProvider>
                <AppContent />
            </AuthProvider>
        </ThemeProvider>
    );
}

export default App;

