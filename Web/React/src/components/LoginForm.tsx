// src/components/LoginForm.tsx
import React, { useState } from 'react';
import { useAuth} from "../hooks/useAuth";

const LoginForm = ({ onRegister }: { onRegister: () => void }) => {

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const {login} = useAuth();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await login(email, password);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Login failed');
        } finally {
            setLoading(false);
        }
    };

    return (
        <>
            <div className="title">Welcome Back</div>
            <div className="sub">Log in to manage your budget</div>

            {error && <div className="error"> {error} </div>}

            <form onSubmit={handleSubmit}>

                <label htmlFor="email">Email</label>
                <input
                    id="email"
                    type="email"
                    placeholder="Enter your email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                />

                <label htmlFor="password">Password</label>
                <input
                    id="password"
                    type="password"
                    placeholder="Enter your password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />

                <div className="muted-line" style={{marginTop: 8}}>
                    <a href="#" className="link" id="forgot-link">
                        Forgot password?
                    </a>
                </div>

                <button className="btn" type="submit" disabled={loading}>
                    {loading ? 'Logging in...' : 'Log in'}
                </button>

            </form>


            <div className="small-muted">
                Don't have an account?
                <a className="link" onClick={onRegister}>
                    Sign up
                </a>
            </div>
        </>
    );
}

export default LoginForm;
