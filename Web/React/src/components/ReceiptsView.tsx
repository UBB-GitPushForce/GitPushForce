import React, { useCallback, useEffect, useRef, useState } from 'react';
import '../App.css';
import type { ReceiptItem } from './Receipts';
import { useAuth } from '../hooks/useAuth';
import apiClient from '../services/api-client';

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


// DONE: paged fetch
async function fetchPage(pageIndex: number, pageSize: number, filters: any) {
  try {
    const params: any = {
      offset: pageIndex * pageSize,
      limit: pageSize,
      sort_by: 'created_at',
      order: 'desc',
    };

    // fixed mappings:
    // title search
    if (filters.qTitle) params.title = filters.qTitle;
    // subtitle/category search
    if (filters.qSubtitle) params.category = filters.qSubtitle;

    // amount range (keep backend-friendly names used earlier: min_price/max_price)
    if (filters.minAmount != null) params.min_price = filters.minAmount;
    if (filters.maxAmount != null) params.max_price = filters.maxAmount;

    // date added filters (created_at)
    if (filters.dateAddedFrom) params.date_from = filters.dateAddedFrom;
    if (filters.dateAddedTo) params.date_to = filters.dateAddedTo;

    // transaction date filters (separate params)
    if (filters.dateTxFrom) params.tx_date_from = filters.dateTxFrom;
    if (filters.dateTxTo) params.tx_date_to = filters.dateTxTo;

    // onlyGroup: map to a single param the backend can interpret
    // (group / independent / any)
    if (filters.onlyGroup && filters.onlyGroup !== 'any') {
      params.only_group = filters.onlyGroup === 'group' ? 'group' : 'independent';
    }

    const res = await apiClient.get('/expenses', { params });

    const items: any[] = Array.isArray(res.data) ? res.data : [];

    const pageItems: ReceiptItem[] = items.map((x: any) => ({
      id: x.id,
      title: x.title,
      subtitle: x.category || 'Uncategorized',
      amount: x.amount,
      dateTransaction: x.created_at ? x.created_at.slice(0, 10) : '',
      dateAdded: x.created_at ? x.created_at.slice(0, 10) : '',
      isGroup: !!x.group_id,
      groupId: x.group_id || undefined,
      groupName: x.group_id ? `Group ${x.group_id}` : undefined, // TODO: fetch group name if needed
      addedBy: x.user_id ? `User ${x.user_id}` : 'Unknown',
      initial: x.user_id ? String(x.user_id)[0] : 'U',
    }));

    const hasMore = items.length === pageSize;

    return { items: pageItems, hasMore };
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

const ReceiptsView: React.FC<{ injectedReceipts?: ReceiptItem[] }> = ({ injectedReceipts = [] }) => {
  const { user } = useAuth();
  const [items, setItems] = useState<ReceiptItem[]>([]);
  const [pageIndex, setPageIndex] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  const[editingId, setEditingId] = useState<number | null>(null);
  const[editForm, setEditForm] = useState({ title: '', subtitle: '', amount: '' });

  const [filters, setFilters] = useState<any>({});
  const sentinelRef = useRef<HTMLDivElement | null>(null);

  const [local, setLocal] = useState({
    qTitle: '',
    qSubtitle: '',
    minAmount: '',
    maxAmount: '',
    dateAddedFrom: '',
    dateAddedTo: '',
    dateTxFrom: '',
    dateTxTo: '',
    onlyGroup: 'any',
  });

  const loadFirst = useCallback(async () => {
    setLoading(true);
    try {
      const res = await fetchPage(0, PAGE_SIZE, filters);
      // Filter out duplicates
      const apiIds = new Set(res.items.map(item => item.id));
      const uniqueInjected = injectedReceipts.filter(item => !apiIds.has(item.id));
      setItems([...uniqueInjected, ...res.items]);
      setHasMore(res.hasMore);
      setPageIndex(1);
    } finally {
      setLoading(false);
    }
  }, [filters, injectedReceipts]);

  useEffect(() => { loadFirst(); }, [loadFirst]);

  const startEdit = (item: ReceiptItem) => {
  setEditingId(item.id);
  setEditForm({
    title: item.title,
    subtitle: item.subtitle,
    amount: String(item.amount),
  });
};

const cancelEdit = () => {
  setEditingId(null);
  setEditForm({ title: '', subtitle: '', amount: '' });
};

const saveEdit = async () => {
  if (!editingId || !user) return;
  
  try {
    const body: any = {
      title: editForm.title.trim(),
      category: editForm.subtitle.trim() || 'Manual',
      amount: Math.abs(Number(editForm.amount)),
    };

    const item = items.find(x => x.id === editingId);
    if (item?.isGroup && item.groupId) {
      body.group_id = item.groupId;
    } else {
      body.user_id = user.id;
    }

    const updated = await updateExpense(editingId, body);

    setItems(prev => prev.map(x => 
      x.id === editingId 
        ? { ...x, title: updated.title, subtitle: updated.category, amount: updated.amount }
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

  useEffect(() => {
    if (!injectedReceipts || injectedReceipts.length === 0) return;
    setItems(prev => [...injectedReceipts, ...prev]);
  }, [injectedReceipts]);

  const onApply = () => {
    const f: any = {
      qTitle: local.qTitle?.trim() || undefined,
      qSubtitle: local.qSubtitle?.trim() || undefined,
      minAmount: local.minAmount ? Number(local.minAmount) : null,
      maxAmount: local.maxAmount ? Number(local.maxAmount) : null,
      dateAddedFrom: local.dateAddedFrom || null,
      dateAddedTo: local.dateAddedTo || null,
      dateTxFrom: local.dateTxFrom || null,
      dateTxTo: local.dateTxTo || null,
      onlyGroup: local.onlyGroup || 'any',
    };
    setItems([]); setPageIndex(0); setHasMore(true);
    setFilters(f);
  };

  const onClear = () => {
    setLocal({
      qTitle: '',
      qSubtitle: '',
      minAmount: '',
      maxAmount: '',
      dateAddedFrom: '',
      dateAddedTo: '',
      dateTxFrom: '',
      dateTxTo: '',
      onlyGroup: 'any',
    });
    setFilters({}); setItems([]); setPageIndex(0); setHasMore(true);
  };

  const handleDelete = async (id: number) => {
    const ok = confirm('Are you sure you want to delete this receipt?');
    if (!ok) return;
    try {
      // DONE: call API to delete the expense permanently
      await deleteExpense(id);
      setItems(prev => prev.filter(x => x.id !== id));
      alert('Deleted expense succesfully');
    } catch (err: any) {
      console.error(err);
      alert(err?.response?.data?.detail || 'Delete failed.');
    }
  };

  return (
    <div>
      <div style={{ display:'flex', flexDirection:'column', gap:10 }}>
        <div style={{ display:'flex', gap:8, flexWrap:'wrap' }}>
          <input placeholder="Search title" value={local.qTitle} onChange={e=>setLocal(s=>({...s, qTitle: e.target.value}))} style={{ flex:'1 1 200px', padding:10, borderRadius:8, border:'1px solid #e4e4ee' }} />
          <input placeholder="Search subtitle" value={local.qSubtitle} onChange={e=>setLocal(s=>({...s, qSubtitle: e.target.value}))} style={{ flex:'1 1 160px', padding:10, borderRadius:8, border:'1px solid #e4e4ee' }} />
          <input placeholder="Min amount" type="number" value={local.minAmount} onChange={e=>setLocal(s=>({...s, minAmount: e.target.value}))} style={{ width:120, padding:10, borderRadius:8, border:'1px solid #e4e4ee' }} />
          <input placeholder="Max amount" type="number" value={local.maxAmount} onChange={e=>setLocal(s=>({...s, maxAmount: e.target.value}))} style={{ width:120, padding:10, borderRadius:8, border:'1px solid #e4e4ee' }} />
          <select value={local.onlyGroup} onChange={e=>setLocal(s=>({...s, onlyGroup: e.target.value}))} style={{ padding:8, borderRadius:8 }}>
            <option value="any">All</option>
            <option value="group">Group only</option>
            <option value="independent">Independent</option>
          </select>
          <button className="btn" onClick={onApply}>Apply</button>
          <button className="btn" style={{ background:'transparent', color:'var(--purple-1)', border:'1px solid rgba(0,0,0,0.08)' }} onClick={onClear}>Clear</button>
        </div>

        <div style={{ display:'flex', gap:8, flexWrap:'wrap' }}>
          <div style={{ display:'flex', gap:8, alignItems:'center' }}>
            <div style={{ fontSize:13, color:'var(--muted-dark)' }}>Date added:</div>
            <input type="date" value={local.dateAddedFrom} onChange={e=>setLocal(s=>({...s, dateAddedFrom: e.target.value}))} />
            <div style={{ fontSize:13, color:'var(--muted-dark)' }}>—</div>
            <input type="date" value={local.dateAddedTo} onChange={e=>setLocal(s=>({...s, dateAddedTo: e.target.value}))} />
          </div>

          <div style={{ display:'flex', gap:8, alignItems:'center' }}>
            <div style={{ fontSize:13, color:'var(--muted-dark)' }}>Transaction date:</div>
            <input type="date" value={local.dateTxFrom} onChange={e=>setLocal(s=>({...s, dateTxFrom: e.target.value}))} />
            <div style={{ fontSize:13, color:'var(--muted-dark)' }}>—</div>
            <input type="date" value={local.dateTxTo} onChange={e=>setLocal(s=>({...s, dateTxTo: e.target.value}))} />
          </div>
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
                <input 
                  placeholder="Category" 
                  value={editForm.subtitle} 
                  onChange={e => setEditForm(prev => ({ ...prev, subtitle: e.target.value }))}
                  style={{ padding:8, borderRadius:6, border:'1px solid #e4e4ee' }}
                />
                <input 
                  placeholder="Amount" 
                  type="number"
                  value={editForm.amount} 
                  onChange={e => setEditForm(prev => ({ ...prev, amount: e.target.value }))}
                  style={{ padding:8, borderRadius:6, border:'1px solid #e4e4ee' }}
                />
                <div style={{ display:'flex', gap:8 }}>
                  <button className="bp-add-btn" onClick={saveEdit}>Save</button>
                  <button className="btn" onClick={cancelEdit}>Cancel</button>
                </div>
              </div>
          ) : (
            //VIEW MODE
            <>
              <div style={{ display:'flex', alignItems:'center', gap:12 }}>
                <div style={{ width:56, height:56, borderRadius:10, display:'flex', alignItems:'center', justifyContent:'center', background:'#fff', color:'var(--text-dark)', fontWeight:800, boxShadow:'0 4px 14px rgba(0,0,0,0.04)' }}>
                  {it.initial}
                </div>

                <div style={{ flex:1 }}>
                  <div style={{ fontWeight:800, color:'var(--text-dark)' }}>{it.title}</div>
                  <div style={{ color:'var(--muted-dark)', fontSize:13 }}>{it.subtitle}</div>
                </div>

                <div style={{ textAlign:'right' }}>
                  <div style={{ fontWeight:800, color: it.amount < 0 ? '#ff6b6b' : '#34d399', fontSize:16 }}>{it.amount < 0 ? '-' : '+'}${Math.abs(it.amount)}</div>
                  <div style={{ fontSize:12, color:'var(--muted-dark)' }}>{it.dateTransaction}</div>
                </div>
              </div>

              <div style={{ marginTop: 10, display:'flex', justifyContent:'space-between', alignItems:'center', color:'var(--muted-dark)', fontSize:12 }}>
                <div>Added: {it.dateAdded}</div>
                <div style={{ display:'flex', gap:8, alignItems:'center' }}>
                  <div>{it.isGroup ? `Group: ${it.groupName}` : 'Independent'}</div>
                  <button className="btn" onClick={() => startEdit(it)} style={{ padding:'6px 8px' }}>Edit</button>
                  <button className="btn" onClick={() => handleDelete(it.id)} style={{ padding:'6px 8px' }}>Delete</button>
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

