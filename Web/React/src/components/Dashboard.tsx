import React, { useState } from 'react';
import { useAuth } from '../hooks/useAuth';

const Dashboard = () => {
    const { user, logout } = useAuth();
    const [isLoggingOut, setIsLoggingOut] = useState(false);

    const handleLogout = async () => {
        setIsLoggingOut(true);
        try {
            await logout();
            // Redirect-ul se face automat din App.tsx când isAuthenticated devine false
        } catch (error) {
            console.error('Logout failed:', error);
            setIsLoggingOut(false);
        }
    };

    return (
        <div>
            {user && (
                <div>
                    <p>Welcome, {user.first_name}!</p>
                    <button
                        onClick={handleLogout}
                        disabled={isLoggingOut}
                    >
                        {isLoggingOut ? 'Logging out...' : 'Logout'}
                    </button>
                </div>
            )}
        </div>
    );
};

export default Dashboard;
