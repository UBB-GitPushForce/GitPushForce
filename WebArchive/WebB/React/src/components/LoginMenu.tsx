// src/components/LoginMenu.tsx
import React, { useState } from 'react';
import LoginForm from './LoginForm';
import RegisterForm from './RegisterForm';
import '../App.css';
import ThemeToggle from './ThemeToggle'; // <-- import

const LoginMenu = () => {
    const [showRegister, setShowRegister] = useState(false);

    return (
        <div className="wrap">
            <div className="card" style={{ position: 'relative' }}>
                {/* switch in colțul drept sus */}
                <div style={{ position: 'absolute', right: 12, top: 12 }}>
                    <ThemeToggle />
                </div>

                {!showRegister ? (
                    <LoginForm onRegister={() => setShowRegister(true)} />
                ) : (
                    <RegisterForm onBack={() => setShowRegister(false)} />
                )}
            </div>
        </div>
    );
};

export default LoginMenu;

