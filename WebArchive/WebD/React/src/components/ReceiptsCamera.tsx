// src/components/ReceiptsCamera.tsx
import React, { useRef, useState } from 'react';
import '../App.css';
import type { ReceiptItem } from './Receipts';

/*
  Camera page: shows video preview, allows snapshot and uploads mock.
  TODO: replace uploadMockFromBlob with real upload + OCR API.
*/

async function uploadMockFromBlob(blob: Blob, linkGroupId?: number | null): Promise<ReceiptItem> {
  // TODO: send blob to backend and return created receipt
  await new Promise(r => setTimeout(r, 900));
  const now = new Date().toISOString().slice(0,10);
  return {
    id: Date.now(),
    title: 'photo-' + Date.now(),
    subtitle: 'Camera (mock)',
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

const ReceiptsCamera: React.FC<{ onUploaded: (it: ReceiptItem) => void; groupId?: number | null }> = ({ onUploaded, groupId = null }) => {
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const [stream, setStream] = useState<MediaStream | null>(null);
  const [taking, setTaking] = useState(false);
  const [selectedGroup, setSelectedGroup] = useState<number | null>(groupId ?? null);

  const mockGroups = [
    { id: 1, name: 'Vacation' },
    { id: 2, name: 'Household' },
    { id: 3, name: 'Friends' },
  ];

  const startCamera = async () => {
    if (stream) return;
    try {
      const s = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' }, audio: false });
      setStream(s);
      if (videoRef.current) { videoRef.current.srcObject = s; await videoRef.current.play(); }
    } catch (err) {
      console.error('Camera error', err);
      alert('Cannot access camera.');
    }
  };

  const stopCamera = () => {
    if (stream) { stream.getTracks().forEach(t => t.stop()); setStream(null); }
    if (videoRef.current) { try { videoRef.current.pause(); videoRef.current.srcObject = null; } catch{} }
  };

  const takePhoto = async () => {
    if (!videoRef.current) return;
    setTaking(true);
    try {
      const vw = videoRef.current.videoWidth;
      const vh = videoRef.current.videoHeight;
      const canvas = canvasRef.current!;
      canvas.width = vw; canvas.height = vh;
      const ctx = canvas.getContext('2d')!;
      ctx.drawImage(videoRef.current, 0, 0, vw, vh);
      canvas.toBlob(async (blob) => {
        if (!blob) { alert('Capture failed'); setTaking(false); return; }
        try {
          const result = await uploadMockFromBlob(blob, selectedGroup);
          onUploaded(result);
          stopCamera();
        } catch (err) {
          console.error(err);
          alert('Upload failed (mock)');
        } finally {
          setTaking(false);
        }
      }, 'image/jpeg', 0.9);
    } catch (err) {
      console.error(err);
      setTaking(false);
    }
  };

  return (
    <div style={{ display:'grid', gap:12 }}>
      <div style={{ color:'var(--muted-dark)' }}>Use your device camera to capture the receipt. After taking the photo it will be uploaded (mock).</div>

      <div style={{ display:'flex', gap:8 }}>
        <div style={{ display:'flex', gap:8, alignItems:'center' }}>
          <div style={{ fontSize:13, color:'var(--muted-dark)' }}>Link to group (optional):</div>
          <select value={selectedGroup ?? ''} onChange={(e)=> setSelectedGroup(e.target.value ? Number(e.target.value) : null)} style={{ padding:8, borderRadius:8 }}>
            <option value="">(independent)</option>
            <option value={1}>Vacation</option>
            <option value={2}>Household</option>
            <option value={3}>Friends</option>
          </select>
        </div>

        <div style={{ marginLeft:'auto', display:'flex', gap:8 }}>
          <button className="btn" onClick={startCamera}>Open camera</button>
          <button className="btn" onClick={stopCamera}>Close camera</button>
        </div>
      </div>

      <div style={{ display:'flex', gap:12, flexDirection:'column', alignItems:'center' }}>
        <video ref={videoRef} style={{ width:'100%', maxWidth:920, borderRadius:12, background:'#000' }} playsInline />
        <canvas ref={canvasRef} style={{ display:'none' }} />
        <div style={{ display:'flex', gap:8 }}>
          <button className="bp-add-btn" onClick={takePhoto} disabled={!stream || taking}>{taking ? 'Taking...' : 'Take photo & upload'}</button>
        </div>
      </div>
    </div>
  );
};

export default ReceiptsCamera;

