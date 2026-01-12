// src/components/Profile.tsx
import React, { useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import apiClient from '../services/api-client';
import { useCurrency } from '../contexts/CurrencyContext';
import '../App.css';

interface ProfileProps {
  onRequestNavigate?: (to: 'home'|'groups'|'receipts'|'profile'|'support') => void;
}

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
  });
  const [savedMessage, setSavedMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  // editing helpers used by the existing UI pattern
  const [editingField, setEditingField] = useState<string | null>(null);
  const [tempValue, setTempValue] = useState('');
  const [oldPassword, setOldPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');

  const getBackendErrorMessage = (err: any): string => {
    if (err.response && err.response.data) {
      const data = err.response.data;

      if (data.message) {
        return data.message;
      }
      if (typeof data.detail === 'string') {
        return data.detail;
      }
      if (Array.isArray(data.detail)) {
        return data.detail.map((e: any) => e.msg || 'Invalid input').join(', ');
      }
    }
    return err.message || 'An unexpected error occurred. Please try again.';
  };

  const startEdit = (field: string) => {
    setEditingField(field);
    setErrorMessage(null);
    setSavedMessage(null);
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
    setErrorMessage(null);
  };

  const saveEdit = async () => {
    setErrorMessage(null);
    setSavedMessage(null);
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
          setErrorMessage('Please enter both current and new password');
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
      setErrorMessage(getBackendErrorMessage(err));
    }
  };

  const handleDelete = async () => {
    setErrorMessage(null);
    const ok = confirm('Are you sure you want to delete your account? This action cannot be undone.');
    if (!ok) return;
    if (!user?.id) {
      setErrorMessage('Not authenticated');
      return;
    }

    try {
      // Call backend DELETE /users/{id}
      await apiClient.delete(`/users/${user.id}`);
      setSavedMessage('Account deleted');
      // then logout locally
      try {
        await logout();
      } catch (err) {
        console.warn('Logout after delete failed', err);
      }
    } catch (err: any) {
      console.error('Failed to delete account', err);
      setErrorMessage(getBackendErrorMessage(err));
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
  
  // ------------------- JSX -------------------
  return (
    <>
      <div className="bp-title" style={{ marginTop: 12 }}>Profile</div>

      {errorMessage && !editingField && (
        <div style={{ 
          marginTop: 12, 
          padding: '8px 12px', 
          backgroundColor: '#ffebee', 
          color: '#c62828', 
          borderRadius: 6,
          fontSize: 14 
        }}>
          {errorMessage}
        </div>
      )}

      <div style={{ marginTop: 12, display: 'grid', gap: 12 }}>
        {/* First Name */}
        <div className="bp-box" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <div style={{ color: 'var(--muted-dark)', fontSize: 13 }}>First name</div>
            <div style={{ fontWeight: 800, marginTop: 6 }}>{profile.first_name}</div>
          </div>
          <div>
            {editingField === 'first_name' ? (
              <div style={{ display: 'flex', gap: 8, flexDirection: 'column', alignItems: 'flex-end' }}>
                <div style={{ display: 'flex', gap: 8 }}>
                    <input value={tempValue} onChange={e => setTempValue(e.target.value)} />
                    <button onClick={saveEdit} className="bp-add-btn">Save</button>
                    <button onClick={cancelEdit} className="bp-add-btn">Cancel</button>
                </div>
                {errorMessage && <div style={{ color: '#d32f2f', fontSize: 12 }}>{errorMessage}</div>}
              </div>
            ) : (
              <button onClick={() => startEdit('first_name')} className="bp-add-btn" style={{ padding: '8px 12px', fontSize: 13 }}>Modify</button>
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
              <div style={{ display: 'flex', gap: 8, flexDirection: 'column', alignItems: 'flex-end' }}>
                <div style={{ display: 'flex', gap: 8 }}>
                    <input value={tempValue} onChange={e => setTempValue(e.target.value)} />
                    <button onClick={saveEdit} className="bp-add-btn">Save</button>
                    <button onClick={cancelEdit} className="bp-add-btn">Cancel</button>
                </div>
                {errorMessage && <div style={{ color: '#d32f2f', fontSize: 12 }}>{errorMessage}</div>}
              </div>
            ) : (
              <button onClick={() => startEdit('last_name')} className="bp-add-btn" style={{ padding: '8px 12px', fontSize: 13 }}>Modify</button>
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
              <div style={{ display: 'flex', gap: 8, flexDirection: 'column', alignItems: 'flex-end' }}>
                <div style={{ display: 'flex', gap: 8 }}>
                    <input value={tempValue} onChange={e => setTempValue(e.target.value)} />
                    <button onClick={saveEdit} className="bp-add-btn">Save</button>
                    <button onClick={cancelEdit} className="bp-add-btn">Cancel</button>
                </div>
                {errorMessage && <div style={{ color: '#d32f2f', fontSize: 12 }}>{errorMessage}</div>}
              </div>
            ) : (
              <button onClick={() => startEdit('email')} className="bp-add-btn" style={{ padding: '8px 12px', fontSize: 13 }}>Modify</button>
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
              <div style={{ display: 'flex', gap: 8, flexDirection: 'column', alignItems: 'flex-end' }}>
                 <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                    <input type="password" placeholder="Current password" value={oldPassword} onChange={e => setOldPassword(e.target.value)} />
                    <input type="password" placeholder="New password" value={newPassword} onChange={e => setNewPassword(e.target.value)} />
                    <button onClick={saveEdit} className="bp-add-btn">Save</button>
                    <button onClick={cancelEdit} className="bp-add-btn">Cancel</button>
                 </div>
                 {errorMessage && <div style={{ color: '#d32f2f', fontSize: 12 }}>{errorMessage}</div>}
              </div>
            ) : (
              <button onClick={() => startEdit('password')} className="bp-add-btn" style={{ padding: '8px 12px', fontSize: 13 }}>Modify</button>
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
              <div style={{ display: 'flex', gap: 8, flexDirection: 'column', alignItems: 'flex-end' }}>
                <div style={{ display: 'flex', gap: 8 }}>
                    <input value={tempValue} onChange={e => setTempValue(e.target.value)} />
                    <button onClick={saveEdit} className="bp-add-btn">Save</button>
                    <button onClick={cancelEdit} className="bp-add-btn">Cancel</button>
                </div>
                {errorMessage && <div style={{ color: '#d32f2f', fontSize: 12 }}>{errorMessage}</div>}
              </div>
            ) : (
              <button onClick={() => startEdit('phone_number')} className="bp-add-btn" style={{ padding: '8px 12px', fontSize: 13 }}>Modify</button>
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
                <button onClick={saveEdit} className="bp-add-btn">Save</button>
                <button onClick={cancelEdit} className="bp-add-btn">Cancel</button>
              </div>
            ) : (
              <button onClick={() => startEdit('currency')} className="bp-add-btn" style={{ padding: '8px 12px', fontSize: 13 }}>Modify</button>
            )}
          </div>
        </div>
        <div style={{ display: 'flex', gap: 12, marginTop: 6, alignItems: 'center' }}>
          <button className="bp-add-btn" onClick={handleDelete}>
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

