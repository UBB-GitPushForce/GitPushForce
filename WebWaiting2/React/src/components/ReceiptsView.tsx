// src/components/ReceiptsView.tsx
import React, { useCallback, useEffect, useRef, useState } from 'react';
import '../App.css';
import type { ReceiptItem } from './Receipts';

/*
  ReceiptsView: infinite scroll grid with filters and delete action (mock).
  - injectedReceipts: items injected by upload/manual/camera; these are shown on top.
  - TODO: replace mock fetchPage & delete with server API calls.
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

function makeMockData(total = 500) {
  const subtitles = ['Utilities', 'Food', 'Income', 'Transport', 'Shopping', 'Coffee', 'Rent', 'Other'];
  const titles = ['Electricity Bill', 'Grocery', 'Salary', 'Subway', 'New Shoes', 'Coffee', 'Rent', 'Invoice #1234'];
  const names = ['Alice', 'Bob', 'Charlie', 'Diana', 'Eve', 'You'];
  const items: ReceiptItem[] = [];
  for (let i = 0; i < total; i++) {
    const amount = Math.round((Math.random() * 500 + 1) * (Math.random() > 0.8 ? 1 : -1));
    const dateTx = randomDate(new Date(2024, 0, 1), new Date(2025, 10, 28));
    const dateAdded = randomDate(new Date(dateTx), new Date(2025, 10, 28));
    const isGroup = Math.random() > 0.7;
    const group = isGroup ? mockGroups[Math.floor(Math.random() * mockGroups.length)] : undefined;
    const title = titles[Math.floor(Math.random() * titles.length)];
    const subtitle = subtitles[Math.floor(Math.random() * subtitles.length)];
    const user = names[Math.floor(Math.random() * names.length)];
    items.push({
      id: total - i,
      title,
      subtitle,
      amount,
      dateTransaction: dateTx,
      dateAdded,
      isGroup,
      groupId: group?.id,
      groupName: group?.name,
      addedBy: user,
      initial: (user[0] || 'U').toUpperCase(),
    });
  }
  items.sort((a,b) => {
    if (a.dateAdded === b.dateAdded) return b.id - a.id;
    return b.dateAdded.localeCompare(a.dateAdded);
  });
  return items;
}

const MOCK_DB = makeMockData(600);

// mock paged fetch
async function fetchPage(pageIndex: number, pageSize: number, filters: any) {
  await new Promise(r => setTimeout(r, 220));
  let list = MOCK_DB.slice();

  if (filters.qTitle) {
    const q = filters.qTitle.toLowerCase();
    list = list.filter(i => i.title.toLowerCase().includes(q));
  }
  if (filters.qSubtitle) {
    const q = filters.qSubtitle.toLowerCase();
    list = list.filter(i => i.subtitle.toLowerCase().includes(q));
  }
  if (filters.minAmount != null) list = list.filter(i => i.amount >= filters.minAmount);
  if (filters.maxAmount != null) list = list.filter(i => i.amount <= filters.maxAmount);
  if (filters.dateAddedFrom) list = list.filter(i => i.dateAdded >= filters.dateAddedFrom);
  if (filters.dateAddedTo) list = list.filter(i => i.dateAdded <= filters.dateAddedTo);
  if (filters.dateTxFrom) list = list.filter(i => i.dateTransaction >= filters.dateTxFrom);
  if (filters.dateTxTo) list = list.filter(i => i.dateTransaction <= filters.dateTxTo);
  if (filters.onlyGroup === 'group') list = list.filter(i => !!i.isGroup);
  if (filters.onlyGroup === 'independent') list = list.filter(i => !i.isGroup);

  const start = pageIndex * pageSize;
  const end = start + pageSize;
  const pageItems = list.slice(start, end);
  const hasMore = end < list.length;
  return { items: pageItems, hasMore };
}

// mock delete API
async function deleteMock(id: number) {
  // TODO: call DELETE /receipts/{id}
  await new Promise(r => setTimeout(r, 300));
  return true;
}

const ReceiptsView: React.FC<{ injectedReceipts?: ReceiptItem[] }> = ({ injectedReceipts = [] }) => {
  const [items, setItems] = useState<ReceiptItem[]>([]);
  const [pageIndex, setPageIndex] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);

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
      setItems([...injectedReceipts, ...res.items]);
      setHasMore(res.hasMore);
      setPageIndex(1);
    } finally {
      setLoading(false);
    }
  }, [filters, injectedReceipts]);

  useEffect(() => { loadFirst(); }, [loadFirst]);

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
      // TODO: call API to delete the receipt permanently
      await deleteMock(id);
      setItems(prev => prev.filter(x => x.id !== id));
      alert('Deleted (mock). Replace with API call.');
    } catch (err) {
      console.error(err);
      alert('Delete failed (mock).');
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
                  <button className="btn" onClick={() => handleDelete(it.id)} style={{ padding:'6px 8px' }}>Delete</button>
                </div>
              </div>
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

