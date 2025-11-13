import React, { useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import '../App.css';

interface ProfileProps {
    onRequestNavigate?: (to: 'home'|'groups'|'receipts'|'profile'|'support') => void;
}

const Profile: React.FC<ProfileProps> = ({ onRequestNavigate }) => {
    const { user } = useAuth();
    const [isLoggingOut, setIsLoggingOut] = useState(false);

    const [profile, setProfile] = useState({
        first_name: user?.first_name ?? '',
        last_name: user?.last_name ?? '',
        email: user?.email ?? '',
        phone_number: user?.phone_number ?? '',
        password: '••••••••'
    });

    const [editingField, setEditingField] = useState<string | null>(null);
    const [tempValue, setTempValue] = useState('');

    const startEdit = (field: string) => {
        setEditingField(field);
        setTempValue((profile as any)[field] ?? '');
    };

    const cancelEdit = () => {
        setEditingField(null);
        setTempValue('');
    };

    const saveEdit = () => {
        // TODO: hookup backend update
        setProfile(prev => ({ ...prev, [editingField as string]: tempValue }));
        setEditingField(null);
        setTempValue('');
        alert('Saved (mock). Hook this to your update API.');
    };

    const handleDelete = async () => {
        const ok = confirm('Are you sure you want to delete your account? This action cannot be undone.');
        if (!ok) return;
        // TODO: call delete endpoint
        alert('Account deleted (mock). You should call delete endpoint and then logout.');
    };

    const handleLogout = async () => {
        setIsLoggingOut(true);
        try {
            // TODO: call logout through auth context
        } catch (err) {
            console.error(err);
            setIsLoggingOut(false);
        }
    };

    // ------------- 2FA section (front-end only / mock) -------------
    type TwoFAOption = 'off'|'email'|'number'|'app';
    const [twoFA, setTwoFA] = useState<TwoFAOption>('off'); // currently active method (mock)
    const [selected2FA, setSelected2FA] = useState<TwoFAOption>('off'); // selection before activation
    const [verifCode, setVerifCode] = useState('');
    const [verifMode, setVerifMode] = useState<'idle'|'sent'|'verifying'>('idle');
    const [verifError, setVerifError] = useState('');

    const activate2FA = async () => {
        setVerifError('');
        if (selected2FA === 'off') {
            alert('Select a method to active (email, number or our phone application).');
            return;
        }

        if (selected2FA === 'app') {
            // our phone application activation flow (mock) — immediate activation
            // TODO: backend: register device / push notification setup
            setTwoFA('app');
            alert('Two-factor (our phone application) active (mock).');
            return;
        }

        // For email/number, simulate sending code and verifying
        try {
            setVerifMode('sent');
            // TODO: call backend to send code: apiClient.post('/users/2fa/send', { method: selected2FA })
            await new Promise(r => setTimeout(r, 600));
            alert(`(Mock) Verification code sent via ${selected2FA}. Use 123456 to verify.`);
        } catch (err) {
            console.error(err);
            setVerifError('Failed to send verification code (mock).');
            setVerifMode('idle');
        }
    };

    const confirmActivate2FA = async () => {
        setVerifError('');
        setVerifMode('verifying');
        try {
            // TODO: verify code on backend: apiClient.post('/users/2fa/verify', { method: selected2FA, code: verifCode })
            await new Promise(r => setTimeout(r, 600));
            if (verifCode.trim() === '123456') {
                setTwoFA(selected2FA);
                setVerifMode('idle');
                setVerifCode('');
                alert('Two-factor authentication active (mock).');
            } else {
                throw new Error('Invalid code (mock)');
            }
        } catch (err: any) {
            setVerifError(err.message || 'Verification failed (mock)');
            setVerifMode('idle');
        }
    };

    const dezactivate2FA = async () => {
        // TODO: call backend to remove 2FA
        setTwoFA('off');
        alert('Two-factor authentication dezactivate (mock).');
    };

    // ----------------------------------------------------------------

    return (
        <>
            <div style={{ marginTop: 12, fontWeight: 800, fontSize: 20, color: 'var(--text-dark)' }}>Profile</div>

            <div style={{ marginTop: 12, display: 'grid', gap: 12 }}>
                {/* First Name */}
                <div className="bp-box" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                        <div style={{ color: 'var(--muted-dark)', fontSize: 13 }}>First name</div>
                        <div style={{ fontWeight: 800, marginTop: 6 }}>{profile.first_name}</div>
                    </div>
                    <div>
                        {editingField === 'first_name' ? (
                            <div style={{ display: 'flex', gap: 8 }}>
                                <input value={tempValue} onChange={e => setTempValue(e.target.value)} />
                                <button onClick={saveEdit} className="btn">Save</button>
                                <button onClick={cancelEdit} className="btn" style={{ background: 'transparent', color: 'var(--purple-1)', border: '1px solid rgba(0,0,0,0.08)' }}>Cancel</button>
                            </div>
                        ) : (
                            <button onClick={() => startEdit('first_name')} className="btn" style={{ padding: '8px 12px', fontSize: 13 }}>Modify</button>
                        )}
                    </div>
                </div>

                {/* Last Name */}
                <div className="bp-box" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                        <div style={{ color: 'var(--muted-dark)', fontSize: 13 }}>Last name</div>
                        <div style={{ fontWeight: 800, marginTop: 6 }}>{profile.last_name}</div>
                    </div>
                    <div>
                        {editingField === 'last_name' ? (
                            <div style={{ display: 'flex', gap: 8 }}>
                                <input value={tempValue} onChange={e => setTempValue(e.target.value)} />
                                <button onClick={saveEdit} className="btn">Save</button>
                                <button onClick={cancelEdit} className="btn" style={{ background: 'transparent', color: 'var(--purple-1)', border: '1px solid rgba(0,0,0,0.08)' }}>Cancel</button>
                            </div>
                        ) : (
                            <button onClick={() => startEdit('last_name')} className="btn" style={{ padding: '8px 12px', fontSize: 13 }}>Modify</button>
                        )}
                    </div>
                </div>

                {/* Email */}
                <div className="bp-box" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                        <div style={{ color: 'var(--muted-dark)', fontSize: 13 }}>Email</div>
                        <div style={{ fontWeight: 800, marginTop: 6 }}>{profile.email}</div>
                    </div>
                    <div>
                        {editingField === 'email' ? (
                            <div style={{ display: 'flex', gap: 8 }}>
                                <input value={tempValue} onChange={e => setTempValue(e.target.value)} />
                                <button onClick={saveEdit} className="btn">Save</button>
                                <button onClick={cancelEdit} className="btn" style={{ background: 'transparent', color: 'var(--purple-1)', border: '1px solid rgba(0,0,0,0.08)' }}>Cancel</button>
                            </div>
                        ) : (
                            <button onClick={() => startEdit('email')} className="btn" style={{ padding: '8px 12px', fontSize: 13 }}>Modify</button>
                        )}
                    </div>
                </div>

                {/* Password */}
                <div className="bp-box" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                        <div style={{ color: 'var(--muted-dark)', fontSize: 13 }}>Password</div>
                        <div style={{ fontWeight: 800, marginTop: 6 }}>{profile.password}</div>
                    </div>
                    <div>
                        {editingField === 'password' ? (
                            <div style={{ display: 'flex', gap: 8 }}>
                                <input type="password" value={tempValue} onChange={e => setTempValue(e.target.value)} />
                                <button onClick={saveEdit} className="btn">Save</button>
                                <button onClick={cancelEdit} className="btn" style={{ background: 'transparent', color: 'var(--purple-1)', border: '1px solid rgba(0,0,0,0.08)' }}>Cancel</button>
                            </div>
                        ) : (
                            <button onClick={() => startEdit('password')} className="btn" style={{ padding: '8px 12px', fontSize: 13 }}>Modify</button>
                        )}
                    </div>
                </div>

                {/* Phone number */}
                <div className="bp-box" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                        <div style={{ color: 'var(--muted-dark)', fontSize: 13 }}>Phone number</div>
                        <div style={{ fontWeight: 800, marginTop: 6 }}>{profile.phone_number}</div>
                    </div>
                    <div>
                        {editingField === 'phone_number' ? (
                            <div style={{ display: 'flex', gap: 8 }}>
                                <input value={tempValue} onChange={e => setTempValue(e.target.value)} />
                                <button onClick={saveEdit} className="btn">Save</button>
                                <button onClick={cancelEdit} className="btn" style={{ background: 'transparent', color: 'var(--purple-1)', border: '1px solid rgba(0,0,0,0.08)' }}>Cancel</button>
                            </div>
                        ) : (
                            <button onClick={() => startEdit('phone_number')} className="btn" style={{ padding: '8px 12px', fontSize: 13 }}>Modify</button>
                        )}
                    </div>
                </div>

                {/* 2FA Section */}
                <div style={{ marginTop: 8, padding: 12, borderRadius: 10, background: '#f7f7fb', border: '1px solid #e8e9f2' }}>
                    <div style={{ fontWeight: 800 }}>Two-Factor Authentication</div>
                    <div style={{ color: 'var(--muted-dark)', marginTop: 6 }}>Current status: <strong>{twoFA === 'off' ? 'Off' : twoFA}</strong></div>

                    <div style={{ display: 'flex', gap: 8, marginTop: 10, alignItems: 'center' }}>
                        <label style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                            <input type="radio" name="2fa" value="off" checked={selected2FA === 'off'} onChange={() => setSelected2FA('off')} />
                            Off
                        </label>

                        <label style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                            <input type="radio" name="2fa" value="email" checked={selected2FA === 'email'} onChange={() => setSelected2FA('email')} />
                            Email
                        </label>

                        <label style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                            <input type="radio" name="2fa" value="number" checked={selected2FA === 'number'} onChange={() => setSelected2FA('number')} />
                            Number (SMS)
                        </label>

                        <label style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                            <input type="radio" name="2fa" value="app" checked={selected2FA === 'app'} onChange={() => setSelected2FA('app')} />
                            Our phone application
                        </label>

                        <div style={{ marginLeft: 'auto', display: 'flex', gap: 8 }}>
                            <button className="btn" onClick={activate2FA}>active</button>
                            <button className="btn" onClick={dezactivate2FA}>dezactivate</button>
                        </div>
                    </div>

                    {verifMode === 'sent' && (
                        <div style={{ marginTop: 10 }}>
                            <div style={{ color: 'var(--muted-dark)' }}>Enter verification code sent to {selected2FA}:</div>
                            <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
                                <input value={verifCode} onChange={e => setVerifCode(e.target.value)} placeholder="123456" />
                                <button className="btn" onClick={confirmActivate2FA}>Verify</button>
                                <button className="btn" style={{ background: 'transparent', color: 'var(--purple-1)', border: '1px solid rgba(0,0,0,0.08)' }} onClick={() => { setVerifMode('idle'); setVerifCode(''); }}>Cancel</button>
                            </div>
                            {verifError && <div style={{ color: 'red', marginTop: 8 }}>{verifError}</div>}
                            <div style={{ marginTop: 6, color: 'var(--muted-dark)' }}>(Mock) Use <strong>123456</strong> to confirm.</div>
                        </div>
                    )}
                </div>

                <div style={{ display: 'flex', gap: 12, marginTop: 6, alignItems: 'center' }}>
                    <button className="bp-add-btn" onClick={() => alert('Modify profile — use Save on each field (mock).')}>
                        Save Changes
                    </button>
                    <button className="btn" style={{ background: 'transparent', color: 'var(--purple-1)', border: '1px solid rgba(0,0,0,0.08)' }} onClick={handleDelete}>
                        Delete account
                    </button>
                </div>
            </div>
        </>
    );
};

export default Profile;

