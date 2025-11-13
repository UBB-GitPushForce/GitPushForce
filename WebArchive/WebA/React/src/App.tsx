import LoginMenu from './components/LoginMenu';
import {useAuth} from "./hooks/useAuth";
import {AuthProvider} from "./contexts/AuthContext";
import Dashboard from "./components/Dashboard";


const AppContent = () => {
    const { isAuthenticated, isChecking } = useAuth();

    if (isChecking) {
        return <div>Loading...</div>;
    }

    return isAuthenticated ? <Dashboard /> : <LoginMenu />;
}

const App = () => {
    return (
        <AuthProvider>
            <AppContent />
        </AuthProvider>
    );
}

export default App;
