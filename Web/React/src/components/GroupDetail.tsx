// src/components/GroupDetail.tsx
import React, { useEffect, useState } from 'react';
import apiClient from '../services/api-client';
import { useAuth } from '../hooks/useAuth';
import '../App.css';
import { useCurrency } from '../contexts/CurrencyContext';
import categoryService, { Category } from '../services/category-service';

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
    const { user } = useAuth();
    const [groupName, setGroupName] = useState<string | null>(null);
    const [invitationCode, setInvitationCode] = useState<string | null>(null);
    const [showInvitation, setShowInvitation] = useState(false);
    const [expenses, setExpenses] = useState<Expense[]>([]);
    const [loading, setLoading] = useState(false);
    const [categories, setCategories] = useState<Category[]>([]);
    const [showAddExpense, setShowAddExpense] = useState(false);
    const [newExpense, setNewExpense] = useState({
        title: '',
        amount: '',
        categoryId: null as number | null,
    });

    const fetchGroupInfo = async () => {
        if (!groupId) return;
        
        try {
            const res = await apiClient.get(`/groups/${groupId}`);
            // Backend returns APIResponse { success: true, data: GroupResponse }
            const groupData = res.data?.data || res.data;
            setGroupName(groupData.name || `Group ${groupId}`);
            setInvitationCode(groupData.invitation_code || null);
        } catch (err) {
            console.error('Failed to fetch group info', err);
            setGroupName(`Group ${groupId}`);
        }
    };

    const fetchGroupExpenses = async () => {
        if (!groupId) return;
        
        setLoading(true);
        try {
            const res = await apiClient.get(`/expenses/group/${groupId}`);
            
            // Backend returns APIResponse { success: true, data: [expenses] }
            const responseData = res.data;
            const items = Array.isArray(responseData) ? responseData : (responseData?.data || []);
            
            // Fetch categories
            const categories = await categoryService.getCategories();
            const categoryMap = new Map(categories.map(c => [c.id, c.title]));
            
            // Get unique user IDs
            const uniqueUserIds = [...new Set(items.map((exp: any) => exp.user_id))];
            
            // Fetch user details for each unique user ID
            const userDetailsMap: Record<number, { first_name: string; last_name: string }> = {};
            await Promise.all(
                uniqueUserIds.map(async (userId: any) => {
                    try {
                        const userRes = await apiClient.get(`/users/${userId}`);
                        // Backend returns APIResponse { success: true, data: UserResponse }
                        const userData = userRes.data?.data || userRes.data;
                        userDetailsMap[userId] = {
                            first_name: userData.first_name || 'User',
                            last_name: userData.last_name || '',
                        };
                    } catch (err) {
                        console.error(`Failed to fetch user ${userId}`, err);
                        userDetailsMap[userId] = { first_name: `User ${userId}`, last_name: '' };
                    }
                })
            );
            
            const mapped: Expense[] = items.map((exp: any) => {
                const userDetails = userDetailsMap[exp.user_id] || { first_name: 'Unknown', last_name: '' };
                const fullName = `${userDetails.first_name}`.trim();
                
                return {
                    id: exp.id,
                    title: exp.title || 'Untitled',
                    category: categoryMap.get(exp.category_id) || 'Uncategorized',
                    amount: exp.amount || 0,
                    userName: fullName,
                    userInitial: (userDetails.first_name[0] || 'U').toUpperCase(),
                    date: exp.created_at ? new Date(exp.created_at).toISOString().slice(0, 10) : new Date().toISOString().slice(0, 10),
                };
            });
            
            setExpenses(mapped);
        } catch (err) {
            console.error('Failed to fetch group expenses', err);
            setExpenses([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (!groupId) return;
        fetchGroupInfo();
        fetchGroupExpenses();
        fetchCategories();
    }, [groupId]);

    const fetchCategories = async () => {
        try {
            const cats = await categoryService.getCategories();
            
            // If no categories exist, create default ones
            if (cats.length === 0) {
                const defaultCategories = ['Food', 'Transport', 'Entertainment', 'Shopping', 'Bills', 'Other'];
                for (const title of defaultCategories) {
                    try {
                        await categoryService.createCategory(title);
                    } catch (err) {
                        console.error(`Failed to create category ${title}`, err);
                    }
                }
                // Refetch
                const newCats = await categoryService.getCategories();
                setCategories(newCats);
                if (newCats.length > 0) {
                    setNewExpense(prev => ({ ...prev, categoryId: newCats[0].id }));
                }
            } else {
                setCategories(cats);
                if (cats.length > 0) {
                    setNewExpense(prev => ({ ...prev, categoryId: cats[0].id }));
                }
            }
        } catch (err) {
            console.error('Failed to fetch categories', err);
        }
    };

    const cur = useCurrency();

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

                <div className="bp-title">{groupName ?? 'Group'}</div>
            </div>

            <div className="bp-section-title" style={{ marginTop: 12, fontSize: 14 }}>Expenses for this group</div>

            <div style={{ marginTop: 12, display: 'flex', flexDirection: 'column', gap: 12 }}>
                {expenses.map(e => (
                    <article key={e.id} className="bp-tx">
                        <div className="bp-thumb" style={{ width:48, height:48, borderRadius:10, fontSize:16 }}>{e.userInitial}</div>

                        <div className="bp-meta">
                            <div className="bp-tx-title">{e.title}</div>
                            <div className="bp-tx-cat">{e.category} • {e.userName}</div>
                            <div style={{ marginTop: 8, fontSize: 12, color: 'var(--muted-dark)' }}>{e.date}</div>
                        </div>

                        <div style={{ marginLeft: 12, fontWeight: 800 }} className={`bp-amount ${e.amount < 0 ? 'negative' : 'positive'}`}>
                            {e.amount < 0 ? '-' : '+'}{cur.formatAmount(Math.abs(e.amount))}
                        </div>
                    </article>
                ))}
            </div>

            {/* footer action — Add expense */}
            <div style={{ marginTop: 18, display: 'flex', gap: 8 }}>
                <button
                    className="bp-add-btn"
                    onClick={() => setShowAddExpense(!showAddExpense)}
                    disabled={loading}
                >
                    {showAddExpense ? 'Cancel' : 'Add Expense'}
                </button>

                <button
                    className="bp-add-btn"
                    onClick={() => setShowInvitation(!showInvitation)}
                >
                    {showInvitation ? 'Hide Invitation Code' : 'Show Invitation Code'}
                </button>
            </div>

            {/* Add Expense Form */}
            {showAddExpense && (
                <div style={{ 
                    marginTop: 20, 
                    padding: 20, 
                    background: 'var(--card-bg)', 
                    borderRadius: 12, 
                    border: '1px solid rgba(0,0,0,0.08)' 
                }}>
                    <div style={{ fontWeight: 700, fontSize: 16, marginBottom: 15 }}>Add Expense</div>
                    
                    <form onSubmit={async (e) => {
                        e.preventDefault();
                        
                        if (!user?.id || !groupId) {
                            alert('User or group not available');
                            return;
                        }

                        if (!newExpense.title.trim() || !newExpense.amount || !newExpense.categoryId) {
                            alert('Please fill in all fields');
                            return;
                        }

                        const amount = parseFloat(newExpense.amount);
                        if (amount <= 0) {
                            alert('Amount must be positive');
                            return;
                        }

                        try {
                            const body = {
                                title: newExpense.title.trim(),
                                amount,
                                category_id: newExpense.categoryId,
                                group_id: groupId,
                            };

                            await apiClient.post('/expenses', body);
                            
                            // Refresh the expense list
                            await fetchGroupExpenses();
                            
                            // Reset form
                            setNewExpense({
                                title: '',
                                amount: '',
                                categoryId: categories.length > 0 ? categories[0].id : null,
                            });
                            setShowAddExpense(false);
                            alert('Expense added successfully!');
                        } catch (err: any) {
                            console.error('Failed to create expense', err);
                            alert(`Failed to add expense: ${err.response?.data?.detail || err.message}`);
                        }
                    }} style={{ display: 'grid', gap: 12 }}>
                        
                        <div>
                            <label style={{ display: 'block', marginBottom: 6, fontSize: 14, fontWeight: 600 }}>
                                Title
                            </label>
                            <input
                                type="text"
                                value={newExpense.title}
                                onChange={(e) => setNewExpense(prev => ({ ...prev, title: e.target.value }))}
                                placeholder="e.g. Lunch at restaurant"
                                style={{
                                    width: '100%',
                                    padding: '10px 12px',
                                    borderRadius: 8,
                                    border: '1px solid rgba(0,0,0,0.1)',
                                    fontSize: 14,
                                }}
                                required
                            />
                        </div>

                        <div>
                            <label style={{ display: 'block', marginBottom: 6, fontSize: 14, fontWeight: 600 }}>
                                Amount
                            </label>
                            <input
                                type="number"
                                step="0.01"
                                min="0.01"
                                value={newExpense.amount}
                                onChange={(e) => setNewExpense(prev => ({ ...prev, amount: e.target.value }))}
                                placeholder="0.00"
                                style={{
                                    width: '100%',
                                    padding: '10px 12px',
                                    borderRadius: 8,
                                    border: '1px solid rgba(0,0,0,0.1)',
                                    fontSize: 14,
                                }}
                                required
                            />
                        </div>

                        <div>
                            <label style={{ display: 'block', marginBottom: 6, fontSize: 14, fontWeight: 600 }}>
                                Category
                            </label>
                            <select
                                value={newExpense.categoryId || ''}
                                onChange={(e) => setNewExpense(prev => ({ ...prev, categoryId: parseInt(e.target.value) }))}
                                style={{
                                    width: '100%',
                                    padding: '10px 12px',
                                    borderRadius: 8,
                                    border: '1px solid rgba(0,0,0,0.1)',
                                    fontSize: 14,
                                    background: '#fff',
                                }}
                                required
                            >
                                {categories.map(cat => (
                                    <option key={cat.id} value={cat.id}>
                                        {cat.title}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <button
                            type="submit"
                            className="bp-add-btn"
                            style={{ marginTop: 8 }}
                        >
                            Submit Expense
                        </button>
                    </form>
                </div>
            )}

            {/* Invitation Code Display */}
            {showInvitation && groupId && invitationCode && (
                <div style={{ 
                    marginTop: 20, 
                    padding: 20, 
                    background: 'var(--card-bg)', 
                    borderRadius: 12, 
                    border: '1px solid rgba(0,0,0,0.08)',
                    textAlign: 'center'
                }}>
                    <div style={{ fontWeight: 700, fontSize: 16, marginBottom: 10 }}>Group Invitation</div>
                    
                    <div style={{ 
                        fontSize: 24, 
                        fontWeight: 800, 
                        color: 'var(--purple-1)', 
                        marginBottom: 15,
                        letterSpacing: 2
                    }}>
                        {invitationCode}
                    </div>
                    
                    <div style={{ marginTop: 10, color: 'var(--muted-dark)', fontSize: 13 }}>
                        Share this code to invite members
                    </div>
                </div>
            )}
        </div>
    );
};

export default GroupDetail;

