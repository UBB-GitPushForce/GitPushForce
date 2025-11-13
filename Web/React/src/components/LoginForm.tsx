import React, { useState, useEffect } from 'react';
import { useAuth} from "../hooks/useAuth";

const LoginForm = ({ onRegister }: { onRegister: () => void }) => {

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const { login, pendingTwoFactor, verifyTwoFactor, cancelTwoFactor } = useAuth();

    const [twoFactorCode, setTwoFactorCode] = useState('');
    const [twoFactorError, setTwoFactorError] = useState('');
    const [sendingAgain, setSendingAgain] = useState(false);

    useEffect(() => {
        // reset 2FA inputs when pending state changes
        setTwoFactorCode('');
        setTwoFactorError('');
    }, [pendingTwoFactor]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            await login(email, password);
            // if pendingTwoFactor is set by AuthContext, the UI will show code input (do not navigate away)
        } catch (err: any) {
            setError(err.response?.data?.message || err.message || 'Login failed');
        } finally {
            setLoading(false);
        }
    };

    const submit2FA = async () => {
        setTwoFactorError('');
        setLoading(true);
        try {
            // verify code via AuthContext (mock accepts 123456)
            await verifyTwoFactor(twoFactorCode.trim());
            // success => AuthContext will set user and UI will navigate (AppContent uses useAuth)
        } catch (err: any) {
            setTwoFactorError(err.message || 'Verification failed');
        } finally {
            setLoading(false);
        }
    };

    const resendCode = async () => {
        setSendingAgain(true);
        try {
            // TODO: call backend to resend code (based on pendingTwoFactor method)
            // e.g. await apiClient.post('/users/auth/resend-2fa', { ... })
            // MOCK: just wait
            await new Promise(r => setTimeout(r, 700));
            alert('Code resent (mock). Use code: 123456');
        } catch (err) {
            console.error(err);
            alert('Resend failed (mock)');
        } finally {
            setSendingAgain(false);
        }
    };

    if (pendingTwoFactor) {
        return (
            <>
                <div className="title">Two-Factor Authentication</div>
                <div className="sub">A verification code was sent via <strong>{pendingTwoFactor.method}</strong>. Enter the code below.</div>

                {twoFactorError && <div className="error">{twoFactorError}</div>}

                <div style={{ marginTop: 10 }}>
                    <label htmlFor="tfacode">Verification code</label>
                    <input id="tfacode" type="text" value={twoFactorCode} onChange={e => setTwoFactorCode(e.target.value)} placeholder="123456" />
                </div>

                <div style={{ display: 'flex', gap: 8, marginTop: 12 }}>
                    <button className="btn" onClick={submit2FA} disabled={loading || !twoFactorCode}>Verify</button>
                    <button className="btn" style={{ background: 'transparent', color: 'var(--purple-1)', border: '1px solid rgba(0,0,0,0.08)' }} onClick={resendCode} disabled={sendingAgain}>
                        {sendingAgain ? 'Resending...' : 'Resend code'}
                    </button>
                    <button className="btn" style={{ background: 'transparent', color: 'var(--purple-1)', border: '1px solid rgba(0,0,0,0.08)' }} onClick={() => cancelTwoFactor()}>
                        Cancel
                    </button>
                </div>

                <div style={{ marginTop: 12, color: 'var(--muted-dark)' }}>
                    (Mock) Use <strong>123456</strong> to verify.
                </div>
            </>
        );
    }

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

