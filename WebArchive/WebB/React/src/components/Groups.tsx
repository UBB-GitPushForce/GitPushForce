// src/components/Groups.tsx
import React, { useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import '../App.css';

interface Group {
    id: number;
    name: string;
    members: number;
    thumb: string;
}

const Groups: React.FC<{ navigate: (to: 'home'|'groups'|'receipts'|'profile'|'support') => void }> = ({ navigate }) => {
    const { logout } = useAuth();
    const [isLoggingOut, setIsLoggingOut] = useState(false);

    const [groups, setGroups] = useState<Group[]>([
        { id: 1, name: 'Vacation', members: 2, thumb: 'V' },
        { id: 2, name: 'Household', members: 4, thumb: 'H' },
        { id: 3, name: 'Friends', members: 6, thumb: 'F' },
    ]);

    const handleLogout = async () => {
        setIsLoggingOut(true);
        try {
            await logout();
        } catch (err) {
            console.error('Logout failed', err);
            setIsLoggingOut(false);
        }
    };

    const handleCreateGroup = () => {
        const name = prompt('Group name');
        if (!name) return;
        const membersRaw = prompt('Number of members', '1');
        const members = membersRaw ? parseInt(membersRaw, 10) || 1 : 1;
        const id = (groups[groups.length - 1]?.id || 0) + 1;
        const thumb = name.trim()[0]?.toUpperCase() || 'G';
        setGroups(prev => [...prev, { id, name: name.trim(), members, thumb }]);
    };

    return (
        <>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 12 }}>
                <div style={{ fontWeight: 800, fontSize: 20, color: 'var(--text-dark)' }}>Groups</div>
            </div>

            <div style={{ marginTop: 12, fontWeight: 800, fontSize: 15, color: 'var(--text-dark)' }}>My Groups</div>

            <div style={{ marginTop: 10, display: 'flex', flexDirection: 'column', gap: 10 }}>
                {groups.map(g => (
                    <div className="bp-group-card" key={g.id}>
                        <div className="bp-group-thumb">{g.thumb}</div>
                        <div style={{ display: 'flex', flexDirection: 'column' }}>
                            <div style={{ fontWeight: 800, color: 'var(--text-dark)' }}>{g.name}</div>
                            <div style={{ color: 'var(--muted-dark)', fontSize: 13, marginTop: 6 }}>{g.members} members</div>
                        </div>
                    </div>
                ))}
            </div>

            <button className="bp-create-group" onClick={handleCreateGroup}>
                + Create Group
            </button>
        </>
    );
};

export default Groups;

