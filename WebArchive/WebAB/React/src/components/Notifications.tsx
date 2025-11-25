// src/components/Notifications.tsx
import React, { useEffect, useState } from 'react';
import '../App.css';

/**
 * Notifications page
 * - Listens to window 'new-notification' events (dispatched by Alerts simulate or by backend hooks)
 * - Persists notifications in localStorage key 'mock_notifications'
 * - Each notification: { id, title, description, date, read, sourceAlertId? }
 *
 * TODO: replace localStorage with real GET /notifications and mark read API calls.
 */

type NotificationItem = {
  id: number;
  title: string;
  description: string;
  date: string;
  read: boolean;
  sourceAlertId?: number;
};

const NOTIF_KEY = 'mock_notifications';

function loadNotifs(): NotificationItem[] {
  try {
    const raw = localStorage.getItem(NOTIF_KEY);
    if (!raw) return [];
    return JSON.parse(raw);
  } catch {
    return [];
  }
}
function saveNotifs(arr: NotificationItem[]) {
  localStorage.setItem(NOTIF_KEY, JSON.stringify(arr));
}

const Notifications: React.FC = () => {
  const [items, setItems] = useState<NotificationItem[]>(() => loadNotifs());

  useEffect(() => {
    saveNotifs(items);
  }, [items]);

  useEffect(() => {
    const handler = (e: any) => {
      const d = e?.detail;
      if (d && d.id) {
        setItems(prev => [{ ...d, read: false }, ...prev]);
      }
    };
    window.addEventListener('new-notification', handler as EventListener);
    return () => window.removeEventListener('new-notification', handler as EventListener);
  }, []);

  const markRead = (id: number) => {
    setItems(prev => prev.map(i => i.id === id ? { ...i, read: true } : i));
    // TODO: PATCH /notifications/{id}/read
  };

  const clearAll = () => {
    if (!confirm('Clear all notifications?')) return;
    setItems([]);
    // TODO: DELETE /notifications (or mark all read)
  };

  const mockGenerate = () => {
    const n: NotificationItem = {
      id: Date.now(),
      title: 'Mock: Weekly summary',
      description: 'Your weekly spending increased by 12% vs last week.',
      date: new Date().toISOString(),
      read: false,
    };
    setItems(prev => [n, ...prev]);
  };

  return (
    <div style={{ marginTop: 12 }}>
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
        <div style={{ fontWeight:800, fontSize:20, color:'var(--text-dark)' }}>Notifications</div>
        <div style={{ display:'flex', gap:8 }}>
          <button className="bp-add-btn" onClick={mockGenerate}>Generate mock</button>
          <button className="bp-add-btn" onClick={clearAll}>Clear all</button>
        </div>
      </div>

      <div style={{ marginTop:12 }}>
        {items.length === 0 && <div style={{ color:'var(--muted-dark)' }}>No notifications yet.</div>}
        <div style={{ display:'grid', gap:8 }}>
          {items.map(it => (
            <div key={it.id} className="bp-box" style={{ display:'flex', justifyContent:'space-between', alignItems:'flex-start', gap:8 }}>
              <div style={{ flex:1 }}>
                <div style={{ fontWeight:800 }}>{it.title}</div>
                <div style={{ color:'var(--muted-dark)', fontSize:13, marginTop:6 }}>{it.description}</div>
                <div style={{ fontSize:12, color:'var(--muted-dark)', marginTop:8 }}>{new Date(it.date).toLocaleString()}</div>
              </div>

              <div style={{ display:'flex', flexDirection:'column', gap:8, alignItems:'flex-end' }}>
                {!it.read ? <button className="bp-add-btn" onClick={() => markRead(it.id)} style={{ padding:'6px 10px' }}>Mark read</button> : <div style={{ color:'var(--muted-dark)' }}>Read</div>}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default Notifications;

