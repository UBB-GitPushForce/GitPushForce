// src/components/ReceiptsManual.tsx
import React, { useState } from 'react';
import '../App.css';
import type { ReceiptItem } from './Receipts';
import { useAuth } from '../hooks/useAuth';
import apiClient from '../services/api-client';

const ReceiptsManual: React.FC<{ onCreated: (it: ReceiptItem) => void; groupId?: number | null }> = ({ onCreated, groupId = null }) => {
  const [title, setTitle] = useState('');
  const { user } = useAuth();
  const [subtitle, setSubtitle] = useState('');
  const [amount, setAmount] = useState<string>('');

  const mockGroups = [
    { id: 1, name: 'Vacation' },
    { id: 2, name: 'Household' },
    { id: 3, name: 'Friends' },
  ];

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || amount === '') {
      alert('Please provide title and amount.');
      return;
    }

    // DONE: call API to create the receipt

    if (!user) {
      alert('You must be logged in to add a receipt.');
      return;
    }

    try {
      const body: any = {
        title: title.trim(),
        category: subtitle.trim() || 'Manual',
        amount: Number(amount),
      };

      if (groupId) {
      body.group_id = groupId;
      } else {
        body.user_id = user.id;
      }

      const res = await apiClient.post('/expenses', body);

      const created = res.data;
      const item: ReceiptItem = {
      id: created.id,
      title: created.title,
      subtitle: created.category,
      amount: created.amount,
      dateAdded: created.created_at ? created.created_at.slice(0, 10) : new Date().toISOString().slice(0, 10),
      isGroup: !!groupId,
      groupId: groupId || undefined,
      groupName: groupId ? (mockGroups.find(g=>g.id===groupId)?.name) : undefined,
      addedBy: 'You',
      initial: 'Y',
    };

    onCreated(item);

    // reset
    setTitle(''); setSubtitle(''); setAmount('');
  } catch (err: any) {
    console.error('Failed to create receipt/expense', err);
    alert(err?.response?.data?.detail || 'Failed to create receipt');
  }

  };

  return (
    <form onSubmit={onSubmit} style={{ display:'grid', gap:10 }}>
      <label>Title</label>
      <input type="text" value={title} onChange={e=>setTitle(e.target.value)} placeholder="E.g. Grocery" required />

      <label>Subtitle / Category</label>
      <input type="text" value={subtitle} onChange={e=>setSubtitle(e.target.value)} placeholder="E.g. Food" />

      <label>Amount</label>
      <input type="number" value={amount} onChange={e=>setAmount(e.target.value)} placeholder="50" required step="any" />

      {groupId && (
        <div style={{ color:'var(--muted-dark)', fontSize:13 }}>Linked to group: {mockGroups.find(g=>g.id===groupId)?.name}</div>
      )}

      <div style={{ display:'flex', gap:8 }}>
        <button className="bp-add-btn" type="submit">Create</button>
        <button type="button" className="bp-add-btn" onClick={() => { setTitle(''); setSubtitle(''); setAmount(''); }}>Reset</button>
      </div>
    </form>
  );
};

export default ReceiptsManual;

