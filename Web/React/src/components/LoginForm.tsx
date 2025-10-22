// src/components/LoginForm.tsx
import React from 'react';

const LoginForm = ({ onRegister }: { onRegister: () => void }) => (
    <>
        <div className="title">Welcome Back</div>
        <div className="sub">Log in to manage your budget</div>

        <label htmlFor="username">Email or username</label>
        <input id="username" type="text" placeholder="Enter your email or username" />

        <label htmlFor="password">Password</label>
        <input id="password" type="password" placeholder="Enter your password" />

        <div className="muted-line" style={{ marginTop: 8 }}>
            <a href="#" className="link" id="forgot-link">
                Forgot password?
            </a>
        </div>

        <button className="btn">Log in</button>

        <div className="small-muted">
            Don't have an account?
            <a className="link" onClick={onRegister}>
                Sign up
            </a>
        </div>
    </>
);

export default LoginForm;
