// src/components/Map.tsx
import React, { useEffect, useState } from 'react';
import '../App.css';

type GoalItem = {
  id: number;
  title: string;
  note?: string;
  targetDate?: string; // YYYY-MM-DD
  done: boolean;
  createdAt: string;
};

const GOALS_KEY = 'mock_goals_v1';

function loadGoals(): GoalItem[] {
  try {
    const raw = localStorage.getItem(GOALS_KEY);
    if (!raw) return [];
    return JSON.parse(raw);
  } catch {
    return [];
  }
}

function saveGoals(arr: GoalItem[]) {
  localStorage.setItem(GOALS_KEY, JSON.stringify(arr));
}

const Map: React.FC = () => {
  const [goals, setGoals] = useState<GoalItem[]>(() => loadGoals());
  const [title, setTitle] = useState('');
  const [note, setNote] = useState('');
  const [targetDate, setTargetDate] = useState('');

  useEffect(() => {
    saveGoals(goals);
  }, [goals]);

  const addGoal = () => {
    if (!title.trim()) { alert('Please provide a title'); return; }
    const g: GoalItem = {
      id: Date.now(),
      title: title.trim(),
      note: note.trim() || undefined,
      targetDate: targetDate || undefined,
      done: false,
      createdAt: new Date().toISOString(),
    };
    setGoals(prev => [g, ...prev]);
    setTitle(''); setNote(''); setTargetDate('');
  };

  const toggleDone = (id: number) => {
    setGoals(prev => prev.map(g => g.id === id ? { ...g, done: !g.done } : g));
    const g = goals.find(x => x.id === id);
    if (g && !g.done) {
      // emit a notification when marking done
      const payload = {
        id: Date.now(),
        title: `Goal completed: ${g.title}`,
        description: `You marked "${g.title}" as done.`,
        date: new Date().toISOString(),
        read: false,
        source: 'map'
      };
      window.dispatchEvent(new CustomEvent('new-notification', { detail: payload }));
    }
  };

  const removeGoal = (id: number) => {
    if (!confirm('Remove this item?')) return;
    setGoals(prev => prev.filter(g => g.id !== id));
  };

  return (
    <div style={{ marginTop: 12 }}>
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
        <div className="bp-title">Map (Goals)</div>
        <div style={{ color:'var(--muted-dark)' }}>Add items you want to do and mark them done</div>
      </div>

      <div style={{ marginTop:12, display:'grid', gap:8 }}>
        <div style={{ display:'grid', gridTemplateColumns: '1fr auto', gap:8 }}>
          <input placeholder="New item title" value={title} onChange={e=>setTitle(e.target.value)} />
          <button className="bp-add-btn" onClick={addGoal}>Add</button>
        </div>

        <input placeholder="Note (optional)" value={note} onChange={e=>setNote(e.target.value)} />
        <div style={{ display:'flex', gap:8, alignItems:'center' }}>
          <div style={{ fontSize:13, color:'var(--muted-dark)' }}>Target date (optional)</div>
          <input type="date" value={targetDate} onChange={e=>setTargetDate(e.target.value)} />
        </div>

        <div style={{ marginTop: 8 }}>
          {goals.length === 0 && <div style={{ color:'var(--muted-dark)' }}>No goals yet. Add one above.</div>}

          <div style={{ display:'grid', gap:10, marginTop:6 }}>
            {goals.map(g => (
              <div key={g.id} className="bp-box" style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
                <div>
                  <div style={{ fontWeight:800, textDecoration: g.done ? 'line-through' : 'none' }}>{g.title}</div>
                  {g.note && <div style={{ color:'var(--muted-dark)', fontSize:13 }}>{g.note}</div>}
                  {g.targetDate && <div style={{ color:'var(--muted-dark)', fontSize:12 }}>Target: {g.targetDate}</div>}
                </div>

                <div style={{ display:'flex', gap:8 }}>
                  <button className="btn" onClick={() => toggleDone(g.id)}>{g.done ? 'Undo' : 'Done'}</button>
                  <button className="btn" onClick={() => removeGoal(g.id)}>Delete</button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Map;

