import React, {useState} from 'react';
import {useAuth} from "../hooks/useAuth";
import apiClient from '../services/api-client';

const RegisterForm = ({ onBack }: { onBack: () => void }) => {

    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { register } = useAuth();

    // identity verification step
    const [verificationMethod, setVerificationMethod] = useState<'email'|'number'>('email');
    const [verificationStep, setVerificationStep] = useState<'choose'|'verify'|'done'>('choose');
    const [verifyCode, setVerifyCode] = useState('');
    const [verifyError, setVerifyError] = useState('');
    const [sending, setSending] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            // TODO: call register endpoint and trigger verification delivery (email or sms)
            // Example: await apiClient.post('/users/auth/register', { first_name: firstName, last_name: lastName, phone_number: phoneNumber, email, password, verification_method: verificationMethod });
            // For the mock flow we simulate sending a code and go to verification step:
            await new Promise(r => setTimeout(r, 600));
            setVerificationStep('verify');
            alert(`(Mock) Verification code sent via ${verificationMethod}. Use 123456 to verify.`);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Registration failed');
        } finally {
            setLoading(false);
        }
    };

    const submitVerification = async () => {
        setVerifyError('');
        setSending(true);
        try {
            // TODO: call backend to verify registration code, e.g. apiClient.post('/users/auth/verify-registration', { code: verifyCode, email })
            await new Promise(r => setTimeout(r, 600));
            if (verifyCode.trim() === '123456') {
                // finalize registration: optionally call register() from AuthContext to complete auth
                // Here we call register() which calls authService.register (may or may not authenticate)
                await register({
                    first_name: firstName,
                    last_name: lastName,
                    phone_number: phoneNumber,
                    email,
                    password
                }, verificationMethod);
                setVerificationStep('done');
                alert('Registration verified (mock). You are now signed in (mock).');
            } else {
                throw new Error('Invalid verification code (mock)');
            }
        } catch (err: any) {
            setVerifyError(err.message || 'Verification failed');
        } finally {
            setSending(false);
        }
    };

    if (verificationStep === 'verify') {
        return (
            <>
                <div className="title">Verify your identity</div>
                <div className="sub">We sent a code via <strong>{verificationMethod}</strong>. Enter it below to complete registration.</div>

                {verifyError && <div className="error">{verifyError}</div>}

                <label>Verification code</label>
                <input type="text" value={verifyCode} onChange={e => setVerifyCode(e.target.value)} placeholder="123456" />

                <div style={{ display: 'flex', gap: 8 }}>
                    <button className="btn" onClick={submitVerification} disabled={sending || !verifyCode}>Verify</button>
                    <button className="btn" style={{ background: 'transparent', color: 'var(--purple-1)', border: '1px solid rgba(0,0,0,0.08)' }} onClick={() => setVerificationStep('choose')}>
                        Back
                    </button>
                </div>

                <div style={{ marginTop: 8, color: 'var(--muted-dark)' }}>(Mock) Use <strong>123456</strong> as code.</div>
            </>
        );
    }

    if (verificationStep === 'done') {
        return (
            <>
                <div className="title">Welcome!</div>
                <div className="sub">Your account has been created and verified (mock).</div>
                <div style={{ marginTop: 12 }}>
                    <button className="btn" onClick={onBack}>Go to sign in</button>
                </div>
            </>
        );
    }

    return (
        <>
            <div className="title">Sign Up</div>
            <div className="sub">Create your budget manager account</div>

            {error && <div className="error"> {error} </div>}

            <form onSubmit={handleSubmit}>

                <label htmlFor="reg-firstname">First name</label>
                <input
                    id="reg-firstname"
                    type="text"
                    placeholder="Enter your first name"
                    value={firstName}
                    onChange={(e) => setFirstName(e.target.value)}
                    required
                />

                <label htmlFor="reg-secondname">Second name</label>
                <input
                    id="reg-secondname"
                    type="text"
                    placeholder="Enter your second name"
                    value={lastName}
                    onChange={(e) => setLastName(e.target.value)}
                    required
                />

                <label htmlFor="reg-phonenumber">Phone number</label>
                <input
                    id="reg-phonenumber"
                    type="text"
                    placeholder="Enter your phone number"
                    value={phoneNumber}
                    onChange={(e) => setPhoneNumber(e.target.value)}
                    required
                />

                <label htmlFor="reg-email">Email</label>
                <input
                    id="reg-email"
                    type="email"
                    placeholder="Enter your email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                />

                <label htmlFor="reg-password">Password</label>
                <input
                    id="reg-password"
                    type="password"
                    placeholder="Create password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />

                <div style={{ marginTop: 8 }}>
                    <div style={{ fontWeight: 700, marginBottom: 6 }}>Identity verification method</div>
                    <label style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                        <input type="radio" name="verif" value="email" checked={verificationMethod === 'email'} onChange={() => setVerificationMethod('email')} />
                        Verify by email
                    </label>
                    <label style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                        <input type="radio" name="verif" value="number" checked={verificationMethod === 'number'} onChange={() => setVerificationMethod('number')} />
                        Verify by phone message (SMS)
                    </label>
                </div>

                <button className="btn" type="submit" disabled={loading}>
                    {loading ? 'Signing up...' : 'Sign Up'}
                </button>

            </form>

            <div className="small-muted">
                Already have an account?
                <a className="link" onClick={onBack}>
                    Sign in
                </a>
            </div>
        </>
    );
}

export default RegisterForm;

