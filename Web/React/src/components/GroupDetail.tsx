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
    userId: number;
}

interface GroupLog {
    id: number;
    user_id: number;
    action: string;
    created_at: string;
}

interface TimelineItem {
    type: 'expense' | 'log';
    data: Expense | GroupLog;
    timestamp: Date;
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
    const [timeline, setTimeline] = useState<TimelineItem[]>([]);
    const [memberCount, setMemberCount] = useState<number>(0);
    const [loading, setLoading] = useState(false);
    const [categories, setCategories] = useState<Category[]>([]);
    const [showAddExpense, setShowAddExpense] = useState(false);
    const [newExpense, setNewExpense] = useState({
        title: '',
        amount: '',
        categoryId: null as number | null,
    });
    const [splitModalExpenseId, setSplitModalExpenseId] = useState<number | null>(null);
    const [groupUsers, setGroupUsers] = useState<Array<{ id: number; first_name: string; paid: boolean }>>([]);
    const [loadingSplit, setLoadingSplit] = useState(false);
    const [statistics, setStatistics] = useState<{
        my_share_of_expenses: number;
        my_total_paid: number;
        net_balance_paid_for_others: number;
        rest_of_group_expenses: number;
    } | null>(null);
    const [showStatistics, setShowStatistics] = useState(false);

    const fetchGroupInfo = async () => {
        if (!groupId) return;
        
        try {
            const res = await apiClient.get(`/groups/${groupId}`);
            // Backend returns APIResponse { success: true, data: GroupResponse }
            const groupData = res.data?.data || res.data;
            setGroupName(groupData.name || `Group ${groupId}`);
            setInvitationCode(groupData.invitation_code || null);
            
            // Fetch member count
            const membersRes = await apiClient.get(`/groups/${groupId}/users/nr`);
            const memberData = membersRes.data?.data || membersRes.data;
            setMemberCount(memberData || 0);
            
            // Fetch statistics
            const statsRes = await apiClient.get(`/groups/${groupId}/statistics/user-summary`);
            const statsData = statsRes.data?.data || statsRes.data;
            setStatistics(statsData);
        } catch (err) {
            console.error('Failed to fetch group info', err);
            setGroupName(`Group ${groupId}`);
        }
    };

    const fetchGroupExpenses = async () => {
        if (!groupId) return;
        
        setLoading(true);
        try {
            // Fetch expenses
            const res = await apiClient.get(`/expenses/group/${groupId}`);
            
            // Backend returns APIResponse { success: true, data: [expenses] }
            const responseData = res.data;
            const items = Array.isArray(responseData) ? responseData : (responseData?.data || []);
            
            // Fetch group logs
            const logsRes = await apiClient.get(`/group_logs/${groupId}`);
            const logsData = logsRes.data?.data || logsRes.data || [];
            
            // Fetch categories - fetch all categories since backend returns all anyway (known bug)
            // This ensures we can map categories from all users in the group
            const categories = await categoryService.getCategories(user?.id);
            const categoryMap = new Map(categories.map(c => [c.id, c.title]));
            
            // Get unique user IDs from both expenses and logs
            const expenseUserIds = items.map((exp: any) => exp.user_id);
            const logUserIds = logsData.map((log: any) => log.user_id);
            const uniqueUserIds = [...new Set([...expenseUserIds, ...logUserIds])];
            
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
                    userId: exp.user_id,
                };
            });
            
            setExpenses(mapped);
            
            // Create timeline combining expenses and logs
            const timelineItems: TimelineItem[] = [
                ...mapped.map(exp => ({
                    type: 'expense' as const,
                    data: exp,
                    timestamp: new Date(exp.date),
                })),
                ...logsData.map((log: any) => ({
                    type: 'log' as const,
                    data: {
                        id: log.id,
                        user_id: log.user_id,
                        action: log.action,
                        created_at: log.created_at,
                        userName: userDetailsMap[log.user_id]?.first_name || 'User',
                    },
                    timestamp: new Date(log.created_at),
                })),
            ];
            
            // Sort by timestamp
            timelineItems.sort((a, b) => a.timestamp.getTime() - b.timestamp.getTime());
            
            setTimeline(timelineItems);
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
            const cats = await categoryService.getCategories(user?.id);
            
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
                const newCats = await categoryService.getCategories(user?.id);
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

    const openSplitModal = async (expenseId: number) => {
        if (!groupId) return;
        
        setSplitModalExpenseId(expenseId);
        setLoadingSplit(true);
        
        try {
            // Fetch all users in the group
            const usersRes = await apiClient.get(`/groups/${groupId}/users`);
            const usersData = usersRes.data?.data || usersRes.data || [];
            
            // Fetch payment status for this expense
            const paymentsRes = await apiClient.get(`/expenses_payments/${expenseId}/payments`);
            const paymentsData = paymentsRes.data?.data || paymentsRes.data || [];
            const paidUserIds = new Set(paymentsData.map((p: any) => p.user_id));
            
            // Combine user list with payment status
            const usersWithPaymentStatus = usersData.map((u: any) => ({
                id: u.id,
                first_name: u.first_name || 'User',
                paid: paidUserIds.has(u.id),
            }));
            
            setGroupUsers(usersWithPaymentStatus);
        } catch (err) {
            console.error('Failed to fetch split data', err);
            alert('Failed to load split information');
        } finally {
            setLoadingSplit(false);
        }
    };

    const togglePaymentStatus = async (expenseId: number, userId: number, currentlyPaid: boolean) => {
        try {
            if (currentlyPaid) {
                // Unmark as paid
                await apiClient.delete(`/expenses_payments/${expenseId}/pay/${userId}`);
            } else {
                // Mark as paid
                await apiClient.post(`/expenses_payments/${expenseId}/pay/${userId}`);
            }
            
            // Refresh the split modal data
            await openSplitModal(expenseId);
        } catch (err) {
            console.error('Failed to toggle payment status', err);
            alert('Failed to update payment status');
        }
    };

    const handleLeaveGroup = async () => {
        if (!groupId) return;
        
        const confirmed = window.confirm('Are you sure you want to leave this group?');
        if (!confirmed) return;
        
        try {
            await apiClient.delete(`/groups/${groupId}/leave`);
            alert('You have left the group');
            onBack();
        } catch (err) {
            console.error('Failed to leave group', err);
            alert('Failed to leave group');
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

            {/* Member count panel with statistics summary */}
            <div style={{
                marginTop: 12,
                padding: '8px 12px',
                background: 'var(--card-bg)',
                borderRadius: 8,
                border: '1px solid rgba(0,0,0,0.08)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                gap: 16,
                fontSize: 13,
                color: 'var(--muted-dark)',
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                        <circle cx="9" cy="7" r="4"></circle>
                        <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                        <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                    </svg>
                    <span><strong>{memberCount}</strong> {memberCount === 1 ? 'member' : 'members'}</span>
                </div>
                
                {statistics && (
                    <div style={{ 
                        display: 'flex', 
                        alignItems: 'center', 
                        gap: 12,
                        fontSize: 13,
                        fontWeight: 600,
                    }}>
                        <span style={{ color: 'green' }}>
                            {cur.formatAmount(statistics.my_share_of_expenses)} GOT
                        </span>
                        <span>|</span>
                        <span style={{ color: 'red' }}>
                            {cur.formatAmount(statistics.my_total_paid)} PAID
                        </span>
                        <span>|</span>
                        <span style={{ color: 'var(--purple-1)' }}>
                            {cur.formatAmount(statistics.rest_of_group_expenses)} EXTRA
                        </span>
                    </div>
                )}
            </div>

            {/* Statistics Panel */}
            {statistics && (
                <div style={{ marginTop: 12 }}>
                    <button
                        className="bp-add-btn"
                        onClick={() => setShowStatistics(!showStatistics)}
                        style={{ marginBottom: showStatistics ? 12 : 0 }}
                    >
                        {showStatistics ? 'Hide Details' : 'Show Details'}
                    </button>
                    
                    {showStatistics && (
                        <div style={{
                            padding: 16,
                            background: 'var(--card-bg)',
                            borderRadius: 12,
                            border: '1px solid rgba(0,0,0,0.08)',
                        }}>
                            <div style={{ fontWeight: 700, fontSize: 16, marginBottom: 12 }}>
                                Statistics for this group
                            </div>
                            
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                                <div style={{ 
                                    padding: 10, 
                                    background: '#fff', 
                                    borderRadius: 8,
                                    border: '1px solid rgba(0,0,0,0.05)',
                                }}>
                                    <div style={{ fontSize: 12, color: 'var(--muted-dark)', marginBottom: 4 }}>
                                        1. How much you got from shared expenses
                                    </div>
                                    <div style={{ fontSize: 18, fontWeight: 700, color: 'var(--purple-1)' }}>
                                        {cur.formatAmount(statistics.my_share_of_expenses)}
                                    </div>
                                </div>

                                <div style={{ 
                                    padding: 10, 
                                    background: '#fff', 
                                    borderRadius: 8,
                                    border: '1px solid rgba(0,0,0,0.05)',
                                }}>
                                    <div style={{ fontSize: 12, color: 'var(--muted-dark)', marginBottom: 4 }}>
                                        2. How much you paid for this group
                                    </div>
                                    <div style={{ fontSize: 18, fontWeight: 700, color: 'var(--purple-1)' }}>
                                        {cur.formatAmount(statistics.my_total_paid)}
                                    </div>
                                </div>

                                <div style={{ 
                                    padding: 10, 
                                    background: '#fff', 
                                    borderRadius: 8,
                                    border: '1px solid rgba(0,0,0,0.05)',
                                }}>
                                    <div style={{ fontSize: 12, color: 'var(--muted-dark)', marginBottom: 4 }}>
                                        3. How much you paid for other expenses
                                    </div>
                                    <div style={{ fontSize: 18, fontWeight: 700, color: statistics.net_balance_paid_for_others >= 0 ? 'green' : 'red' }}>
                                        {cur.formatAmount(statistics.net_balance_paid_for_others)}
                                    </div>
                                </div>

                                <div style={{ 
                                    padding: 10, 
                                    background: '#fff', 
                                    borderRadius: 8,
                                    border: '1px solid rgba(0,0,0,0.05)',
                                }}>
                                    <div style={{ fontSize: 12, color: 'var(--muted-dark)', marginBottom: 4 }}>
                                        4. Rest of group expenses (excluding yours)
                                    </div>
                                    <div style={{ fontSize: 18, fontWeight: 700, color: 'var(--purple-1)' }}>
                                        {cur.formatAmount(statistics.rest_of_group_expenses)}
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            )}

            <div className="bp-section-title" style={{ marginTop: 12, fontSize: 14 }}>Expenses for this group</div>

            {/* Chat-like timeline display with expenses and join/leave logs */}
            <div style={{ marginTop: 12, display: 'flex', flexDirection: 'column', gap: 8, paddingBottom: 20 }}>
                {timeline.map((item, index) => {
                    if (item.type === 'log') {
                        const logData = item.data as GroupLog & { userName: string };
                        
                        return (
                            <div
                                key={`log-${logData.id}`}
                                style={{
                                    display: 'flex',
                                    justifyContent: 'center',
                                    marginTop: 8,
                                    marginBottom: 8,
                                }}
                            >
                                <div style={{
                                    fontSize: 13,
                                    color: 'var(--muted-dark)',
                                    fontStyle: 'italic',
                                    padding: '8px 16px',
                                    background: 'rgba(0,0,0,0.03)',
                                    borderRadius: 12,
                                }}>
                                    {logData.userName} {logData.action === 'JOIN' ? 'joined' : 'left'} the group
                                </div>
                            </div>
                        );
                    }
                    
                    // Expense item
                    const e = item.data as Expense;
                    const isCurrentUser = e.userId === user?.id;
                    
                    return (
                        <div
                            key={`expense-${e.id}`}
                            style={{
                                display: 'flex',
                                flexDirection: 'column',
                                alignItems: isCurrentUser ? 'flex-end' : 'flex-start',
                                marginBottom: 4,
                            }}
                        >
                            {/* Username label (only for other users) */}
                            {!isCurrentUser && (
                                <div style={{ 
                                    fontSize: 11, 
                                    color: 'var(--muted-dark)', 
                                    marginBottom: 4,
                                    marginLeft: 8,
                                }}>
                                    {e.userName}
                                </div>
                            )}
                            
                            {/* Message bubble */}
                            <div
                                style={{
                                    maxWidth: '70%',
                                    padding: '12px 16px',
                                    borderRadius: isCurrentUser ? '18px 18px 4px 18px' : '18px 18px 18px 4px',
                                    background: isCurrentUser ? 'var(--purple-1)' : 'var(--card-bg)',
                                    color: isCurrentUser ? '#fff' : 'inherit',
                                    border: isCurrentUser ? 'none' : '1px solid rgba(0,0,0,0.08)',
                                    boxShadow: '0 1px 2px rgba(0,0,0,0.05)',
                                }}
                            >
                                <div style={{ 
                                    fontWeight: 600, 
                                    fontSize: 15,
                                    marginBottom: 4,
                                }}>
                                    {e.title}
                                </div>
                                
                                <div style={{ 
                                    fontSize: 20, 
                                    fontWeight: 800,
                                    marginBottom: 6,
                                }}>
                                    {e.amount < 0 ? '-' : ''}{cur.formatAmount(Math.abs(e.amount))}
                                </div>
                                
                                <div style={{ 
                                    fontSize: 11, 
                                    opacity: 0.8,
                                    display: 'flex',
                                    justifyContent: 'space-between',
                                    alignItems: 'center',
                                    gap: 12,
                                }}>
                                    <span>{e.category}</span>
                                    <span>{e.date}</span>
                                </div>
                                
                                {/* Split button for current user's expenses */}
                                {isCurrentUser && (
                                    <button
                                        onClick={() => openSplitModal(e.id)}
                                        style={{
                                            marginTop: 8,
                                            padding: '4px 12px',
                                            fontSize: 11,
                                            fontWeight: 600,
                                            background: 'rgba(255,255,255,0.2)',
                                            color: '#fff',
                                            border: '1px solid rgba(255,255,255,0.3)',
                                            borderRadius: 6,
                                            cursor: 'pointer',
                                            width: '100%',
                                        }}
                                    >
                                        Split
                                    </button>
                                )}
                            </div>
                        </div>
                    );
                })}
            </div>

            {/* footer action — Add expense */}
            <div style={{ marginTop: 18, display: 'flex', gap: 8, justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ display: 'flex', gap: 8 }}>
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

                <button
                    onClick={handleLeaveGroup}
                    style={{
                        padding: '10px 16px',
                        fontSize: 14,
                        fontWeight: 600,
                        background: '#fff',
                        color: '#e74c3c',
                        border: '2px solid #e74c3c',
                        borderRadius: 8,
                        cursor: 'pointer',
                    }}
                >
                    Leave Group
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
                            
                            // Refresh the expense list and statistics
                            await fetchGroupExpenses();
                            await fetchGroupInfo(); // This will refresh statistics
                            
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

            {/* Split Modal */}
            {splitModalExpenseId !== null && (
                <div
                    style={{
                        position: 'fixed',
                        top: 0,
                        left: 0,
                        right: 0,
                        bottom: 0,
                        background: 'rgba(0,0,0,0.5)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        zIndex: 1000,
                    }}
                    onClick={() => setSplitModalExpenseId(null)}
                >
                    <div
                        style={{
                            background: '#fff',
                            borderRadius: 16,
                            padding: 24,
                            maxWidth: 400,
                            width: '90%',
                            maxHeight: '80vh',
                            overflow: 'auto',
                        }}
                        onClick={(e) => e.stopPropagation()}
                    >
                        <div style={{ 
                            fontWeight: 700, 
                            fontSize: 18, 
                            marginBottom: 16,
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                        }}>
                            <span>Split Payment</span>
                            <button
                                onClick={() => setSplitModalExpenseId(null)}
                                style={{
                                    background: 'none',
                                    border: 'none',
                                    fontSize: 24,
                                    cursor: 'pointer',
                                    color: 'var(--muted-dark)',
                                }}
                            >
                                ×
                            </button>
                        </div>

                        {loadingSplit ? (
                            <div style={{ textAlign: 'center', padding: 20, color: 'var(--muted-dark)' }}>
                                Loading...
                            </div>
                        ) : (
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                                {groupUsers.map(user => (
                                    <label
                                        key={user.id}
                                        style={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: 12,
                                            padding: 12,
                                            background: 'var(--card-bg)',
                                            borderRadius: 8,
                                            cursor: 'pointer',
                                            border: '1px solid rgba(0,0,0,0.08)',
                                        }}
                                    >
                                        <input
                                            type="checkbox"
                                            checked={user.paid}
                                            onChange={() => togglePaymentStatus(splitModalExpenseId, user.id, user.paid)}
                                            style={{
                                                width: 18,
                                                height: 18,
                                                cursor: 'pointer',
                                            }}
                                        />
                                        <span style={{ 
                                            fontSize: 15, 
                                            fontWeight: 500,
                                            textDecoration: user.paid ? 'line-through' : 'none',
                                            opacity: user.paid ? 0.6 : 1,
                                        }}>
                                            {user.first_name}
                                        </span>
                                        {user.paid && (
                                            <span style={{ 
                                                marginLeft: 'auto', 
                                                fontSize: 12, 
                                                color: 'green',
                                                fontWeight: 600,
                                            }}>
                                                ✓ Paid
                                            </span>
                                        )}
                                    </label>
                                ))}
                            </div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};

export default GroupDetail;

