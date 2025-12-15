// src/components/Receipts.tsx
import React, { useState, useEffect } from 'react';
import '../App.css';
import ReceiptsView from './ReceiptsView';
import ReceiptsUpload from './ReceiptsUpload';
import ReceiptsCamera from './ReceiptsCamera';
import { useAuth } from '../hooks/useAuth';
import groupService, { Group } from '../services/group-service';

export type ReceiptItem = {
  id: number;
  title: string;
  subtitle: string;
  amount: number;
  dateTransaction: string;
  dateAdded: string;
  isGroup: boolean;
  groupId?: number;
  groupName?: string;
  addedBy?: string;
  initial?: string;
};

const Receipts: React.FC<{ navigate?: (to: string) => void }> = ({ navigate }) => {
  const { user } = useAuth();
  // Modified: default entry is the "chooseAdd" step so Receipts page shows the Add flow directly.
  const [subpage, setSubpage] = useState<'menu'|'chooseAdd'|'addOptions'|'view'|'upload'|'camera'>('chooseAdd');

  const [groups, setGroups] = useState<Group[]>([]);
  const [loadingGroups, setLoadingGroups] = useState(false);

  // groupId when adding a group-linked receipt
  const [selectedGroup, setSelectedGroup] = useState<number | null>(null);

  // refreshKey: increment to trigger ReceiptsView reload
  const [refreshKey, setRefreshKey] = useState<number>(0);

  // Fetch groups on mount
  useEffect(() => {
    if (!user) return;
    const fetchGroups = async () => {
      setLoadingGroups(true);
      try {
        const data = await groupService.getUserGroups(user.id);
        setGroups(data);
      } catch (err) {
        console.error('Failed to load groups', err);
      } finally {
        setLoadingGroups(false);
      }
    };
    fetchGroups();
  }, [user]);

  const triggerRefresh = () => {
    setRefreshKey(prev => prev + 1);
  };

  return (
    <div style={{ marginTop: 12 }}>
      {/* Header */}
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
        <div className="bp-title">Receipts</div>
        <div style={{ color:'var(--muted-dark)' }}>History & uploads</div>
      </div>

      {/* Choose single or group */}
      {subpage === 'chooseAdd' && (
        <div style={{ marginTop: 12, display: 'grid', gap: 12 }}>
          <div style={{ fontWeight:700 }}>Add receipt: choose type</div>

            <div style={{ display:'flex', gap:8 }}>
            <button className="bp-add-btn" onClick={() => { setSelectedGroup(null); setSubpage('addOptions'); }}>Single (only you)</button>

            <div style={{ display:'flex', gap:8, alignItems:'center' }}>
              <select 
                value={selectedGroup ?? ''} 
                onChange={(e) => setSelectedGroup(e.target.value ? Number(e.target.value) : null)}
                disabled={loadingGroups}
              >
                <option value="">{loadingGroups ? 'Loading...' : 'Select group...'}</option>
                {groups.map(g => <option key={g.id} value={g.id}>{g.name}</option>)}
              </select>
              <button className="bp-add-btn" onClick={() => {
                if (!selectedGroup) { alert('Select a group first'); return; }
                setSubpage('addOptions');
              }}>Group</button>
            </div>

            <div style={{ marginLeft: 'auto' }}>
              <button className="bp-add-btn" onClick={() => setSubpage('chooseAdd')}>Refresh</button>
            </div>
          </div>
        </div>
      )}

      {/* Add options (manual / upload / camera) */}
      {subpage === 'addOptions' && (
        <div style={{ marginTop: 12, display: 'grid', gap: 12 }}>
          <div style={{ fontWeight:700 }}>How do you want to add the receipt?</div>
          {selectedGroup && (
             <div style={{ fontSize: 13, color: 'var(--muted-dark)' }}>
               Adding to group: <strong>{groups.find(g => g.id === selectedGroup)?.name}</strong>
             </div>
          )}

          <div style={{ display:'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px,1fr))', gap:12 }}>
            {/* Manual add removed: use Home 'Add Expense' instead */}

            <div>
              <button className="bp-add-btn" style={{ width:'100%' }} onClick={() => setSubpage('upload')}>Upload file</button>
              <div style={{ marginTop:8, color:'var(--muted-dark)', fontSize:13 }}>Upload PDF / PNG / JPG.</div>
            </div>

            <div>
              <button className="bp-add-btn" style={{ width:'100%' }} onClick={() => setSubpage('camera')}>Use camera</button>
              <div style={{ marginTop:8, color:'var(--muted-dark)', fontSize:13 }}>Open camera and take a photo.</div>
            </div>
          </div>

          <div style={{ marginTop: 8 }}>
            <button className="bp-add-btn" onClick={() => setSubpage('chooseAdd')}>Back</button>
          </div>
        </div>
      )}



      {/* UPLOAD */}
      {subpage === 'upload' && (
        <div style={{ marginTop:12 }}>
          <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
            <div className="bp-section-title">Add receipt — Upload</div>
            <div style={{ display:'flex', gap:8 }}>
              <button className="bp-add-btn" onClick={() => setSubpage('addOptions')}>Back</button>
            </div>
          </div>

          <div style={{ marginTop:12 }}>
            <ReceiptsUpload 
                onUploaded={(it) => { triggerRefresh(); setSubpage('chooseAdd'); }} 
                groupId={selectedGroup} 
                groups={groups}
            />
          </div>
        </div>
      )}

      {/* CAMERA */}
      {subpage === 'camera' && (
        <div style={{ marginTop:12 }}>
          <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
            <div className="bp-section-title">Add receipt — Camera</div>
            <div style={{ display:'flex', gap:8 }}>
              <button className="bp-add-btn" onClick={() => setSubpage('addOptions')}>Back</button>
            </div>
          </div>

          <div style={{ marginTop:12 }}>
            <ReceiptsCamera 
                onUploaded={(it) => { triggerRefresh(); setSubpage('chooseAdd'); }} 
                groupId={selectedGroup}
                groups={groups}
            />
          </div>
        </div>
      )}
    </div>
  );
};

export default Receipts;

