// src/components/ReceiptsUpload.tsx
import React, { useRef, useState } from 'react';
import '../App.css';
import type { ReceiptItem } from './Receipts';

/*
  Upload page: file picker + "drop file" area + max size enforcement.
  Calls onUploaded with a mock created ReceiptItem.
  TODO: replace uploadMock(...) with actual API call to backend OCR endpoint.
*/

const MAX_BYTES = 5 * 1024 * 1024; // 5 MB
const allowedTypes = ['image/png','image/jpeg','image/jpg','application/pdf'];

async function uploadMock(file: File, linkGroupId?: number | null): Promise<ReceiptItem> {
  // TODO: replace with real upload + OCR API
  await new Promise(r => setTimeout(r, 800));
  const now = new Date().toISOString().slice(0,10);
  return {
    id: Date.now(),
    title: file.name.replace(/\.[^.]+$/, ''),
    subtitle: 'Scanned (mock)',
    amount: Math.round((Math.random()*200+1) * (Math.random()>0.7?1:-1)),
    dateTransaction: now,
    dateAdded: now,
    isGroup: !!linkGroupId,
    groupId: linkGroupId || undefined,
    groupName: linkGroupId ? (linkGroupId === 1 ? 'Vacation' : linkGroupId === 2 ? 'Household' : 'Friends') : undefined,
    addedBy: 'You',
    initial: 'Y',
  };
}

const ReceiptsUpload: React.FC<{ onUploaded: (it: ReceiptItem) => void; groupId?: number | null }> = ({ onUploaded, groupId = null }) => {
  const inputRef = useRef<HTMLInputElement | null>(null);
  const [busy, setBusy] = useState(false);
  const [selectedGroup, setSelectedGroup] = useState<number | null>(groupId ?? null);

  const handleFiles = async (fileList: FileList | null) => {
    if (!fileList || fileList.length === 0) return;
    const file = fileList[0];
    if (file.size > MAX_BYTES) {
      alert('File too large. Max size: 5 MB.');
      return;
    }
    if (!allowedTypes.includes(file.type)) {
      alert('Unsupported file type. Use PNG, JPG or PDF.');
      return;
    }

    setBusy(true);
    try {
      const result = await uploadMock(file, selectedGroup);
      onUploaded(result);
    } catch (err) {
      console.error(err);
      alert('Upload failed (mock).');
    } finally {
      setBusy(false);
    }
  };

  const onDrop = (e: React.DragEvent) => {
    e.preventDefault(); e.stopPropagation();
    handleFiles(e.dataTransfer.files);
  };
  const onDragOver = (e: React.DragEvent) => { e.preventDefault(); e.stopPropagation(); };

  return (
    <div style={{ display:'grid', gap:12 }}>
      <div style={{ color:'var(--muted-dark)' }}>Allowed types: .pdf, .png, .jpg. Max size: 5 MB.</div>

      <div
        onDrop={onDrop}
        onDragOver={onDragOver}
        style={{
          border: '2px dashed rgba(0,0,0,0.06)',
          padding: 18,
          borderRadius: 10,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          cursor: 'pointer',
          background: 'linear-gradient(180deg, rgba(255,255,255,0.01), rgba(255,255,255,0.005))'
        }}
        onClick={() => inputRef.current?.click()}
      >
        <div style={{ textAlign:'center', color:'var(--muted-dark)' }}>
          <div style={{ fontWeight:800, color:'var(--text-dark)' }}>Drop file here</div>
          <div style={{ fontSize:13 }}>or click to open file dialog</div>
        </div>
      </div>

      <input ref={inputRef} type="file" accept=".pdf,image/png,image/jpeg" style={{ display:'none' }} onChange={(e)=> handleFiles(e.target.files)} />

      <div style={{ display:'flex', gap:8, alignItems:'center' }}>
        <div style={{ display:'flex', gap:8, alignItems:'center' }}>
          <div style={{ fontSize:13, color:'var(--muted-dark)' }}>Link to group (optional):</div>
          <select value={selectedGroup ?? ''} onChange={(e)=> setSelectedGroup(e.target.value ? Number(e.target.value) : null)} style={{ padding:8, borderRadius:8 }}>
            <option value="">(independent)</option>
            <option value={1}>Vacation</option>
            <option value={2}>Household</option>
            <option value={3}>Friends</option>
          </select>
        </div>

        <div style={{ marginLeft:'auto' }}>
          <button className="btn" onClick={() => inputRef.current?.click()} disabled={busy}>{busy ? 'Uploading...' : 'Choose file'}</button>
        </div>
      </div>
    </div>
  );
};

export default ReceiptsUpload;

