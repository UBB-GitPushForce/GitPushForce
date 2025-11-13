// src/components/LoginMenu.tsx
import React, { useState } from 'react';
import LoginForm from './LoginForm';
import RegisterForm from './RegisterForm';
import '../App.css';

const LoginMenu = () => {
    const [showRegister, setShowRegister] = useState(false);

    return (
        <div className="wrap">
            <div className="card">
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
