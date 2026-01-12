// src/components/ReceiptsManual.tsx
import React, { useState, useEffect } from 'react';
import '../App.css';
import type { ReceiptItem } from './Receipts';
import { useAuth } from '../hooks/useAuth';
import apiClient from '../services/api-client';
import categoryService, { Category } from '../services/category-service';

const ReceiptsManual: React.FC<{ onCreated: (it: ReceiptItem) => void; groupId?: number | null }> = ({ onCreated, groupId = null }) => {
  const [title, setTitle] = useState('');
  const { user } = useAuth();
  const [subtitle, setSubtitle] = useState('');
  const [amount, setAmount] = useState<string>('');
  const [categories, setCategories] = useState<Category[]>([]);
  const [selectedCategoryId, setSelectedCategoryId] = useState<number | null>(null);

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const cats = await categoryService.getCategories();
        
        // If no categories exist, create default ones
        if (cats.length === 0) {
          const defaultCategories = ['Food', 'Transport', 'Entertainment', 'Shopping', 'Bills', 'Other'];
          const createdCats: Category[] = [];
          
          for (const title of defaultCategories) {
            try {
              const createdData = await categoryService.createCategory(title);
              // Fetch the created category details
              const allCats = await categoryService.getCategories();
              const newCat = allCats.find(c => c.id === createdData.id);
              if (newCat) createdCats.push(newCat);
            } catch (err) {
              console.error(`Failed to create category ${title}`, err);
            }
          }
          
          setCategories(createdCats);
          if (createdCats.length > 0) {
            setSelectedCategoryId(createdCats[0].id);
          }
        } else {
          setCategories(cats);
          if (cats.length > 0) {
            setSelectedCategoryId(cats[0].id);
          }
        }
      } catch (err) {
        console.error('Failed to fetch categories', err);
      }
    };
    fetchCategories();
  }, []);

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!title.trim() || amount === '') {
      alert('Please provide title and amount.');
      return;
    }

    if (!user) {
      alert('You must be logged in to add a receipt.');
      return;
    }

    try {
      const categoryId = selectedCategoryId || (categories.length > 0 ? categories[0].id : 1);
      
      const body: any = {
        title: title.trim(),
        category_id: categoryId,
        amount: Number(amount),
      };

      if (groupId) {
        body.group_id = groupId;
      }

      const res = await apiClient.post('/expenses', body);

      // Backend returns APIResponse { success: true, data: { id: ... } }
      const responseData = res.data;
      const created = responseData?.data || responseData;
      
      const categoryTitle = categories.find(c => c.id === categoryId)?.title || 'Other';
      
      // Fetch group name if expense belongs to a group
      let groupName: string | undefined = undefined;
      if (groupId) {
        try {
          const groupRes = await apiClient.get(`/groups/${groupId}`);
          const groupData = groupRes.data?.data || groupRes.data;
          groupName = groupData.name || `Group ${groupId}`;
        } catch (err) {
          console.error(`Failed to fetch group ${groupId}`, err);
          groupName = `Group ${groupId}`;
        }
      }
      
      const item: ReceiptItem = {
      id: created.id,
      title: created.title,
      subtitle: categoryTitle,
      amount: created.amount,
      dateAdded: created.created_at ? created.created_at.slice(0, 10) : new Date().toISOString().slice(0, 10),
      isGroup: !!groupId,
      groupId: groupId || undefined,
      groupName: groupName,
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

      <label>Category</label>
      <select 
        value={selectedCategoryId || ''} 
        onChange={e => setSelectedCategoryId(Number(e.target.value))} 
        required
        disabled={categories.length === 0}
        style={{ padding: '8px', borderRadius: '4px', border: '1px solid var(--border)' }}
      >
        {categories.length === 0 ? (
          <option value="">Loading categories...</option>
        ) : (
          categories.map(cat => (
            <option key={cat.id} value={cat.id}>{cat.title}</option>
          ))
        )}
      </select>

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

