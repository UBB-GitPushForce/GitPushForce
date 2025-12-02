// src/components/Data.tsx
import React, { useEffect, useMemo, useState } from 'react';
import '../App.css';
import { useAuth } from '../hooks/useAuth';
import apiClient from '../services/api-client';
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
  category: string;
  amount: number; // negative for expense, positive for income
  created_at?: string; // ISO date
  date?: string; // fallback
  user_id?: number;
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
  const [categoryFilter, setCategoryFilter] = useState<string>('any');
  const [categoriesList, setCategoriesList] = useState<string[]>([]);
  const [dateFrom, setDateFrom] = useState<string>('');
  const [dateTo, setDateTo] = useState<string>('');

  // Load available categories once (from user's expenses). We limit to a reasonable number.
  useEffect(() => {
    let cancelled = false;
    const loadCategories = async () => {
      try {
        const res = await apiClient.get('/expenses', { params: { offset: 0, limit: 1000 } });
        const items: Expense[] = Array.isArray(res.data) ? res.data : [];
        if (cancelled) return;
        const setCats = new Set<string>();
        items.forEach((it) => setCats.add(it.category || 'Uncategorized'));
        setCategoriesList(Array.from(setCats));
      } catch (err) {
        console.error('Failed to load categories for Data page', err);
        setCategoriesList([]);
      }
    };

    if (user) loadCategories();
    return () => {
      cancelled = true;
    };
  }, [user]);

  // Load expenses whenever filters change — server-side filtering keeps data consistent and scales.
  useEffect(() => {
    let cancelled = false;
    const load = async () => {
      setLoading(true);
      try {
        const params: any = { offset: 0, limit: 1000 };
        if (categoryFilter && categoryFilter !== 'any') params.category = categoryFilter;
        if (dateFrom) params.date_from = dateFrom;
        if (dateTo) params.date_to = dateTo;
        const res = await apiClient.get('/expenses', { params });
        const items: Expense[] = Array.isArray(res.data) ? res.data : [];
        if (cancelled) return;
        setExpenses(items);
      } catch (err) {
        console.error('Failed to fetch expenses for Data page', err);
        setExpenses([]);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    if (user) load();
    else setExpenses([]);

    return () => {
      cancelled = true;
    };
  }, [user, categoryFilter, dateFrom, dateTo]);

  // Filtering
  const filtered = useMemo(() => {
    return expenses.filter((e) => {
      const d = dateKey(e);
      if (dateFrom && d < dateFrom) return false;
      if (dateTo && d > dateTo) return false;
      if (categoryFilter !== 'any' && e.category !== categoryFilter) return false;
      return true;
    });
  }, [expenses, categoryFilter, dateFrom, dateTo]);

  // Line chart: totals per day (sum amounts)
  const lineData = useMemo(() => {
    const grouped = sumBy(filtered, dateKey, (e) => e.amount);
    // sort by date ascending
    grouped.sort((a, b) => (a.key < b.key ? -1 : 1));
    return grouped.map((g) => ({ date: g.key, total: Math.round((g.value + Number.EPSILON) * 100) / 100 }));
  }, [filtered]);

  // Bar chart: daily absolute spending (expenses only)
  const barData = useMemo(() => {
    // take only negative amounts (expenses) and sum absolute
    const expensesOnly = filtered.filter((e) => e.amount < 0);
    const grouped = sumBy(expensesOnly, dateKey, (e) => Math.abs(e.amount));
    grouped.sort((a, b) => (a.key < b.key ? -1 : 1));
    return grouped.map((g) => ({ date: g.key, spent: Math.round((g.value + Number.EPSILON) * 100) / 100 }));
  }, [filtered]);

  // Pie chart: by category totals (absolute)
  const pieData = useMemo(() => {
    const grouped = sumBy(filtered, (e) => e.category || 'Uncategorized', (e) => Math.abs(e.amount));
    grouped.sort((a, b) => b.value - a.value); // descending
    return grouped.map((g, i) => ({ name: g.key, value: Math.round((g.value + Number.EPSILON) * 100) / 100, color: COLORS[i % COLORS.length] }));
  }, [filtered]);

  const categories = useMemo(() => ['any', ...categoriesList], [categoriesList]);

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
            <select value={categoryFilter} onChange={(e) => setCategoryFilter(e.target.value)}>
              {categories.map((c) => (
                <option key={c} value={c}>
                  {c === 'any' ? 'All' : c}
                </option>
              ))}
            </select>
          </label>

          <label style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
            From:
            <input type="date" value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} />
          </label>

          <label style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
            To:
            <input type="date" value={dateTo} onChange={(e) => setDateTo(e.target.value)} />
          </label>

          <div style={{ marginLeft: 'auto', display: 'flex', gap: 8 }}>
            <button
              className="bp-add-btn"
              onClick={() => {
                // reset filters
                setCategoryFilter('any');
                setDateFrom('');
                setDateTo('');
              }}
            >
              Reset filters
            </button>
            <button
              className="bp-add-btn"
              onClick={() => {
                // TODO: export current filtered data to CSV/PDF
                const csv = filtered
                  .map((r) => `${r.id},"${r.title}","${r.category}",${r.amount},${dateKey(r)}`)
                  .join('\n');
                const blob = new Blob([`id,title,category,amount,date\n${csv}`], { type: 'text/csv' });
                const url = URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = 'expenses_export.csv';
                a.click();
                URL.revokeObjectURL(url);
              }}
            >
              Export CSV
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

