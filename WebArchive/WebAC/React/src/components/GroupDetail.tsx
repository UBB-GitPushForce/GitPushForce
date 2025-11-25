// src/components/GroupDetail.tsx
import React, { useEffect, useState } from 'react';
import '../App.css';

interface Expense {
    id: number;
    title: string;
    category: string;
    amount: number;
    userName: string;
    userInitial: string;
    date: string;
}

interface Props {
    groupId: number | null;
    onBack: () => void;
}

const GroupDetail: React.FC<Props> = ({ groupId, onBack }) => {
    const [groupName, setGroupName] = useState<string | null>(null);
    const [expenses, setExpenses] = useState<Expense[]>([]);

    useEffect(() => {
        if (!groupId) return;

        // Mock fetch group info and expenses
        // TODO: replace with API call e.g. GET /groups/{groupId} and GET /groups/{groupId}/expenses
        const mockGroupNames: Record<number,string> = {
            1: 'Vacation',
            2: 'Household',
            3: 'Friends',
        };

        setGroupName(mockGroupNames[groupId] || `Group ${groupId}`);

        // Sample/mock expenses (chat-like)
        const mockExpenses: Expense[] = [
            { id: 1, title: 'Hotel booking', category: 'Travel', amount: -420, userName: 'Alice', userInitial: 'A', date: '2025-10-01' },
            { id: 2, title: 'Dinner', category: 'Food', amount: -85, userName: 'Bob', userInitial: 'B', date: '2025-10-02' },
            { id: 3, title: 'Train tickets', category: 'Travel', amount: -120, userName: 'Charlie', userInitial: 'C', date: '2025-10-03' },
            { id: 4, title: 'Refund', category: 'Adjustment', amount: +50, userName: 'Alice', userInitial: 'A', date: '2025-10-04' },
        ];

        // Simulate network delay for realism (optional)
        const t = setTimeout(() => setExpenses(mockExpenses), 120);
        return () => clearTimeout(t);
    }, [groupId]);

    return (
        <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginTop: 12 }}>
                <div
                    onClick={onBack}
                    style={{ display: 'inline-flex', alignItems: 'center', gap: 8, cursor: 'pointer', color: 'var(--purple-1)', fontWeight: 700 }}
                    aria-label="Back to groups"
                >
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                        <path d="M15 18L9 12L15 6" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
                    </svg>
                    Back
                </div>

                <div style={{ fontWeight: 800, fontSize: 20, color: 'var(--text-dark)' }}>
                    {groupName ?? 'Group'}
                </div>
            </div>

            <div style={{ marginTop: 12, color: 'var(--muted-dark)', fontSize: 14 }}>
                Expenses for this group (chat-style)
            </div>

            <div style={{ marginTop: 12, display: 'flex', flexDirection: 'column', gap: 12 }}>
                {expenses.map(e => (
                    <div key={e.id} style={{ display: 'flex', gap: 12, alignItems: 'flex-start' }}>
                        {/* avatar square */}
                        <div style={{
                            width: 48, height: 48, borderRadius: 10, display: 'flex',
                            alignItems: 'center', justifyContent: 'center',
                            background: 'linear-gradient(135deg, rgba(124,58,237,0.14), rgba(108,52,235,0.06))',
                            color: '#fff', fontWeight: 800, fontSize: 16, flexShrink: 0
                        }}>{e.userInitial}</div>

                        {/* main bubble */}
                        <div style={{
                            flex: 1,
                            background: 'var(--bp-bubble-bg, rgba(255,255,255,0.02))',
                            borderRadius: 12,
                            padding: 10,
                            border: '1px solid rgba(0,0,0,0.04)',
                            boxShadow: '0 6px 18px rgba(0,0,0,0.04)'
                        }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <div>
                                    <div style={{ fontWeight: 800, color: 'var(--text-dark)' }}>{e.title}</div>
                                    <div style={{ color: 'var(--muted-dark)', fontSize: 13, marginTop: 4 }}>{e.category} • {e.userName}</div>
                                </div>
                                <div style={{ marginLeft: 12, fontWeight: 800, color: e.amount < 0 ? '#ff6b6b' : '#34d399' }}>
                                    {e.amount < 0 ? '-' : '+'}${Math.abs(e.amount)}
                                </div>
                            </div>

                            <div style={{ marginTop: 8, fontSize: 12, color: 'var(--muted-dark)' }}>{e.date}</div>
                        </div>
                    </div>
                ))}
            </div>

            {/* footer action — Add expense (mock) */}
            <div style={{ marginTop: 18, display: 'flex', gap: 8 }}>
                <button
                    className="bp-add-btn"
                    onClick={() => {
                        // TODO: replace with real add-expense flow (open modal / navigate to create form)
                        const title = prompt('Expense title', 'New Expense');
                        if (!title) return;
                        const amountRaw = prompt('Amount (use negative for expense, positive for income)', '-10');
                        const amount = amountRaw ? parseFloat(amountRaw) || 0 : 0;
                        const user = prompt('Your name', 'You') || 'You';
                        const id = (expenses[expenses.length - 1]?.id || 0) + 1;
                        const newExp: Expense = {
                            id, title, category: 'Misc', amount, userName: user, userInitial: (user[0]||'U').toUpperCase(), date: new Date().toISOString().slice(0,10)
                        };
                        setExpenses(prev => [newExp, ...prev]);
                        // TODO: POST to /groups/{groupId}/expenses
                    }}
                >
                    Add Expense
                </button>

                <button
                    className="bp-add-btn"
                    onClick={() => {
                        // quick mock split summary or similar — placeholder
                        alert('Show split summary (mock). TODO: implement API-driven split details.');
                    }}
                >
                    Split summary
                </button>
            </div>
        </div>
    );
};

export default GroupDetail;

