// src/components/RegisterForm.tsx
import React from 'react';

const RegisterForm = ({ onBack }: { onBack: () => void }) => (
    <>
        <div className="title">Sign Up</div>

        <label htmlFor="reg-username">Username</label>
        <input id="reg-username" type="text" placeholder="Choose a username" />

        <label htmlFor="reg-email">Email</label>
        <input id="reg-email" type="email" placeholder="name@example.com" />

        <label htmlFor="reg-password">Password</label>
        <input id="reg-password" type="password" placeholder="Create password" />

        <label htmlFor="reg-confirm">Confirm Password</label>
        <input id="reg-confirm" type="password" placeholder="Confirm password" />

        <button className="btn">Sign Up</button>

        <div className="small-muted">
            Already have an account?
            <a className="link" onClick={onBack}>
                Sign in
            </a>
        </div>
    </>
);

export default RegisterForm;
