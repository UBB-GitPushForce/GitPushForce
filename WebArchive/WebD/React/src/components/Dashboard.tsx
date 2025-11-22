// src/components/Dashboard.tsx
import React, { useEffect, useState } from 'react';
import apiClient from '../services/api-client';
import { useAuth } from '../hooks/useAuth';
import '../App.css';
import ThemeToggle from './ThemeToggle';
import Groups from './Groups';
import Profile from './Profile';
import Support from './Support';
import GroupDetail from './GroupDetail';
import Receipts from './Receipts';
import ReceiptsView from './ReceiptsView'; // <-- adăugat
import ChatBot from './ChatBot';
import Data from './Data';
import Alerts from './Alerts';
import Notifications from './Notifications';
import Map from './Map';

interface Tx {
  id: number;
  title: string;
  cat: string;
  amount: number;
  thumb: string;
}

const Dashboard: React.FC = () => {
  const { user, logout } = useAuth();
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const [screen, setScreen] = useState<
    | 'home'
    | 'groups'
    | 'receipts'
    | 'profile'
    | 'support'
    | 'groupDetail'
    | 'chat'
    | 'data'
    | 'alerts'
    | 'notifications'
    | 'map'
  >('home');
  const [selectedGroupId, setSelectedGroupId] = useState<number | null>(null);
  const [recentTx, setRecentTx] = useState<Tx[]>([]);
  const [totalSpent, setTotalSpent] = useState<number>(0);

  // Fetch last 5 transactions from API (mock safe)
  const fetchRecentTransactions = async () => {
    try {
      const res = await apiClient.get('/expenses', {
        params: {
          offset: 0,
          limit: 5,
          sort_by: 'created_at',
          order: 'desc',
        },
      });

      const items = Array.isArray(res.data) ? res.data : [];
      const mapped: Tx[] = items.map((x: any) => ({
        id: x.id,
        title: x.title,
        cat: x.category || 'Other',
        amount: x.amount,
        thumb: x.title ? x.title[0].toUpperCase() : 'T',
      }));

      setRecentTx(mapped);
    } catch (err) {
      console.error('Failed to fetch recent transactions', err);
      setRecentTx([]);
    }
  };

  // Fetch total spent amount
  const fetchTotalSpent = async () => {
    try {
      const res = await apiClient.get('/expenses', {
        params: {
          offset: 0,
          limit: 100,
        },
      });

      const items = Array.isArray(res.data) ? res.data : [];
      const total = items.reduce((sum: number, exp: any) => sum + (exp.amount || 0), 0);
      setTotalSpent(total);
    } catch (err) {
      console.error('Failed to fetch total spent', err);
      setTotalSpent(0);
    }
  };

  useEffect(() => {
    fetchRecentTransactions();
    fetchTotalSpent();
  }, []);

  useEffect(() => {
    if (screen === 'home') {
      fetchRecentTransactions();
      fetchTotalSpent();
    }
  }, [screen]);

  const handleLogout = async () => {
    setIsLoggingOut(true);
    try {
      await logout();
    } catch (err) {
      console.error('Logout failed', err);
      setIsLoggingOut(false);
    }
  };

  const navigate = (to: typeof screen) => {
    setScreen(to);
    if (to !== 'groupDetail') setSelectedGroupId(null);
  };

  const openGroup = (groupId: number) => {
    setSelectedGroupId(groupId);
    setScreen('groupDetail');
  };

  const cardClass = `bp-card${screen === 'receipts' ? ' receipts-wide' : ''}`;

  return (
    <div className="bp-wrap">
      <div className={cardClass} role="main">
        <div className="bp-topbar">
          <div style={{ display: 'flex', flexDirection: 'column' }}>
            <div style={{ fontSize: 12, color: 'var(--muted-dark)' }}>Welcome</div>
            <div className="bp-title">Hi, {user?.first_name ?? 'User'}</div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <ThemeToggle />
            <button
              className="bp-logout"
              onClick={handleLogout}
              disabled={isLoggingOut}
              aria-label="Logout"
            >
              {isLoggingOut ? 'Logging out...' : 'Logout'}
            </button>
          </div>
        </div>

        {/* TOP NAV — desktop-first navigation */}
        <nav className="bp-top-nav" aria-label="main navigation">
          <div className={`bp-nav-item ${screen === 'home' ? 'active' : ''}`} onClick={() => navigate('home')} role="button">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M3 11.5L12 4l9 7.5" strokeWidth="1.4"/></svg>
            <div>Home</div>
          </div>

          <div className={`bp-nav-item ${screen === 'groups' ? 'active' : ''}`} onClick={() => navigate('groups')} role="button">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M17 21v-2a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v2" strokeWidth="1.4"/></svg>
            <div>Groups</div>
          </div>

          <div className={`bp-nav-item ${screen === 'receipts' ? 'active' : ''}`} onClick={() => navigate('receipts')} role="button">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M9 9h6M9 13h6M3 7h18v10a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" strokeWidth="1.4"/></svg>
            <div>Receipts</div>
          </div>

          <div className={`bp-nav-item ${screen === 'data' ? 'active' : ''}`} onClick={() => navigate('data')} role="button">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M3 3v18h18" strokeWidth="1.4"/></svg>
            <div>Data</div>
          </div>

          <div className={`bp-nav-item ${screen === 'chat' ? 'active' : ''}`} onClick={() => navigate('chat')} role="button">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M21 15a2 2 0 0 0-2-2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" strokeWidth="1.4"/></svg>
            <div>AI</div>
          </div>

          {/* Alerts — no image currently. */}
          <div className={`bp-nav-item ${screen === 'alerts' ? 'active' : ''}`} onClick={() => navigate('alerts')} role="button">
            <div>Alerts</div>
          </div>

          {/* Notifications — no image currently. */}
          <div className={`bp-nav-item ${screen === 'notifications' ? 'active' : ''}`} onClick={() => navigate('notifications')} role="button">
            <div>Notifications</div>
          </div>

          {/* Map (Goals) page — no image */}
          <div className={`bp-nav-item ${screen === 'map' ? 'active' : ''}`} onClick={() => navigate('map')} role="button">
            <div>Map</div>
          </div>

          <div style={{ marginLeft: 'auto' }} />

          <div className={`bp-nav-item ${screen === 'profile' ? 'active' : ''}`} onClick={() => navigate('profile')} role="button">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M12 12a5 5 0 1 0 0-10 5 5 0 0 0 0 10zM3 22v-1a4 4 0 0 1 4-4h10a4 4 0 0 1 4 4v1" strokeWidth="1.4"/></svg>
            <div>Profile</div>
          </div>

          {/* Support — no image currently */}
          <div className={`bp-nav-item ${screen === 'support' ? 'active' : ''}`} onClick={() => navigate('support')} role="button">
            <div>Support</div>
          </div>
        </nav>

        {/* Main content router area */}
        <div style={{ marginTop: 14 }}>
          {screen === 'home' && (
            <>
              <div className="bp-section-title">Budget Summary</div>

              <div className="bp-row" style={{ marginTop: 10 }}>
                <div className="bp-box" style={{ flex: 1 }}>
                  <div className="label">Total Spent</div>
                  <div className="value" style={{ color: '#ff6b6b' }}>-€{Math.abs(totalSpent).toFixed(2)}</div>
                </div>
              </div>

              <div className="bp-section-title">Recent Transactions</div>

              <div className="bp-tx-list">
                {recentTx.length === 0 ? (
                  <div style={{ textAlign: 'center', color: 'var(--muted-dark)', padding: 20 }}>
                    No recent transactions
                  </div>
                ) : (
                  recentTx.map(tx => (
                    <div className="bp-tx" key={tx.id}>
                      <div className="bp-thumb">{tx.thumb}</div>
                      <div className="bp-meta">
                        <div className="bp-tx-title">{tx.title}</div>
                        <div className="bp-tx-cat">{tx.cat}</div>
                      </div>
                      <div className={`bp-amount ${tx.amount < 0 ? 'negative' : 'positive'}`}>
                        {tx.amount < 0 ? '-' : '+'}€{Math.abs(tx.amount)}
                      </div>
                    </div>
                  ))
                )}
              </div>

              <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
                <button className="bp-add-btn" onClick={() => setScreen('receipts')}>Add Receipts</button>
              </div>

              {/* NEW: Embedded receipts view on Home (moved from Receipts -> View) */}
              <div className="bp-section-title" style={{ marginTop: 18 }}>All receipts</div>
              <div style={{ marginTop: 8 }}>
                <div className="receipts-grid-container">
                  <ReceiptsView />
                </div>
              </div>

              <div className="bp-dotted" />
            </>
          )}

          {screen === 'groups' && <Groups navigate={navigate} openGroup={openGroup} />}

          {screen === 'groupDetail' && selectedGroupId !== null && (
            <GroupDetail groupId={selectedGroupId} onBack={() => setScreen('groups')} />
          )}

          {screen === 'profile' && <Profile onRequestNavigate={(t) => setScreen(t)} />}

          {screen === 'support' && <Support navigate={(t: any) => setScreen(t)} />}

          {screen === 'receipts' && <Receipts navigate={(t: string) => setScreen(t as any)} />}

          {screen === 'chat' && <ChatBot />}

          {screen === 'data' && <Data />}

          {screen === 'alerts' && <Alerts />}

          {screen === 'notifications' && <Notifications />}

          {screen === 'map' && <Map />}
        </div>

        {/* BOTTOM NAV — kept for mobile (hidden on desktop by CSS) */}
        <nav className="bp-bottom-nav" aria-label="bottom navigation">
          <div className={`bp-nav-item ${screen === 'home' ? 'active' : ''}`} onClick={() => navigate('home')}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M3 11.5L12 4l9 7.5" strokeWidth="1.4"/></svg>
            <div>Home</div>
          </div>

          <div className={`bp-nav-item ${screen === 'groups' ? 'active' : ''}`} onClick={() => navigate('groups')}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M17 21v-2a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v2" strokeWidth="1.4"/></svg>
            <div>Groups</div>
          </div>

          <div className={`bp-nav-item ${screen === 'receipts' ? 'active' : ''}`} onClick={() => navigate('receipts')}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M9 9h6M9 13h6M3 7h18v10a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" strokeWidth="1.4"/></svg>
            <div>Receipts</div>
          </div>

          <div className={`bp-nav-item ${screen === 'data' ? 'active' : ''}`} onClick={() => navigate('data')}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M3 3v18h18" strokeWidth="1.4"/></svg>
            <div>Data</div>
          </div>

          <div className={`bp-nav-item ${screen === 'chat' ? 'active' : ''}`} onClick={() => navigate('chat')}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M21 15a2 2 0 0 0-2-2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" strokeWidth="1.4"/></svg>
            <div>AI</div>
          </div>

          <div className={`bp-nav-item ${screen === 'profile' ? 'active' : ''}`} onClick={() => navigate('profile')}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor"><path d="M12 12a5 5 0 1 0 0-10 5 5 0 0 0 0 10zM3 22v-1a4 4 0 0 1 4-4h10a4 4 0 0 1 4 4v1" strokeWidth="1.4"/></svg>
            <div>Profile</div>
          </div>
        </nav>
      </div>
    </div>
  );
};

export default Dashboard;

