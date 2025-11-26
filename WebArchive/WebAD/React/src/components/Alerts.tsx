// src/components/Alerts.tsx
import React, { useEffect, useState } from 'react';
import '../App.css';
import { useAuth } from '../hooks/useAuth';

type AlertType =
  | 'ai_alerts'
  | 'spent_over_limit'
  | 'remaining_too_low'
  | 'reminder';

type AiSchedule = {
  times: string[]; // e.g. ['09:00','18:30']
  weekdays: string[]; // e.g. ['Mon','Tue']
  specificDates: string[]; // 'YYYY-MM-DD'
};

type AlertItem = {
  id: number;
  name: string;
  type: AlertType;
  enabled: boolean;
  threshold?: number;
  aiSchedule?: AiSchedule;
  reminderDate?: string; // for reminder type: YYYY-MM-DD or datetime string
  reminderNote?: string;
  createdAt: string;
};

const ALERTS_KEY = 'mock_alerts_v3';

function loadAlerts(): AlertItem[] {
  try {
    const raw = localStorage.getItem(ALERTS_KEY);
    if (!raw) return [];
    return JSON.parse(raw);
  } catch {
    return [];
  }
}

function saveAlerts(arr: AlertItem[]) {
  localStorage.setItem(ALERTS_KEY, JSON.stringify(arr));
}

const defaultAiSchedule = (): AiSchedule => ({ times: ['09:00'], weekdays: [], specificDates: [] });

const Alerts: React.FC = () => {
  const { user } = useAuth();
  const [alerts, setAlerts] = useState<AlertItem[]>(() => loadAlerts());
  const [editing, setEditing] = useState<AlertItem | null>(null);
  const [creating, setCreating] = useState(false);

  useEffect(() => {
    saveAlerts(alerts);
  }, [alerts]);

  const emitNotification = (title: string, description: string, sourceAlertId?: number) => {
    const payload = {
      id: Date.now(),
      title,
      description,
      date: new Date().toISOString(),
      read: false,
      sourceAlertId,
    };
    window.dispatchEvent(new CustomEvent('new-notification', { detail: payload }));
  };

  const addAlert = (a: Partial<AlertItem>) => {
    const id = Date.now();
    const item: AlertItem = {
      id,
      name: a.name || 'New alert',
      type: (a.type as AlertType) || 'remaining_too_low',
      enabled: a.enabled ?? true,
      threshold: a.threshold ?? 100,
      aiSchedule: a.aiSchedule ?? defaultAiSchedule(),
      reminderDate: a.reminderDate,
      reminderNote: a.reminderNote,
      createdAt: new Date().toISOString(),
    };
    setAlerts(prev => [item, ...prev]);
    setCreating(false);
  };

  const updateAlert = (id: number, patch: Partial<AlertItem>) => {
    setAlerts(prev => prev.map(a => a.id === id ? { ...a, ...patch } : a));
    setEditing(null);
  };

  const removeAlert = (id: number) => {
    if (!confirm('Remove this alert?')) return;
    setAlerts(prev => prev.filter(a => a.id !== id));
  };

  const simulateTrigger = (a: AlertItem) => {
    let title = `Alert triggered: ${a.name}`;
    let desc = '';
    switch (a.type) {
      case 'ai_alerts':
        desc = `AI analysis produced a suggestion for you.`;
        break;
      case 'spent_over_limit':
        desc = `Spent over ${a.threshold}.`;
        break;
      case 'remaining_too_low':
        desc = `Remaining budget dropped below ${a.threshold}.`;
        break;
      case 'reminder':
        desc = `Reminder: ${a.reminderNote || a.name} — due ${a.reminderDate || 'soon'}.`;
        break;
    }
    emitNotification(title, desc, a.id);
    alert('Notification emitted (mock). Check Notifications page.');
  };

  return (
    <div style={{ marginTop: 12 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div className="bp-title">Alerts</div>
        <div style={{ color: 'var(--muted-dark)' }}>Define rules that generate notifications</div>
      </div>

      <div style={{ marginTop: 12, display: 'grid', gap: 12 }}>
        <div style={{ display: 'flex', gap: 8 }}>
          <button className="bp-add-btn" onClick={() => { setCreating(true); setEditing(null); }}>+ Add alert</button>
          <div style={{ marginLeft: 'auto', display: 'flex', gap: 8 }}>
            <button className="bp-add-btn" onClick={() => { setAlerts([]); saveAlerts([]); }}>Clear all (mock)</button>
          </div>
        </div>

        <div style={{ display: 'grid', gap: 8 }}>
          {alerts.length === 0 && <div style={{ color: 'var(--muted-dark)' }}>No alerts yet. Click "Add alert" to create one.</div>}
          {alerts.map(a => (
            <div key={a.id} className="bp-box" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              {/* TODO: add small icon here (SVG or image) if later desired */}
              <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
                <div>
                  <div style={{ fontWeight: 800 }}>{a.name}</div>
                  <div style={{ color: 'var(--muted-dark)', fontSize: 13 }}>
                    Type: {a.type === 'ai_alerts' ? 'AI Alerts' : a.type === 'spent_over_limit' ? 'Spent over limit Alert' : a.type === 'remaining_too_low' ? 'Remaining too low Alert' : 'Reminder'} • {a.enabled ? 'active' : 'dezactivate'}
                  </div>
                  {a.type === 'reminder' && a.reminderDate && (
                    <div style={{ color: 'var(--muted-dark)', fontSize: 13 }}>Due: {a.reminderDate} {a.reminderNote ? `• ${a.reminderNote}` : ''}</div>
                  )}
                </div>
              </div>

              <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                <button className="bp-add-btn" onClick={() => { setEditing(a); setCreating(false); }}>Edit</button>
                <button className="bp-add-btn" onClick={() => simulateTrigger(a)}>Simulate trigger</button>
                <button className="bp-add-btn" onClick={() => removeAlert(a.id)}>Delete</button>
              </div>
            </div>
          ))}
        </div>

        {(creating || editing) && (
          <div className="bp-box" style={{ marginTop: 8 }}>
            <AlertForm
              initial={editing ?? undefined}
              onCancel={() => { setEditing(null); setCreating(false); }}
              onSave={(payload) => {
                if (editing) updateAlert(editing.id, payload);
                else addAlert(payload as any);
              }}
            />
          </div>
        )}
      </div>
    </div>
  );
};

export default Alerts;

/* -------------------------
   Subcomponent: AlertForm
   ------------------------- */

function AlertForm({ initial, onSave, onCancel }: { initial?: AlertItem; onSave: (p: Partial<AlertItem>) => void; onCancel: () => void }) {
  const [name, setName] = useState(initial?.name ?? '');
  const [type, setType] = useState<AlertType>(initial?.type ?? 'remaining_too_low');
  const [enabled, setEnabled] = useState<boolean>(initial?.enabled ?? true);
  const [threshold, setThreshold] = useState<number>(initial?.threshold ?? 100);
  const [aiSchedule, setAiSchedule] = useState<AiSchedule>(initial?.aiSchedule ?? defaultAiSchedule());
  const [reminderDate, setReminderDate] = useState<string | undefined>(initial?.reminderDate);
  const [reminderNote, setReminderNote] = useState<string | undefined>(initial?.reminderNote);

  const addTime = () => setAiSchedule(s => ({ ...s, times: [...s.times, '09:00'] }));
  const removeTime = (idx: number) => setAiSchedule(s => ({ ...s, times: s.times.filter((_,i)=>i!==idx) }));
  const updateTime = (idx: number, val: string) => setAiSchedule(s => ({ ...s, times: s.times.map((t,i)=> i===idx?val:t) }));

  const toggleWeekday = (d: string) => {
    setAiSchedule(s => s.weekdays.includes(d) ? { ...s, weekdays: s.weekdays.filter(w=>w!==d) } : { ...s, weekdays: [...s.weekdays, d] });
  };

  const addSpecificDate = () => setAiSchedule(s => ({ ...s, specificDates: [...s.specificDates, new Date().toISOString().slice(0,10)] }));
  const updateSpecificDate = (i: number, v: string) => setAiSchedule(s => ({ ...s, specificDates: s.specificDates.map((d,idx)=>idx===i?v:d) }));
  const removeSpecificDate = (i: number) => setAiSchedule(s => ({ ...s, specificDates: s.specificDates.filter((_,idx)=>idx!==i) }));

  const submit = () => {
    const payload: Partial<AlertItem> = {
      name,
      type,
      enabled,
      threshold,
      aiSchedule,
      reminderDate,
      reminderNote,
    };
    onSave(payload);
  };

  return (
    <div>
      <div style={{ fontWeight: 800, marginBottom: 8 }}>{initial ? 'Edit alert' : 'Add alert'}</div>

      <div style={{ display: 'grid', gap: 8 }}>
        <label>Name</label>
        <input value={name} onChange={e=>setName(e.target.value)} placeholder="My alert name" />

        <label>Type</label>
        <select value={type} onChange={e=>setType(e.target.value as AlertType)}>
          <option value="ai_alerts">AI Alerts</option>
          <option value="spent_over_limit">Spent over limit Alert</option>
          <option value="remaining_too_low">Remaining too low Alert</option>
          <option value="reminder">Reminder</option>
        </select>

        {(type === 'spent_over_limit' || type === 'remaining_too_low') && (
          <>
            <label>Threshold (amount)</label>
            <input type="number" value={threshold} onChange={e=>setThreshold(Number(e.target.value))} />
          </>
        )}

        {type === 'ai_alerts' && (
          <>
            <div style={{ fontWeight: 700 }}>AI schedule</div>

            <div style={{ display: 'grid', gap: 8 }}>
              <div>
                <div style={{ fontSize: 13, color: 'var(--muted-dark)' }}>Times (add multiple)</div>
                <div style={{ display:'flex', gap:8, marginTop:6, flexWrap:'wrap' }}>
                  {aiSchedule.times.map((t, idx) => (
                    <div key={idx} style={{ display:'flex', gap:6, alignItems:'center' }}>
                      <input type="time" value={t} onChange={e=>updateTime(idx, e.target.value)} />
                      <button className="bp-add-btn" onClick={()=>removeTime(idx)}>x</button>
                    </div>
                  ))}
                  <button className="bp-add-btn" onClick={addTime}>Add time</button>
                </div>
              </div>

              <div>
                <div style={{ fontSize: 13, color: 'var(--muted-dark)' }}>Weekdays</div>
                <div style={{ display:'flex', gap:6, marginTop:6, flexWrap:'wrap' }}>
                  {['Mon','Tue','Wed','Thu','Fri','Sat','Sun'].map(d => (
                    <label key={d} style={{ display:'flex', gap:6, alignItems:'center' }}>
                      <input type="checkbox" checked={aiSchedule.weekdays.includes(d)} onChange={()=>toggleWeekday(d)} />
                      {d}
                    </label>
                  ))}
                </div>
              </div>

              <div>
                <div style={{ fontSize: 13, color: 'var(--muted-dark)' }}>Specific dates</div>
                <div style={{ display:'flex', gap:8, marginTop:6, flexDirection:'column' }}>
                  {aiSchedule.specificDates.map((d,idx) => (
                    <div key={idx} style={{ display:'flex', gap:8, alignItems:'center' }}>
                      <input type="date" value={d} onChange={e=>updateSpecificDate(idx, e.target.value)} />
                      <button className="bp-add-btn" onClick={()=>removeSpecificDate(idx)}>x</button>
                    </div>
                  ))}
                  <button className="bp-add-btn" onClick={addSpecificDate}>Add date</button>
                </div>
              </div>
            </div>
          </>
        )}

        {type === 'reminder' && (
          <>
            <label>Due date</label>
            <input type="date" value={reminderDate ?? ''} onChange={e=>setReminderDate(e.target.value)} />

            <label>Note</label>
            <input value={reminderNote ?? ''} onChange={e=>setReminderNote(e.target.value)} placeholder="e.g. Pay rent" />
          </>
        )}

        <div style={{ display: 'flex', gap: 8 }}>
          <button className="bp-add-btn" onClick={submit}>{initial ? 'Save' : 'Add alert'}</button>
          <button className="bp-add-btn" onClick={onCancel}>Cancel</button>
        </div>
      </div>
    </div>
  );
}

