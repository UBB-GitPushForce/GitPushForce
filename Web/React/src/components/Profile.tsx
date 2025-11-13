// src/components/Profile.tsx
import React, { useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import '../App.css';

interface ProfileProps {
  onRequestNavigate?: (to: 'home'|'groups'|'receipts'|'profile'|'support') => void;
}

const Profile: React.FC<ProfileProps> = ({ onRequestNavigate }) => {
  const { user, logout } = useAuth();
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  const [profile, setProfile] = useState({
    first_name: user?.first_name ?? '',
    last_name: user?.last_name ?? '',
    email: user?.email ?? '',
    phone_number: user?.phone_number ?? '',
    password: '••••••••',
    currency: 'EUR', // default
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
    // TODO: call backend API to save profile field (PATCH /users/{id} or similar)
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

  const saveAll = async () => {
    // TODO: call API to save entire profile (including currency)
    alert('Profile saved (mock). Implement API call to save profile data.');
  };

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

        {/* Currency selection (new) */}
        <div className="bp-box" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <div style={{ color: 'var(--muted-dark)', fontSize: 13 }}>Currency</div>
            <div style={{ fontWeight: 800, marginTop: 6 }}>{profile.currency === 'EUR' ? 'Euro (EUR)' : 'Lei (RON)'}</div>
          </div>
          <div>
            {editingField === 'currency' ? (
              <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                <select value={tempValue} onChange={e => setTempValue(e.target.value)}>
                  <option value="EUR">Euro (EUR)</option>
                  <option value="RON">Lei (RON)</option>
                </select>
                <button onClick={saveEdit} className="btn">Save</button>
                <button onClick={cancelEdit} className="btn" style={{ background: 'transparent', color: 'var(--purple-1)', border: '1px solid rgba(0,0,0,0.08)' }}>Cancel</button>
              </div>
            ) : (
              <button onClick={() => startEdit('currency')} className="btn" style={{ padding: '8px 12px', fontSize: 13 }}>Modify</button>
            )}
          </div>
        </div>

        <div style={{ display: 'flex', gap: 12, marginTop: 6, alignItems: 'center' }}>
          <button className="bp-add-btn" onClick={saveAll}>
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

