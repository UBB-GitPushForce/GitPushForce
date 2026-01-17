import React, { useState, useEffect } from 'react';
import apiClient from '../services/api-client';
import { useAuth } from '../hooks/useAuth';
import '../App.css';

interface Group {
    id: number;
    name: string;
    members: number;
    thumb: string;
}

const Groups: React.FC<{
    navigate: (to: 'home'|'groups'|'receipts'|'profile'|'groupDetail'|'data'|'categories') => void;
    openGroup: (groupId: number) => void;
}> = ({ navigate, openGroup }) => {
    const [groups, setGroups] = useState<Group[]>([]);
    const [loading, setLoading] = useState(false);
    const { user } = useAuth();
    const [userLoaded, setUserLoaded] = useState(false);
    const [showCreateForm, setShowCreateForm] = useState(false);
    const [showJoinForm, setShowJoinForm] = useState(false);
    const [newGroup, setNewGroup] = useState({ name: '', description: '' });
    const [invitationCode, setInvitationCode] = useState('');

    useEffect(() => {
        if (user && user.id) {
            setUserLoaded(true);
        }
    }, [user]);

    const fetchGroups = async () => {
        if (!user || !user.id) {
            return;
        }
        
        setLoading(true);
        try {
            const res = await apiClient.get('/groups/', {
                params: { offset: 0, limit: 1000 }
            });
            
            // Backend returns APIResponse { success: true, data: [groups] }
            const responseData = res.data;
            const allGroups = responseData?.data || responseData || [];
            
            if (!Array.isArray(allGroups)) {
                setGroups([]);
                return;
            }
            
            // Filter to only groups where user is a member
            const userGroupsPromises = allGroups.map(async (g: any) => {
                try {
                    const membersRes = await apiClient.get(`/groups/${g.id}/users`);
                    // Backend returns APIResponse { success: true, data: [users] }
                    const responseData = membersRes.data;
                    const members = responseData?.data || responseData || [];
                    
                    if (!Array.isArray(members)) {
                        return null;
                    }
                    
                    const isMember = members.some((m: any) => m.id === user.id || m.user_id === user.id);
                    return isMember ? g : null;
                } catch (err) {
                    console.error(`Failed to check membership for group ${g.id}`, err);
                    return null;
                }
            });
            
            const userGroupsResults = await Promise.all(userGroupsPromises);
            const items = userGroupsResults.filter(g => g !== null);
            
            // Fetch member count for each group
            const mapped: Group[] = await Promise.all(
                items.map(async (g: any) => {
                    let memberCount = 0;
                    try {
                        const countRes = await apiClient.get(`/groups/${g.id}/users/nr`);
                        const responseData = countRes.data;
                        memberCount = typeof responseData === 'number' ? responseData : (responseData?.data || 0);
                        
                        if (typeof memberCount !== 'number') {
                            memberCount = 0;
                        }
                    } catch (err) {
                        console.error(`Failed to fetch member count for group ${g.id}`, err);
                    }
                    
                    return {
                        id: g.id,
                        name: g.name || 'Unnamed Group',
                        members: memberCount,
                        thumb: (g.name?.[0] || 'G').toUpperCase(),
                    };
                })
            );
            
            setGroups(mapped);
        } catch (err) {
            console.error('Failed to fetch groups', err);
            setGroups([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (user && user.id) {
            fetchGroups();
        }
    }, [user, userLoaded]);

    const handleCreateGroup = async (e: React.FormEvent) => {
        e.preventDefault();
        
        if (!user || !user.id) {
            alert('You must be logged in to create a group. Please wait for authentication to complete.');
            return;
        }
        
        if (!newGroup.name.trim()) {
            alert('Please enter a group name');
            return;
        }

        try {
            const body: any = {
                name: newGroup.name.trim(),
            };
            if (newGroup.description && newGroup.description.trim()) {
                body.description = newGroup.description.trim();
            }

            const createRes = await apiClient.post('/groups/', body);
            
            // Backend returns APIResponse { success: true, data: { id: groupId } }
            const responseData = createRes.data;
            const groupId = responseData?.data?.id || responseData?.id;
            
            if (groupId && user.id) {
                try {
                    await apiClient.post(`/groups/${groupId}/users/${user.id}`);
                } catch (err: any) {
                    console.error('Failed to add creator to group', err);
                }
            }
            
            await fetchGroups();
            setNewGroup({ name: '', description: '' });
            setShowCreateForm(false);
            alert('Group created successfully!');
        } catch (err: any) {
            console.error('Failed to create group', err);
            alert(err?.response?.data?.detail || 'Failed to create group');
        }
    };

    const handleJoinGroup = async (e: React.FormEvent) => {
        e.preventDefault();
        
        if (!invitationCode || !invitationCode.trim()) {
            alert('Please enter an invitation code');
            return;
        }

        try {
            await apiClient.post(`/users/join-group/${invitationCode.trim()}`);
            // Successfully joined
            await fetchGroups();
            setInvitationCode('');
            setShowJoinForm(false);
            alert('Successfully joined the group!');
        } catch (err: any) {
            console.error('Failed to join group', err);
            
            // Backend has a bug where it returns 500 after successfully adding user to group
            // The database operation completes, but the response serialization fails
            // So we check if it's a 500 error and try to refresh groups anyway
            if (err?.response?.status === 500 || err?.code === 'ERR_NETWORK') {
                // Try refreshing groups - user might have been added successfully
                await fetchGroups();
                setInvitationCode('');
                setShowJoinForm(false);
                alert('You may have joined the group. Please check your groups list.');
            } else {
                alert(err?.response?.data?.detail || 'Failed to join group. Check the invitation code.');
            }
        }
    };

    return (
        <>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 12 }}>
                <div className="bp-title">Groups</div>
            </div>

            {/* Join Group Form */}
            {showJoinForm && (
                <div style={{ 
                    marginTop: 15, 
                    padding: 20, 
                    background: 'var(--card-bg)', 
                    borderRadius: 12, 
                    border: '1px solid rgba(0,0,0,0.08)' 
                }}>
                    <div style={{ fontWeight: 700, fontSize: 16, marginBottom: 15 }}>Join Group</div>
                    
                    <form onSubmit={handleJoinGroup} style={{ display: 'grid', gap: 12 }}>
                        <div>
                            <label style={{ display: 'block', marginBottom: 6, fontSize: 14, fontWeight: 600 }}>
                                Invitation Code
                            </label>
                            <input
                                type="text"
                                value={invitationCode}
                                onChange={(e) => setInvitationCode(e.target.value)}
                                placeholder="e.g., A2VC3B"
                                style={{
                                    width: '100%',
                                    padding: '10px 12px',
                                    borderRadius: 8,
                                    border: '1px solid rgba(0,0,0,0.1)',
                                    fontSize: 14,
                                    textTransform: 'uppercase',
                                }}
                                required
                            />
                        </div>

                        <button
                            type="submit"
                            className="bp-add-btn"
                            style={{ marginTop: 8 }}
                        >
                            Join Group
                        </button>
                    </form>
                </div>
            )}

            <div className="bp-section-title" style={{ marginTop: 12, fontSize: 15 }}>My Groups</div>

            <div style={{ marginTop: 10, display: 'flex', flexDirection: 'column', gap: 10 }}>
                {loading ? (
                    <div style={{ textAlign: 'center', padding: 20, color: 'var(--muted-dark)' }}>
                        Loading groups...
                    </div>
                ) : groups.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: 20, color: 'var(--muted-dark)' }}>
                        No groups yet. Create your first group!
                    </div>
                ) : (
                    groups.map(g => (
                        <div
                            className="bp-group-card"
                            key={g.id}
                            onClick={() => openGroup(g.id)}
                            style={{ cursor: 'pointer' }}
                            role="button"
                            aria-label={`Open group ${g.name}`}
                        >
                            <div className="bp-group-thumb">{g.thumb}</div>
                            <div style={{ display: 'flex', flexDirection: 'column' }}>
                                <div style={{ fontWeight: 800 }}>{g.name}</div>
                                <div style={{ color: 'var(--muted-dark)', fontSize: 13, marginTop: 6 }}>{g.members} members</div>
                            </div>
                        </div>
                    ))
                )}
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 12 }}>
                <button 
                    className="bp-add-btn" 
                    onClick={() => setShowJoinForm(!showJoinForm)}
                    style={{ fontSize: 14, padding: '8px 16px' }}
                >
                    {showJoinForm ? 'Cancel' : 'Join Group'}
                </button>

                <button 
                    className="bp-add-btn" 
                    onClick={() => setShowCreateForm(!showCreateForm)}
                    style={{ fontSize: 14, padding: '8px 16px' }}
                >
                    {showCreateForm ? 'Cancel' : '+ Create Group'}
                </button>
            </div>

            {/* Create Group Form */}
            {showCreateForm && (
                <div style={{ 
                    marginTop: 15, 
                    padding: 20, 
                    background: 'var(--card-bg)', 
                    borderRadius: 12, 
                    border: '1px solid rgba(0,0,0,0.08)' 
                }}>
                    <div style={{ fontWeight: 700, fontSize: 16, marginBottom: 15 }}>Create New Group</div>
                    
                    <form onSubmit={handleCreateGroup} style={{ display: 'grid', gap: 12 }}>
                        <div>
                            <label style={{ display: 'block', marginBottom: 6, fontSize: 14, fontWeight: 600 }}>
                                Group Name
                            </label>
                            <input
                                type="text"
                                value={newGroup.name}
                                onChange={(e) => setNewGroup(prev => ({ ...prev, name: e.target.value }))}
                                placeholder="e.g., Vacation 2025"
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
                                Description (Optional)
                            </label>
                            <textarea
                                value={newGroup.description}
                                onChange={(e) => setNewGroup(prev => ({ ...prev, description: e.target.value }))}
                                placeholder="What is this group for?"
                                style={{
                                    width: '100%',
                                    padding: '10px 12px',
                                    borderRadius: 8,
                                    border: '1px solid rgba(0,0,0,0.1)',
                                    fontSize: 14,
                                    minHeight: 80,
                                    resize: 'vertical',
                                    fontFamily: 'inherit',
                                }}
                            />
                        </div>

                        <button
                            type="submit"
                            className="bp-add-btn"
                            style={{ marginTop: 8 }}
                        >
                            Create Group
                        </button>
                    </form>
                </div>
            )}
        </>
    );
};

export default Groups;

