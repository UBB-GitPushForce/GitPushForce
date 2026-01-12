// src/components/Data.tsx
import React, { useEffect, useMemo, useState } from 'react';
import '../App.css';
import { useAuth } from '../hooks/useAuth';
import apiClient from '../services/api-client';
import categoryService, { Category } from '../services/category-service';
import {
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  Legend,
} from 'recharts';

type Expense = {
  id: number;
  title: string;
  category_id: number; // API returns ID, not string name
  amount: number;
  created_at?: string;
  date?: string;
};

// Data will be loaded from the backend `/expenses` endpoints.

const COLORS = ['#7c3aed', '#6b29d9', '#059669', '#dc2626', '#f59e0b', '#0ea5e9', '#ef4444'];

function dateKey(e: Expense) {
  const d = e.created_at ?? e.date ?? new Date().toISOString();
  return d.slice(0, 10);
}

function sumBy<T>(arr: T[], keyFn: (t: T) => string, valueFn: (t: T) => number) {
  const map = new Map<string, number>();
  for (const it of arr) {
    const k = keyFn(it);
    map.set(k, (map.get(k) || 0) + valueFn(it));
  }
  return Array.from(map.entries()).map(([k, v]) => ({ key: k, value: v }));
}

const Data: React.FC = () => {
  const { user } = useAuth();
  const [expenses, setExpenses] = useState<Expense[]>([]);
  const [loading, setLoading] = useState(false);
  const [categoryFilterId, setCategoryFilterId] = useState<number | 'all'>('all');
  const [categories, setCategories] = useState<Category[]>([]);
  const [dateFrom, setDateFrom] = useState<string>('');
  const [dateTo, setDateTo] = useState<string>('');

  // Load available categories once (from user's expenses). We limit to a reasonable number.
  useEffect(() => {
    const fetchCats = async () => {
      try {
        const data = await categoryService.getCategories();
        setCategories(data);
      } catch (err) {
        console.error('Failed to load categories', err);
      }
    };
    if (user) fetchCats();
  }, [user]);

  const categoryNameMap = useMemo(() => {
    const map = new Map<number, string>();
    categories.forEach(c => map.set(c.id, c.title));
    return map;
  }, [categories]);

  // Load expenses whenever filters change — server-side filtering keeps data consistent and scales.
  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      setLoading(true);
      try {
        // We fetch a large batch to calculate accurate trends on the frontend
        const params: any = { offset: 0, limit: 1000 };
        
        // Pass date filters to backend to optimize query
        if (dateFrom) params.date_from = dateFrom;
        if (dateTo) params.date_to = dateTo;
        
        // Note: We handle category filtering on frontend for chart consistency, 
        // or you can pass `category` (name) to backend if your API supports it.
        // For now, we fetch all for the date range and filter locally.

        const res = await apiClient.get('/expenses', { params });
        const items: Expense[] = Array.isArray(res.data) ? res.data : (res.data.data || []);
        
        if (cancelled) return;
        setExpenses(items);
      } catch (err) {
        console.error('Failed to fetch expenses', err);
        setExpenses([]);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    if (user) load();
    return () => { cancelled = true; };
  }, [user, dateFrom, dateTo]); // Reload when dates change

  // Filtering
  const filtered = useMemo(() => {
    return expenses.filter((e) => {
      // Date filtering is handled by backend, but double check here if needed
      // Category filtering:
      if (categoryFilterId !== 'all' && e.category_id !== categoryFilterId) return false;
      return true;
    });
  }, [expenses, categoryFilterId]);

  // Line chart: totals per day (sum amounts)
  const lineData = useMemo(() => {
    const grouped = sumBy(filtered, dateKey, (e) => Math.abs(e.amount)); 
    // sort by date ascending
    grouped.sort((a, b) => (a.key < b.key ? -1 : 1));
    return grouped.map((g) => ({ 
        date: g.key, 
        total: Math.round((g.value + Number.EPSILON) * 100) / 100 
    }));
  }, [filtered]);

  // Bar chart: daily absolute spending (expenses only)
  const barData = useMemo(() => {
    const grouped = sumBy(filtered, dateKey, (e) => Math.abs(e.amount));
    grouped.sort((a, b) => (a.key < b.key ? -1 : 1));
    return grouped.map((g) => ({ 
        date: g.key, 
        spent: Math.round((g.value + Number.EPSILON) * 100) / 100 
    }));
  }, [filtered]);

  // Pie chart: by category totals (absolute)
  const pieData = useMemo(() => {
    // Only count expenses (negative amounts)
    const expensesOnly = filtered.filter((e) => e.amount < 0);
    
    // Group by Category Name (looked up from ID)
    const grouped = sumBy(
        filtered, 
        (e) => categoryNameMap.get(e.category_id) || 'Uncategorized', 
        (e) => Math.abs(e.amount)
    );
    
    grouped.sort((a, b) => b.value - a.value);
    
    return grouped.map((g, i) => ({ 
        name: g.key, 
        value: Math.round((g.value + Number.EPSILON) * 100) / 100, 
        color: COLORS[i % COLORS.length] 
    }));
  }, [filtered, categoryNameMap]);

  return (
    <div style={{ marginTop: 12 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div className="bp-title">Data & Analytics</div>
        <div style={{ color: 'var(--muted-dark)' }}>Visualize expense trends</div>
      </div>

      <div style={{ marginTop: 12, display: 'grid', gap: 12 }}>
        {/* Controls */}
        <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', alignItems: 'center' }}>
          <label style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
            Category:
            <select 
                value={categoryFilterId} 
                onChange={(e) => setCategoryFilterId(e.target.value === 'all' ? 'all' : Number(e.target.value))}
                style={{ padding: 8, borderRadius: 6, border: '1px solid #ccc' }}
            >
              <option value="all">All</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>{c.title}</option>
              ))}
            </select>
          </label>

          <label style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
            From:
            <input type="date" value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} style={{ padding: 6, borderRadius: 6, border: '1px solid #ccc' }} />
          </label>

          <label style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
            To:
            <input type="date" value={dateTo} onChange={(e) => setDateTo(e.target.value)} style={{ padding: 6, borderRadius: 6, border: '1px solid #ccc' }} />
          </label>

          <div style={{ marginLeft: 'auto', display: 'flex', gap: 8 }}>
            <button
              className="bp-add-btn"
              onClick={() => {
                // reset filters
                setCategoryFilterId('all');
                setDateFrom('');
                setDateTo('');
              }}
            >
              Reset filters
            </button>
          </div>
        </div>

        {/* Charts grid */}
        <div
          style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
            gap: 12,
            alignItems: 'start',
          }}
        >
          {/* Line chart: balance over time */}
          <div style={{ padding: 12, borderRadius: 10, background: 'var(--card-light)', border: '1px solid #e4e4ee' }}>
            <div style={{ fontWeight: 800, marginBottom: 8 }}>Balance over time</div>
            <div style={{ width: '100%', height: 240 }}>
              <ResponsiveContainer>
                <LineChart data={lineData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis />
                  <Tooltip />
                  <Line type="monotone" dataKey="total" stroke="#7c3aed" strokeWidth={2} dot={{ r: 3 }} />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Bar chart: spending per day */}
          <div style={{ padding: 12, borderRadius: 10, background: 'var(--card-light)', border: '1px solid #e4e4ee' }}>
            <div style={{ fontWeight: 800, marginBottom: 8 }}>Daily spending (absolute)</div>
            <div style={{ width: '100%', height: 240 }}>
              <ResponsiveContainer>
                <BarChart data={barData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis />
                  <Tooltip />
                  <Bar dataKey="spent" fill="#ef4444" />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Pie chart: by category */}
          <div style={{ padding: 12, borderRadius: 10, background: 'var(--card-light)', border: '1px solid #e4e4ee' }}>
            <div style={{ fontWeight: 800, marginBottom: 8 }}>Spending by category</div>
            <div style={{ width: '100%', height: 240, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <ResponsiveContainer>
                <PieChart>
                  <Pie data={pieData} dataKey="value" nameKey="name" innerRadius={50} outerRadius={80} paddingAngle={4}>
                    {pieData.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>

        {loading && <div style={{ color: 'var(--muted-dark)' }}>Loading data...</div>}
        {!loading && filtered.length === 0 && <div style={{ color: 'var(--muted-dark)' }}>No data for selected filters.</div>}
      </div>
    </div>
  );
};

export default Data;

