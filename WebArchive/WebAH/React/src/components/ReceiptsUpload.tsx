// src/components/ReceiptsUpload.tsx
import React, { useRef, useState } from 'react';
import '../App.css';
import type { Group } from '../services/group-service';

const MAX_BYTES = 5 * 1024 * 1024; // 5 MB
const allowedTypes = ['image/png', 'image/jpeg', 'image/jpg', 'application/pdf'];

interface ReceiptsUploadProps {
  // CHANGED: We now pass the raw File back, not a processed Item
  onFileSelected: (file: File) => void;
  // We keep these for type compatibility, though the parent now manages the group logic
  groupId?: number | null;
  groups?: Group[];
}

const ReceiptsUpload: React.FC<ReceiptsUploadProps> = ({ onFileSelected }) => {
  const inputRef = useRef<HTMLInputElement | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const handleFiles = (fileList: FileList | null) => {
    setErrorMsg(null);
    if (!fileList || fileList.length === 0) return;
    
    const file = fileList[0];

    // Validation
    if (file.size > MAX_BYTES) {
      alert('File too large. Max size: 5 MB.');
      return;
    }
    if (!allowedTypes.includes(file.type)) {
      alert('Unsupported file type. Use PNG, JPG or PDF.');
      return;
    }

    // Pass file to parent to trigger AI analysis
    onFileSelected(file);
  };

  const onDrop = (e: React.DragEvent) => {
    e.preventDefault(); 
    e.stopPropagation();
    handleFiles(e.dataTransfer.files);
  };

  const onDragOver = (e: React.DragEvent) => { 
    e.preventDefault(); 
    e.stopPropagation(); 
  };

  return (
    <div style={{ display:'grid', gap:12 }}>
      <div style={{ color:'var(--muted-dark)' }}>Allowed types: .pdf, .png, .jpg. Max size: 5 MB.</div>

      <div
        onDrop={onDrop}
        onDragOver={onDragOver}
        style={{
          border: '2px dashed #ccc',
          padding: 30,
          borderRadius: 10,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          cursor: 'pointer',
          background: '#f9f9f9',
          minHeight: 150
        }}
        onClick={() => inputRef.current?.click()}
      >
        <div className="bp-section-title" style={{marginBottom: 8}}>Drop file here</div>
        <div style={{ fontSize:13, color: '#666' }}>or click to open file dialog</div>
      </div>

      <input 
        ref={inputRef} 
        type="file" 
        accept=".pdf,image/png,image/jpeg" 
        style={{ display:'none' }} 
        onChange={(e)=> handleFiles(e.target.files)} 
      />

      <div style={{ marginLeft:'auto' }}>
        <button className="bp-add-btn" onClick={() => inputRef.current?.click()}>
            Choose file
        </button>
      </div>
    </div>
  );
};

export default ReceiptsUpload;