// src/components/ReceiptsCamera.tsx
import React, { useRef, useState, useEffect } from 'react';
import '../App.css';
import type { Group } from '../services/group-service';

interface ReceiptsCameraProps {
  // CHANGED: Pass the captured file back to parent
  onPhotoTaken: (file: File) => void;
  groupId?: number | null;
  groups?: Group[];
}

const ReceiptsCamera: React.FC<ReceiptsCameraProps> = ({ onPhotoTaken }) => {
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const [stream, setStream] = useState<MediaStream | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  // Auto-start camera on mount, cleanup on unmount
  useEffect(() => {
    startCamera();
    return () => {
      stopCamera();
    };
  }, []);

  const startCamera = async () => {
    if (stream) return;
    try {
      // Prefer back camera ('environment')
      const s = await navigator.mediaDevices.getUserMedia({ 
        video: { facingMode: 'environment' }, 
        audio: false 
      });
      setStream(s);
      if (videoRef.current) { 
        videoRef.current.srcObject = s; 
        await videoRef.current.play(); 
      }
    } catch (err) {
      console.error('Camera error', err);
      setErrorMsg('Cannot access camera. Please ensure permissions are granted.');
    }
  };

  const stopCamera = () => {
    if (stream) { 
        stream.getTracks().forEach(t => t.stop()); 
        setStream(null); 
    }
    if (videoRef.current) { 
        videoRef.current.srcObject = null; 
    }
  };

  const capturePhoto = () => {
    if (!videoRef.current || !canvasRef.current) return;

    const vw = videoRef.current.videoWidth;
    const vh = videoRef.current.videoHeight;
    const canvas = canvasRef.current;
    
    canvas.width = vw; 
    canvas.height = vh;
    
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    ctx.drawImage(videoRef.current, 0, 0, vw, vh);

    // Convert canvas to Blob, then to File
    canvas.toBlob((blob) => {
      if (!blob) {
        alert('Capture failed'); 
        return; 
      }
      
      // Create a File object from the Blob
      const file = new File([blob], `camera_capture_${Date.now()}.jpg`, { type: "image/jpeg" });
      
      stopCamera(); // Stop stream
      onPhotoTaken(file); // Send to parent for AI processing
    }, 'image/jpeg', 0.95);
  };

  return (
    <div style={{ display:'grid', gap:12 }}>
      {errorMsg && <div style={{color: 'red', textAlign:'center'}}>{errorMsg}</div>}

      <div style={{ position: 'relative', width:'100%', borderRadius:12, overflow:'hidden', background:'#000', minHeight: 200 }}>
        <video 
            ref={videoRef} 
            style={{ width:'100%', display: stream ? 'block' : 'none' }} 
            playsInline 
            muted 
        />
        {!stream && !errorMsg && (
            <div style={{color: 'white', textAlign: 'center', paddingTop: 80}}>Loading Camera...</div>
        )}
      </div>

      <canvas ref={canvasRef} style={{ display:'none' }} />

      <div style={{ display:'flex', justifyContent: 'center', gap: 12 }}>
        {!stream ? (
             <button className="bp-add-btn" onClick={startCamera}>Retry Camera</button>
        ) : (
            <button 
                className="bp-add-btn" 
                style={{ width: '100%', maxWidth: 200, padding: 12, fontSize: 16 }} 
                onClick={capturePhoto}
            >
                Take Photo
            </button>
        )}
      </div>
    </div>
  );
};

export default ReceiptsCamera;