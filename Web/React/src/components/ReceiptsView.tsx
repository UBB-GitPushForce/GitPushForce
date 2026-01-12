import React, { useCallback, useEffect, useRef, useState } from 'react';
import '../App.css';
import type { ReceiptItem } from './Receipts';
import { useAuth } from '../hooks/useAuth';
import { useCurrency } from '../contexts/CurrencyContext';
import apiClient from '../services/api-client';
import categoryService, { Category } from '../services/category-service';

/*
  ReceiptsView: infinite scroll grid with filters and delete action (mock).
  - injectedReceipts: items injected by upload/manual/camera; these are shown on top.
  - DONE: replaced mock fetchPage & delete with server API calls.
*/

const PAGE_SIZE = 18;

const mockGroups = [
  { id: 1, name: 'Vacation' },
  { id: 2, name: 'Household' },
  { id: 3, name: 'Friends' },
];

function randomDate(start: Date, end: Date) {
  const t = new Date(start.getTime() + Math.random() * (end.getTime() - start.getTime()));
  return t.toISOString().slice(0, 10);
}

// Fetch categories without caching to ensure fresh data
async function getCategories(userId?: number): Promise<Category[]> {
  try {
    // Fetch all categories to map category_id to title for all expenses (including group expenses)
    return await categoryService.getCategories();
  } catch (err) {
    console.error('Failed to fetch categories', err);
    return [];
  }
}


// DONE: paged fetch
async function fetchPage(pageIndex: number, pageSize: number, filters: any) {
  try {
    const params: any = {
      offset: pageIndex * pageSize,
      limit: pageSize,
      sort_by: filters.sortBy || 'created_at',
      order: filters.sortOrder || 'desc',
    };

    // Backend supported filters:
    // category: string (category title, resolved to IDs on backend)
    if (filters.selectedCategory) params.category = filters.selectedCategory;

    // amount range: min_price, max_price
    if (filters.minAmount != null) params.min_price = filters.minAmount;
    if (filters.maxAmount != null) params.max_price = filters.maxAmount;

    // date filters: date_from, date_to (ISO date strings)
    if (filters.dateAddedFrom) params.date_from = filters.dateAddedFrom;
    if (filters.dateAddedTo) params.date_to = filters.dateAddedTo;

    // Expense type filter: group, individual, or all
    // Backend doesn't have a direct filter, so we'll filter on frontend after fetching
    // For group expenses, we could use group_ids param, but we don't know all group IDs
    // So we fetch all and filter client-side based on whether expense has group_id
    
    const res = await apiClient.get('/expenses', { params });

    // Backend returns APIResponse { success: true, data: [expenses] }
    const responseData = res.data;
    const items: any[] = Array.isArray(responseData) ? responseData : (responseData?.data || []);
    
    const categories = await getCategories(filters.userId);
    const categoryMap = new Map(categories.map(c => [c.id, c.title]));

    // Fetch group names for expenses that have group_id
    const uniqueGroupIds = [...new Set(items.filter(x => x.group_id).map(x => x.group_id))];
    const groupNameMap = new Map<number, string>();
    
    await Promise.all(
      uniqueGroupIds.map(async (groupId: any) => {
        try {
          const groupRes = await apiClient.get(`/groups/${groupId}`);
          const groupData = groupRes.data?.data || groupRes.data;
          groupNameMap.set(groupId, groupData.name || `Group ${groupId}`);
        } catch (err) {
          console.error(`Failed to fetch group ${groupId}`, err);
          groupNameMap.set(groupId, `Group ${groupId}`);
        }
      })
    );

    const pageItems: ReceiptItem[] = items.map((x: any) => ({
      id: x.id,
      title: x.title,
      subtitle: categoryMap.get(x.category_id) || 'Uncategorized',
      amount: x.amount,
      dateTransaction: x.created_at ? x.created_at.slice(0, 10) : '',
      dateAdded: x.created_at ? x.created_at.slice(0, 10) : '',
      isGroup: !!x.group_id,
      groupId: x.group_id || undefined,
      groupName: x.group_id ? groupNameMap.get(x.group_id) : undefined,
      addedBy: x.user_id ? `User ${x.user_id}` : 'Unknown',
      initial: x.title ? x.title[0].toUpperCase() : 'E',
      userId: x.user_id,
    }));

    // Frontend filtering
    let filtered = pageItems;
    
    // Title filtering (backend doesn't support it)
    if (filters.qTitle) {
      filtered = filtered.filter(item => item.title.toLowerCase().includes(filters.qTitle.toLowerCase()));
    }
    
    // Expense type filtering
    if (filters.expenseType === 'group') {
      filtered = filtered.filter(item => item.isGroup);
    } else if (filters.expenseType === 'individual') {
      filtered = filtered.filter(item => !item.isGroup);
    }
    // 'all' means no filter

    const hasMore = items.length === pageSize;

    return { items: filtered, hasMore };
  } catch (err) {
    console.error('Failed to fetch expenses', err);
    return { items: [], hasMore: false };
  }
}

//delete API call
async function deleteExpense(id: number) {
  // DONE: call DELETE /expenses/{id}
  try {
    await apiClient.delete(`/expenses/${id}`);
    return true;
  } catch (err) {
    console.error('Failed to delete expense', err);
    throw err;
  }
}

//update API call
async function updateExpense(id: number, body: any) {
  try {
    const res = await apiClient.put(`/expenses/${id}`, body);
    return res.data;
  } catch (err) {
    console.error('Failed to update expense', err);
    throw err;
  }
}

const ReceiptsView: React.FC<{ onNeedRefresh?: () => void; refreshKey?: number }> = ({ onNeedRefresh, refreshKey }) => {
  const { user } = useAuth();
  const [items, setItems] = useState<ReceiptItem[]>([]);
  const [pageIndex, setPageIndex] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  const[editingId, setEditingId] = useState<number | null>(null);
  const[editForm, setEditForm] = useState({ title: '', subtitle: '', amount: '', categoryId: null as number | null });
  const[allCategories, setAllCategories] = useState<Category[]>([]);
  const[paymentStatus, setPaymentStatus] = useState<Map<number, boolean>>(new Map());

  const [filters, setFilters] = useState<any>({ userId: user?.id });
  const sentinelRef = useRef<HTMLDivElement | null>(null);

  const [local, setLocal] = useState({
    qTitle: '',
    minAmount: '',
    maxAmount: '',
    dateAddedFrom: '',
    dateAddedTo: '',
    selectedCategory: '',
    sortBy: 'created_at',
    sortOrder: 'desc',
    expenseType: 'all' as 'all' | 'group' | 'individual', // New filter for expense type
  });

  const [categories, setCategories] = useState<string[]>([]);

  // Fetch all categories on component mount
  useEffect(() => {
    const fetchAllCategories = async () => {
      try {
        const cats = await categoryService.getCategories(user?.id);
        setAllCategories(cats);
      } catch (err) {
        console.error('Failed to fetch categories', err);
      }
    };
    fetchAllCategories();
  }, []);

  // Extract unique categories from items - accumulate, don't replace
  useEffect(() => {
    const newCategories = items.map(item => item.subtitle).filter(Boolean);
    setCategories(prev => {
      const combined = new Set([...prev, ...newCategories]);
      return Array.from(combined);
    });
  }, [items]);

  const loadFirst = useCallback(async () => {
    setLoading(true);
    try {
      const res = await fetchPage(0, PAGE_SIZE, filters);
      setItems(res.items);
      setHasMore(res.hasMore);
      setPageIndex(1);
    } finally {
      setLoading(false);
    }
  }, [filters]);

  useEffect(() => { loadFirst(); }, [loadFirst, refreshKey]);

  // Fetch payment status for group expenses
  useEffect(() => {
    const fetchPaymentStatus = async () => {
      if (!user?.id) return;
      
      const groupExpenses = items.filter(item => item.isGroup);
      const statusMap = new Map<number, boolean>();
      
      await Promise.all(
        groupExpenses.map(async (expense) => {
          try {
            const res = await apiClient.get(`/expenses_payments/${expense.id}/payments`);
            const payments = res.data?.data || res.data || [];
            // Check if current user has paid
            const hasPaid = payments.some((p: any) => p.user_id === user.id);
            // If user is the owner, they are implicitly paid
            const isPaid = expense.userId === user.id || hasPaid;
            statusMap.set(expense.id, isPaid);
          } catch (err) {
            console.error(`Failed to fetch payment status for expense ${expense.id}`, err);
            // If user owns the expense, mark as paid by default
            statusMap.set(expense.id, expense.userId === user.id);
          }
        })
      );
      
      setPaymentStatus(statusMap);
    };
    
    if (items.length > 0) {
      fetchPaymentStatus();
    }
  }, [items, user?.id]);

  const startEdit = (item: ReceiptItem) => {
  setEditingId(item.id);
  
  // Find the category ID from the subtitle (category title)
  const category = allCategories.find(c => c.title === item.subtitle);
  
  setEditForm({
    title: item.title,
    subtitle: item.subtitle,
    amount: String(item.amount),
    categoryId: category?.id || null,
  });
};

const cancelEdit = () => {
  setEditingId(null);
  setEditForm({ title: '', subtitle: '', amount: '', categoryId: null });
};

const saveEdit = async () => {
  if (!editingId || !user) return;
  
  try {
    // Backend ExpenseUpdate only supports: title, amount, description
    const body: any = {
      title: editForm.title.trim(),
      amount: Number(editForm.amount),
    };

    const updated = await updateExpense(editingId, body);

    setItems(prev => prev.map(x => 
      x.id === editingId 
        ? { 
            ...x, 
            title: updated.title || editForm.title, 
            amount: updated.amount !== undefined ? updated.amount : Number(editForm.amount)
          }
        : x
    ));

    cancelEdit();
  } catch (err: any) {
    console.error(err);
    alert(err?.response?.data?.detail || 'Update failed');
  }
};

  const loadMore = useCallback(async () => {
    if (!hasMore || loading) return;
    setLoading(true);
    try {
      const res = await fetchPage(pageIndex, PAGE_SIZE, filters);
      setItems(prev => [...prev, ...res.items]);
      setHasMore(res.hasMore);
      setPageIndex(prev => prev + 1);
    } finally {
      setLoading(false);
    }
  }, [pageIndex, hasMore, filters, loading]);

  useEffect(() => {
    const node = sentinelRef.current;
    if (!node) return;
    const io = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting) loadMore();
      });
    }, { rootMargin: '400px' });
    io.observe(node);
    return () => io.disconnect();
  }, [loadMore]);

  const onApply = () => {
    const f: any = {
      selectedCategory: local.selectedCategory || undefined,
      minAmount: local.minAmount ? Number(local.minAmount) : null,
      maxAmount: local.maxAmount ? Number(local.maxAmount) : null,
      dateAddedFrom: local.dateAddedFrom || null,
      dateAddedTo: local.dateAddedTo || null,
      sortBy: local.sortBy,
      sortOrder: local.sortOrder,
      userId: user?.id, // Add userId for category fetching
      expenseType: local.expenseType, // Add expense type filter
    };
    
    // Store title filter for frontend filtering (backend doesn't support title search)
    f.qTitle = local.qTitle?.trim() || undefined;
    
    setItems([]); setPageIndex(0); setHasMore(true);
    setFilters(f);
  };

  const onClear = () => {
    setLocal({
      qTitle: '',
      minAmount: '',
      maxAmount: '',
      dateAddedFrom: '',
      dateAddedTo: '',
      selectedCategory: '',
      sortBy: 'created_at',
      sortOrder: 'desc',
      expenseType: 'all',
    });
    setFilters({ userId: user?.id }); setItems([]); setPageIndex(0); setHasMore(true);
  };

  const handleDelete = async (id: number) => {
    const ok = confirm('Are you sure you want to delete this receipt?');
    if (!ok) return;
    try {
      // DONE: call API to delete the expense permanently
      await deleteExpense(id);
      setItems(prev => prev.filter(x => x.id !== id));
      // notify parent (Dashboard) so it can refresh totals and recent transactions
      onNeedRefresh && onNeedRefresh();
      alert('Deleted expense succesfully');
    } catch (err: any) {
      console.error(err);
      alert(err?.response?.data?.detail || 'Delete failed.');
    }
  };

  return (
    <div>
      <div style={{ display:'flex', flexDirection:'column', gap:10 }}>
        <div style={{ display:'flex', gap:8, flexWrap:'wrap', alignItems:'center' }}>
          <input placeholder="Search title" value={local.qTitle} onChange={e=>setLocal(s=>({...s, qTitle: e.target.value}))} style={{ flex:'1 1 200px', padding:10, borderRadius:8, border:'1px solid #e4e4ee' }} />
          <input type="date" value={local.dateAddedFrom} onChange={e=>setLocal(s=>({...s, dateAddedFrom: e.target.value}))} style={{ flex:'1 1 160px', padding:10, borderRadius:8, border:'1px solid #e4e4ee' }} />
          <input type="date" value={local.dateAddedTo} onChange={e=>setLocal(s=>({...s, dateAddedTo: e.target.value}))} style={{ flex:'1 1 160px', padding:10, borderRadius:8, border:'1px solid #e4e4ee' }} />
          <input placeholder="Min amount" type="number" value={local.minAmount} onChange={e=>setLocal(s=>({...s, minAmount: e.target.value}))} style={{ width:120, padding:10, borderRadius:8, border:'1px solid #e4e4ee' }} />
          <input placeholder="Max amount" type="number" value={local.maxAmount} onChange={e=>setLocal(s=>({...s, maxAmount: e.target.value}))} style={{ width:120, padding:10, borderRadius:8, border:'1px solid #e4e4ee' }} />
          <select value={local.selectedCategory} onChange={e=>setLocal(s=>({...s, selectedCategory: e.target.value}))} style={{ padding:8, borderRadius:8, minWidth:150 }}>
            <option value="">All Categories</option>
            {categories.map(cat => (
              <option key={cat} value={cat}>{cat}</option>
            ))}
          </select>
          <select value={local.sortBy} onChange={e=>setLocal(s=>({...s, sortBy: e.target.value}))} style={{ padding:8, borderRadius:8, minWidth:130 }}>
            <option value="created_at">Sort by Date</option>
            <option value="amount">Sort by Amount</option>
            <option value="title">Sort by Title</option>
          </select>
          <select value={local.sortOrder} onChange={e=>setLocal(s=>({...s, sortOrder: e.target.value}))} style={{ padding:8, borderRadius:8, minWidth:110 }}>
            <option value="desc">Descending</option>
            <option value="asc">Ascending</option>
          </select>
          <button 
            className="bp-add-btn" 
            onClick={() => setLocal(s=>({...s, expenseType: 'all'}))}
            style={{ 
              background: local.expenseType === 'all' ? 'var(--purple-1)' : '#fff',
              color: local.expenseType === 'all' ? '#fff' : 'var(--purple-1)',
              border: '2px solid var(--purple-1)',
            }}
          >
            All
          </button>
          <button 
            className="bp-add-btn" 
            onClick={() => setLocal(s=>({...s, expenseType: 'group'}))}
            style={{ 
              background: local.expenseType === 'group' ? 'var(--purple-1)' : '#fff',
              color: local.expenseType === 'group' ? '#fff' : 'var(--purple-1)',
              border: '2px solid var(--purple-1)',
            }}
          >
            Group
          </button>
          <button 
            className="bp-add-btn" 
            onClick={() => setLocal(s=>({...s, expenseType: 'individual'}))}
            style={{ 
              background: local.expenseType === 'individual' ? 'var(--purple-1)' : '#fff',
              color: local.expenseType === 'individual' ? '#fff' : 'var(--purple-1)',
              border: '2px solid var(--purple-1)',
            }}
          >
            Individual
          </button>
        </div>
        
        <div style={{ display:'flex', gap:8 }}>
          <button className="bp-add-btn" onClick={onApply}>Apply</button>
          <button className="bp-add-btn" onClick={onClear}>Clear</button>
        </div>
      </div>

      <div style={{ marginTop: 12 }}>
        <div
          style={{
            display: 'grid',
            gap: 12,
            gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
            alignItems: 'start',
          }}
        >
          {items.map(it => (
            <article key={it.id} className="bp-tx" style={{ flexDirection:'column', alignItems:'stretch', padding:12 }}>
              {editingId === it.id ? (
            // EDIT MODE
              <div style={{ display:'grid', gap:10 }}>
                <input 
                  placeholder="Title" 
                  value={editForm.title} 
                  onChange={e => setEditForm(prev => ({ ...prev, title: e.target.value }))}
                  style={{ padding:8, borderRadius:6, border:'1px solid #e4e4ee' }}
                />
                <select 
                  value={editForm.categoryId || ''} 
                  onChange={e => {
                    const categoryId = parseInt(e.target.value);
                    const category = allCategories.find(c => c.id === categoryId);
                    setEditForm(prev => ({ 
                      ...prev, 
                      categoryId: categoryId,
                      subtitle: category?.title || '' 
                    }));
                  }}
                  style={{ padding:8, borderRadius:6, border:'1px solid #e4e4ee', background:'#fff' }}
                  disabled
                  title="Category cannot be changed after creation"
                >
                  <option value="">Select Category</option>
                  {allCategories.map(cat => (
                    <option key={cat.id} value={cat.id}>{cat.title}</option>
                  ))}
                </select>
                <input 
                  placeholder="Amount" 
                  type="number"
                  value={editForm.amount} 
                  onChange={e => setEditForm(prev => ({ ...prev, amount: e.target.value }))}
                  style={{ padding:8, borderRadius:6, border:'1px solid #e4e4ee' }}
                />
                <div style={{ display:'flex', gap:8 }}>
                  <button className="bp-add-btn" onClick={saveEdit}>Save</button>
                  <button className="bp-add-btn" onClick={cancelEdit}>Cancel</button>
                </div>
              </div>
          ) : (
            //VIEW MODE
            <>
              <div style={{ display:'flex', alignItems:'center', gap:12 }}>
                <div className="bp-thumb" style={{ width:56, height:56, display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center', background:'#fff', fontWeight:800, boxShadow:'0 4px 14px rgba(0,0,0,0.04)', position:'relative' }}>
                  <div style={{ fontSize: 24 }}>{it.initial}</div>
                  {it.isGroup && (
                    <div style={{ 
                      fontSize: 9, 
                      fontWeight: 700, 
                      padding: '2px 6px', 
                      borderRadius: 3, 
                      marginTop: 2,
                      background: paymentStatus.get(it.id) ? '#52c41a' : '#ff4d4f', 
                      color: '#fff' 
                    }}>
                      {paymentStatus.get(it.id) ? 'PAID' : 'UNPAID'}
                    </div>
                  )}
                </div>

                <div style={{ flex:1 }}>
                  <div style={{ fontWeight:800, color:'#000', fontSize:16, marginBottom:6 }}>{it.title}</div>
                  <div>
                    <span style={{ padding:'4px 10px', borderRadius:4, background:'var(--purple-1)', color:'#fff', fontSize:12, fontWeight:600 }}>{it.subtitle}</span>
                  </div>
                </div>

                <div style={{ textAlign:'right' }}>
                  <div style={{ fontWeight:800, color: '#ff6b6b', fontSize:16 }}>{useCurrency().formatAmount(-Math.abs(it.amount))}</div>
                  <div style={{ fontSize:12, color:'var(--muted-dark)' }}>{it.dateTransaction}</div>
                </div>
              </div>

              <div style={{ marginTop: 10, display:'flex', justifyContent:'space-between', alignItems:'center', color:'var(--muted-dark)', fontSize:12 }}>
                <div>Added: {it.dateAdded}</div>
                <div style={{ display:'flex', gap:8, alignItems:'center' }}>
                  <div>{it.isGroup ? `Group: ${it.groupName}` : 'Independent'}</div>
                  <button className="bp-add-btn" onClick={() => startEdit(it)} style={{ padding:'6px 8px' }}>Edit</button>
                  <button className="bp-add-btn" onClick={() => handleDelete(it.id)} style={{ padding:'6px 8px' }}>Delete</button>
                </div>
              </div>
            </>
          )}
          </article>
        ))}
        </div>

        <div ref={sentinelRef} style={{ height: 2 }} />

        {loading && <div style={{ textAlign:'center', color:'var(--muted-dark)', marginTop:8 }}>Loading...</div>}
        {!hasMore && !loading && <div style={{ textAlign:'center', color:'var(--muted-dark)', marginTop:8 }}>End of history</div>}
      </div>
    </div>
  );
};

export default ReceiptsView;

