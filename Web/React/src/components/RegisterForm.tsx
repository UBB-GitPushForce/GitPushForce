import React, { useState } from 'react';
import { useAuth } from "../hooks/useAuth";

const RegisterForm = ({ onBack }: { onBack: () => void }) => {

    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const { register } = useAuth();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            // Call the register method (authService)
            await register({
                first_name: firstName,
                last_name: lastName,
                phone_number: phoneNumber,
                email,
                password
            });

            // After successful registration, go back to login or dashboard
            onBack();
        } catch (err: any) {
            setError(err.response?.data?.message || 'Registration failed');
        } finally {
            setLoading(false);
        }
    };

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

