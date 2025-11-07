// src/components/ReceiptsManual.tsx
import React, { useState } from 'react';
import '../App.css';
import type { ReceiptItem } from './Receipts';

const ReceiptsManual: React.FC<{ onCreated: (it: ReceiptItem) => void; groupId?: number | null }> = ({ onCreated, groupId = null }) => {
  const [title, setTitle] = useState('');
  const [subtitle, setSubtitle] = useState('');
  const [amount, setAmount] = useState<number | ''>('');
  const [dateTx, setDateTx] = useState<string>(new Date().toISOString().slice(0,10));

  const mockGroups = [
    { id: 1, name: 'Vacation' },
    { id: 2, name: 'Household' },
    { id: 3, name: 'Friends' },
  ];

  const onSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || amount === '') {
      alert('Please provide title and amount.');
      return;
    }

    // TODO: call API to create the receipt
    const now = new Date().toISOString().slice(0,10);
    const item: ReceiptItem = {
      id: Date.now(),
      title: title.trim(),
      subtitle: subtitle.trim() || 'Manual',
      amount: Number(amount),
      dateTransaction: dateTx,
      dateAdded: now,
      isGroup: !!groupId,
      groupId: groupId || undefined,
      groupName: groupId ? (mockGroups.find(g=>g.id===groupId)?.name) : undefined,
      addedBy: 'You',
      initial: 'Y',
    };

    onCreated(item);

    // reset
    setTitle(''); setSubtitle(''); setAmount(''); setDateTx(new Date().toISOString().slice(0,10));
  };

  return (
    <form onSubmit={onSubmit} style={{ display:'grid', gap:10 }}>
      <label>Title</label>
      <input type="text" value={title} onChange={e=>setTitle(e.target.value)} placeholder="E.g. Grocery" required />

      <label>Subtitle / Category</label>
      <input type="text" value={subtitle} onChange={e=>setSubtitle(e.target.value)} placeholder="E.g. Food" />

      <label>Amount</label>
      <input type="number" value={amount as any} onChange={e=>setAmount(e.target.value === '' ? '' : Number(e.target.value))} placeholder="-50" required />

      <label>Transaction date</label>
      <input type="date" value={dateTx} onChange={e=>setDateTx(e.target.value)} />

      {groupId && (
        <div style={{ color:'var(--muted-dark)', fontSize:13 }}>Linked to group: {mockGroups.find(g=>g.id===groupId)?.name}</div>
      )}

      <div style={{ display:'flex', gap:8 }}>
        <button className="bp-add-btn" type="submit">Create</button>
        <button type="button" className="btn" onClick={() => { setTitle(''); setSubtitle(''); setAmount(''); setDateTx(new Date().toISOString().slice(0,10)); }}>Reset</button>
      </div>
    </form>
  );
};

export default ReceiptsManual;

