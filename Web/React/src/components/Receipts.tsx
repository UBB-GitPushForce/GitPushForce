// src/components/Receipts.tsx
import React, { useState } from 'react';
import '../App.css';
import ReceiptsView from './ReceiptsView';
import ReceiptsManual from './ReceiptsManual';
import ReceiptsUpload from './ReceiptsUpload';
import ReceiptsCamera from './ReceiptsCamera';

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

const mockGroups = [
  { id: 1, name: 'Vacation' },
  { id: 2, name: 'Household' },
  { id: 3, name: 'Friends' },
];

const Receipts: React.FC<{ navigate?: (to: string) => void }> = ({ navigate }) => {
  // simple internal pages
  const [subpage, setSubpage] = useState<'menu'|'chooseAdd'|'addOptions'|'view'|'manual'|'upload'|'camera'>('menu');

  // groupId when adding a group-linked receipt
  const [selectedGroup, setSelectedGroup] = useState<number | null>(null);

  // injected receipts (appear immediately in view after creation)
  const [injectedReceipts, setInjectedReceipts] = useState<ReceiptItem[]>([]);

  const addInjectedReceipt = (it: ReceiptItem) => {
    setInjectedReceipts(prev => [it, ...prev]);
  };

  return (
    <div style={{ marginTop: 12 }}>
      {/* Header */}
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
        <div style={{ fontWeight:800, fontSize:20, color:'var(--text-dark)' }}>Receipts</div>
        <div style={{ color:'var(--muted-dark)' }}>History & uploads</div>
      </div>

      {/* Menu */}
      {subpage === 'menu' && (
        <>
          <div style={{ marginTop: 12, display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 12 }}>
            <div>
              <button className="bp-add-btn" style={{ width: '100%' }} onClick={() => { setSelectedGroup(null); setSubpage('chooseAdd'); }}>Add receipt</button>
              <div style={{ marginTop:8, color:'var(--muted-dark)', fontSize:13 }}>Add a single or group transaction.</div>
            </div>

            <div>
              <button className="btn" style={{ width: '100%' }} onClick={() => setSubpage('view')}>View receipts</button>
              <div style={{ marginTop:8, color:'var(--muted-dark)', fontSize:13 }}>Browse full history with filters and infinite scroll.</div>
            </div>
          </div>
        </>
      )}

      {/* Choose single or group */}
      {subpage === 'chooseAdd' && (
        <div style={{ marginTop: 12, display: 'grid', gap: 12 }}>
          <div style={{ fontWeight:700 }}>Add receipt: choose type</div>

          <div style={{ display:'flex', gap:8 }}>
            <button className="btn" onClick={() => { setSelectedGroup(null); setSubpage('addOptions'); }}>Single (only you)</button>

            <div style={{ display:'flex', gap:8, alignItems:'center' }}>
              <select value={selectedGroup ?? ''} onChange={(e) => setSelectedGroup(e.target.value ? Number(e.target.value) : null)}>
                <option value="">Select group...</option>
                {mockGroups.map(g => <option key={g.id} value={g.id}>{g.name}</option>)}
              </select>
              <button className="btn" onClick={() => {
                if (!selectedGroup) { alert('Select a group first'); return; }
                setSubpage('addOptions');
              }}>Group</button>
            </div>

            <div style={{ marginLeft: 'auto' }}>
              <button className="btn" onClick={() => setSubpage('menu')}>Cancel</button>
            </div>
          </div>
        </div>
      )}

      {/* Add options (manual / upload / camera) */}
      {subpage === 'addOptions' && (
        <div style={{ marginTop: 12, display: 'grid', gap: 12 }}>
          <div style={{ fontWeight:700 }}>How do you want to add the receipt?</div>

          <div style={{ display:'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px,1fr))', gap:12 }}>
            <div>
              <button className="bp-add-btn" style={{ width:'100%' }} onClick={() => setSubpage('manual')}>Add manually</button>
              <div style={{ marginTop:8, color:'var(--muted-dark)', fontSize:13 }}>Type values yourself.</div>
            </div>

            <div>
              <button className="btn" style={{ width:'100%' }} onClick={() => setSubpage('upload')}>Upload file</button>
              <div style={{ marginTop:8, color:'var(--muted-dark)', fontSize:13 }}>Upload PDF / PNG / JPG.</div>
            </div>

            <div>
              <button className="btn" style={{ width:'100%' }} onClick={() => setSubpage('camera')}>Use camera</button>
              <div style={{ marginTop:8, color:'var(--muted-dark)', fontSize:13 }}>Open camera and take a photo.</div>
            </div>
          </div>

          <div style={{ marginTop: 8 }}>
            <button className="btn" onClick={() => setSubpage('menu')}>Back</button>
          </div>
        </div>
      )}

      {/* VIEW */}
      {subpage === 'view' && (
        <div style={{ marginTop:12 }}>
          <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
            <div style={{ fontWeight:800, fontSize:18, color:'var(--text-dark)' }}>All receipts</div>
            <div style={{ display:'flex', gap:8 }}>
              <button className="btn" onClick={() => setSubpage('menu')}>Back</button>
              <button className="btn" onClick={() => { setSubpage('chooseAdd'); }}>Add receipt</button>
            </div>
          </div>

          <div style={{ marginTop:12 }}>
            <div className="receipts-grid-container">
              <ReceiptsView injectedReceipts={injectedReceipts} />
            </div>
          </div>
        </div>
      )}

      {/* MANUAL */}
      {subpage === 'manual' && (
        <div style={{ marginTop:12 }}>
          <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
            <div style={{ fontWeight:800, fontSize:18, color:'var(--text-dark)' }}>Add receipt — Manual</div>
            <div style={{ display:'flex', gap:8 }}>
              <button className="btn" onClick={() => setSubpage('addOptions')}>Back</button>
            </div>
          </div>

          <div style={{ marginTop:12 }}>
            <ReceiptsManual
              onCreated={(it) => { addInjectedReceipt(it); setSubpage('view'); }}
              groupId={selectedGroup}
            />
          </div>
        </div>
      )}

      {/* UPLOAD */}
      {subpage === 'upload' && (
        <div style={{ marginTop:12 }}>
          <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
            <div style={{ fontWeight:800, fontSize:18, color:'var(--text-dark)' }}>Add receipt — Upload</div>
            <div style={{ display:'flex', gap:8 }}>
              <button className="btn" onClick={() => setSubpage('addOptions')}>Back</button>
            </div>
          </div>

          <div style={{ marginTop:12 }}>
            <ReceiptsUpload onUploaded={(it) => { addInjectedReceipt(it); setSubpage('view'); }} groupId={selectedGroup} />
          </div>
        </div>
      )}

      {/* CAMERA */}
      {subpage === 'camera' && (
        <div style={{ marginTop:12 }}>
          <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
            <div style={{ fontWeight:800, fontSize:18, color:'var(--text-dark)' }}>Add receipt — Camera</div>
            <div style={{ display:'flex', gap:8 }}>
              <button className="btn" onClick={() => setSubpage('addOptions')}>Back</button>
            </div>
          </div>

          <div style={{ marginTop:12 }}>
            <ReceiptsCamera onUploaded={(it) => { addInjectedReceipt(it); setSubpage('view'); }} groupId={selectedGroup} />
          </div>
        </div>
      )}
    </div>
  );
};

export default Receipts;

