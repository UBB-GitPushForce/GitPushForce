// src/components/Profile.tsx
import React, { useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import apiClient from '../services/api-client';
import { useCurrency } from '../contexts/CurrencyContext';
import '../App.css';

interface ProfileProps {
  onRequestNavigate?: (to: 'home'|'groups'|'receipts'|'profile'|'support') => void;
}

type TwoFactorOption = 'off' | 'email' | 'number' | 'app';

const Profile: React.FC<ProfileProps> = ({ onRequestNavigate }) => {
  const { user, logout, refreshUser, updateUser } = useAuth();
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  const { currency, setCurrency } = useCurrency();

  const [profile, setProfile] = useState({
    first_name: user?.first_name ?? '',
    last_name: user?.last_name ?? '',
    email: user?.email ?? '',
    phone_number: user?.phone_number ?? '',
    password: '••••••••',
    twoFactor: 'off' as TwoFactorOption, // 'off' | 'email' | 'number' | 'app'
  });
  const [savedMessage, setSavedMessage] = useState<string | null>(null);

  // editing helpers used by the existing UI pattern
  const [editingField, setEditingField] = useState<string | null>(null);
  const [tempValue, setTempValue] = useState('');
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');

  const startEdit = (field: string) => {
    setEditingField(field);
    if (field === 'currency') {
      setTempValue(currency);
    } else if (field === 'password') {
      setOldPassword('');
      setNewPassword('');
      setTempValue('');
    } else {
      setTempValue((profile as any)[field] ?? '');
    }
  };

  const cancelEdit = () => {
    setEditingField(null);
    setTempValue('');
    setOldPassword('');
    setNewPassword('');
  };

  const saveEdit = async () => {
    try {
      if (!editingField) return;

      // Currency handled separately
      if (editingField === 'currency') {
        setCurrency(tempValue === 'EUR' ? 'EUR' : 'RON');
        setSavedMessage('Saved successfully');
        setTimeout(() => setSavedMessage(null), 2500);
        setEditingField(null);
        return;
      }

      if (editingField === 'password') {
        if (!oldPassword || !newPassword) {
          alert('Please enter both current and new password');
          return;
        }
        await apiClient.put('/users/password/change', { old_password: oldPassword, new_password: newPassword });
        setSavedMessage('Password changed successfully');
        setTimeout(() => setSavedMessage(null), 2500);
        setEditingField(null);
        setOldPassword('');
        setNewPassword('');
        return;
      }

      if (!user) throw new Error('Not authenticated');
      const payload: any = { [editingField]: tempValue };
      await apiClient.put(`/users/${user.id}`, payload);

      // Optimistically update local and global user so UI reflects changes immediately
      setProfile(prev => ({ ...prev, [editingField as string]: tempValue }));
      try { updateUser({ [editingField]: tempValue } as any); } catch (err) { /* ignore */ }

      setSavedMessage('Saved successfully');
      setTimeout(() => setSavedMessage(null), 2500);
      setEditingField(null);
      setTempValue('');
    } catch (err: any) {
      console.error('Failed to save profile field', err);
      alert(err?.response?.data?.detail || err.message || 'Save failed');
    }
  };

  const handleDelete = async () => {
    const ok = confirm('Are you sure you want to delete your account? This action cannot be undone.');
    if (!ok) return;
    // TODO: call delete endpoint
    alert('Account deleted (mock). You should call delete endpoint and then logout.');
    try {
      await logout();
    } catch {
      // ignore
    }
  };

  const handleLogout = async () => {
    setIsLoggingOut(true);
    try {
      await logout();
    } catch (err) {
      console.error(err);
      setIsLoggingOut(false);
    }
  };

  // `Save Changes` button removed — individual fields save on their own.

  // --------------- 2FA UI state & helpers (front-end mock) ---------------
  const [selected2FA, setSelected2FA] = useState<TwoFactorOption>(profile.twoFactor);
  const [pendingActivation, setPendingActivation] = useState<TwoFactorOption | null>(null);
  const [verificationCode, setVerificationCode] = useState('');
  const [twoFaMessage, setTwoFaMessage] = useState<string | null>(null);

  const beginActivate = (opt: TwoFactorOption) => {
    // Start activation flow: in real app, trigger backend to send code to email/number/app
    if (opt === 'off') {
      // Deactivate immediately
      setProfile(prev => ({ ...prev, twoFactor: 'off' }));
      setSelected2FA('off');
      setTwoFaMessage('2FA dezactivate (mock).');
      // TODO: call backend to deactivate 2FA
      return;
    }

    // Start pending activation
    setPendingActivation(opt);
    setVerificationCode('');
    setTwoFaMessage(`A verification code was sent (mock) via ${opt === 'email' ? 'email' : opt === 'number' ? 'SMS' : 'the mobile app'}. Enter a code to confirm activation.`);
    // TODO: call backend to send verification code for selected method
  };

  const confirmActivation = () => {
    if (!pendingActivation) return;
    // Mock verification: accept any non-empty code as success (replace with real verification)
    if (!verificationCode.trim()) {
      alert('Please enter the verification code (mock).');
      return;
    }

    // Mark 2FA as active for selected method, ensure only one method active
    setProfile(prev => ({ ...prev, twoFactor: pendingActivation }));
    setSelected2FA(pendingActivation);
    setPendingActivation(null);
    setVerificationCode('');
    setTwoFaMessage(`2FA is now active (${pendingActivation}).`);
    // TODO: call backend to confirm code and persist 2FA activation
  };

  const cancelActivation = () => {
    setPendingActivation(null);
    setVerificationCode('');
    setTwoFaMessage(null);
  };

  const deactivate2FA = () => {
    if (!confirm('Are you sure you want to dezactivate Two-Factor Authentication?')) return;
    setProfile(prev => ({ ...prev, twoFactor: 'off' }));
    setSelected2FA('off');
    setTwoFaMessage('2FA dezactivate (mock).');
    // TODO: call backend to dezactivate 2FA
  };

  // Ensure UI's selected2FA follows persisted profile.twoFactor initially
  React.useEffect(() => {
    setSelected2FA(profile.twoFactor as TwoFactorOption);
  }, []); // run once on mount

  // Keep local profile in sync with AuthContext `user` so updates persist when navigating
  React.useEffect(() => {
    setProfile(prev => ({
      ...prev,
      first_name: user?.first_name ?? prev.first_name,
      last_name: user?.last_name ?? prev.last_name,
      email: user?.email ?? prev.email,
      phone_number: user?.phone_number ?? prev.phone_number,
    }));
  }, [user]);

  // ------------------- JSX -------------------
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
              <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                <input type="password" placeholder="Current password" value={oldPassword} onChange={e => setOldPassword(e.target.value)} />
                <input type="password" placeholder="New password" value={newPassword} onChange={e => setNewPassword(e.target.value)} />
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

        {/* Currency selection */}
        <div className="bp-box" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <div style={{ color: 'var(--muted-dark)', fontSize: 13 }}>Currency</div>
            <div style={{ fontWeight: 800, marginTop: 6 }}>{currency === 'EUR' ? 'Euro (EUR)' : 'Lei'}</div>
          </div>
          <div>
            {editingField === 'currency' ? (
              <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                <select value={tempValue} onChange={e => setTempValue(e.target.value)}>
                  <option value="EUR">Euro (EUR)</option>
                  <option value="RON">Lei</option>
                </select>
                <button onClick={saveEdit} className="btn">Save</button>
                <button onClick={cancelEdit} className="btn" style={{ background: 'transparent', color: 'var(--purple-1)', border: '1px solid rgba(0,0,0,0.08)' }}>Cancel</button>
              </div>
            ) : (
              <button onClick={() => startEdit('currency')} className="btn" style={{ padding: '8px 12px', fontSize: 13 }}>Modify</button>
            )}
          </div>
        </div>

        {/* Two-Factor Authentication */}
        <div className="bp-box" style={{ display: 'grid', gap: 10 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
              <div style={{ color: 'var(--muted-dark)', fontSize: 13 }}>Two-Factor Authentication</div>
              <div style={{ fontWeight: 800, marginTop: 6 }}>
                {profile.twoFactor === 'off' ? 'off' : `active (${profile.twoFactor})`}
              </div>
            </div>

            <div style={{ display: 'flex', gap: 8 }}>
              <button className="btn" onClick={deactivate2FA} style={{ padding: '8px 12px' }}>Dezactivate</button>
            </div>
          </div>

          <div style={{ color: 'var(--muted-dark)', fontSize: 13 }}>
            Choose one method (only one can be active at a time). Click <strong>Activate</strong> to start verification.
          </div>

          <div style={{ display: 'flex', gap: 12, alignItems: 'center', flexWrap: 'wrap' }}>
            <label style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
              <input type="radio" name="twofa" value="off" checked={selected2FA === 'off' && !pendingActivation} onChange={() => { setSelected2FA('off'); }} />
              Off
            </label>

            <label style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
              <input type="radio" name="twofa" value="email" checked={selected2FA === 'email' && !pendingActivation} onChange={() => { setSelected2FA('email'); }} />
              Email
            </label>

            <label style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
              <input type="radio" name="twofa" value="number" checked={selected2FA === 'number' && !pendingActivation} onChange={() => { setSelected2FA('number'); }} />
              Number (SMS)
            </label>

            <label style={{ display: 'flex', gap: 6, alignItems: 'center' }}>
              <input type="radio" name="twofa" value="app" checked={selected2FA === 'app' && !pendingActivation} onChange={() => { setSelected2FA('app'); }} />
              Our phone application
            </label>

            <div style={{ marginLeft: 'auto', display: 'flex', gap: 8 }}>
              <button
                className="bp-add-btn"
                onClick={() => beginActivate(selected2FA)}
              >
                {selected2FA === 'off' ? 'Save' : 'Activate'}
              </button>
            </div>
          </div>

          {/* Pending activation / verification UI */}
          {pendingActivation && (
            <div style={{ marginTop: 8, display: 'grid', gap: 8 }}>
              <div style={{ color: 'var(--muted-dark)' }}>
                A verification code was (mock) sent via {pendingActivation === 'email' ? 'email' : pendingActivation === 'number' ? 'SMS' : 'the mobile app'}. Enter it below to confirm activation.
              </div>
              <input placeholder="Verification code" value={verificationCode} onChange={e => setVerificationCode(e.target.value)} />
              <div style={{ display: 'flex', gap: 8 }}>
                <button className="bp-add-btn" onClick={confirmActivation}>Confirm</button>
                <button className="btn" onClick={cancelActivation} style={{ background: 'transparent', color: 'var(--purple-1)', border: '1px solid rgba(0,0,0,0.08)' }}>Cancel</button>
              </div>
            </div>
          )}

          {twoFaMessage && <div style={{ color: 'var(--muted-dark)', fontSize: 13 }}>{twoFaMessage}</div>}
        </div>

        <div style={{ display: 'flex', gap: 12, marginTop: 6, alignItems: 'center' }}>
          <button className="btn" style={{ background: 'transparent', color: 'var(--purple-1)', border: '1px solid rgba(0,0,0,0.08)' }} onClick={handleDelete}>
            Delete account
          </button>
          {savedMessage && (
            <div style={{ marginLeft: 8, color: 'var(--muted-dark)', fontSize: 13 }}>{savedMessage}</div>
          )}
        </div>
      </div>
    </>
  );
};

export default Profile;

