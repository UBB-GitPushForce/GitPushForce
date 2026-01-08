// src/components/Receipts.tsx
import React, { useState, useEffect } from 'react';
import '../App.css';
import ReceiptsView from './ReceiptsView';
import ReceiptsUpload from './ReceiptsUpload';
import ReceiptsCamera from './ReceiptsCamera';
import { useAuth } from '../hooks/useAuth';
import groupService, { Group } from '../services/group-service';
import receiptService, { ReceiptAnalysisResult } from '../services/receipt-service';
import apiClient from '../services/api-client'; 
import categoryService, { Category } from '../services/category-service'; 

const Receipts: React.FC<{ navigate?: (to: string) => void }> = ({ navigate }) => {
  const { user } = useAuth();
  const [subpage, setSubpage] = useState<'menu'|'chooseAdd'|'addOptions'|'view'|'upload'|'camera'|'review'>('chooseAdd');

  // AI Data
  const [analysisData, setAnalysisData] = useState<ReceiptAnalysisResult | null>(null);
  const [isProcessing, setIsProcessing] = useState(false);
  const [statusMessage, setStatusMessage] = useState(""); // For showing "Saving item 1 of 5..."

  // Data
  const [groups, setGroups] = useState<Group[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [selectedGroup, setSelectedGroup] = useState<number | null>(null);
  const [refreshKey, setRefreshKey] = useState<number>(0);

  // Load Groups and Categories on mount
  useEffect(() => {
    if (!user) return;
    const fetchData = async () => {
      try {
        const [groupsData, catsData] = await Promise.all([
            groupService.getUserGroups(user.id),
            categoryService.getCategories()
        ]);
        setGroups(groupsData);
        setCategories(catsData);
      } catch (err) {
        console.error('Failed to load initial data', err);
      }
    };
    fetchData();
  }, [user]);

  const triggerRefresh = () => setRefreshKey(prev => prev + 1);

  // --- DELETE HANDLER ---
  const handleDeleteItem = (indexToDelete: number) => {
    if (!analysisData) return;
    const updatedItems = analysisData.items.filter((_, idx) => idx !== indexToDelete);
    
    // Recalculate Total
    const newTotal = updatedItems.reduce((sum, item) => sum + item.price, 0);

    setAnalysisData({
        items: updatedItems,
        total: newTotal
    });
  };

  const handleFileProcess = async (file: File) => {
    if (!user) { alert("Login required"); return; }
    setIsProcessing(true);
    setStatusMessage("Analyzing receipt...");
    try {
      const result = await receiptService.processReceipt(file);
      setAnalysisData(result);
      setSubpage('review');
    } catch (error) {
      console.error(error);
      alert("AI Processing failed.");
    } finally {
      setIsProcessing(false);
      setStatusMessage("");
    }
  };

    // --- SAVE HANDLER (Matches Mobile Logic) ---
    // Replace your handleSaveConfirmed with this corrected version:
  const handleSaveConfirmed = async () => {
      if (!analysisData || analysisData.items.length === 0) return;

      setIsProcessing(true);
      setStatusMessage("Saving items...");

      // 1. Create a map for fast lookup
      const categoryMap = new Map<string, Category>();
      categories.forEach(c => categoryMap.set(c.title.toLowerCase(), c));

      let successCount = 0;

      try {
          for (let i = 0; i < analysisData.items.length; i++) {
              const item = analysisData.items[i];
              setStatusMessage(`Saving item ${i + 1} of ${analysisData.items.length}...`);

              // Normalize category name from AI (e.g., "Food " -> "food")
              const aiCategoryName = item.category.trim();
              const aiCategoryKey = aiCategoryName.toLowerCase();

              let targetCategory = categoryMap.get(aiCategoryKey);

              // 2. If Category doesn't exist, Create it
              if (!targetCategory) {
                  try {
                      // Call API to create category
                      const res: any = await categoryService.createCategory(aiCategoryName);

                      // CORRECTLY PARSE RESPONSE:
                      // The backend returns { success: true, data: { id: 123 } }
                      // Depending on your http-service, 'res' might be the whole object or just the data.
                      const newId = res?.data?.id || res?.id;

                      if (newId) {
                          // Construct the category object manually since we have the ID now
                          const newCat: Category = {
                              id: newId,
                              title: aiCategoryName,
                              user_id: user!.id,
                              keywords: item.keywords || []
                          };

                          targetCategory = newCat;
                          categoryMap.set(aiCategoryKey, newCat); // Update map for next items
                          setCategories(prev => [...prev, newCat]); // Update UI state
                      }
                  } catch (err) {
                      console.error(`Failed to create category '${aiCategoryName}'`, err);
                      // ONLY fallback if creation actually failed
                      if (categories.length > 0) targetCategory = categories[0];
                  }
              }

              // 3. Save Expense
              if (targetCategory) {
                  const payload = {
                      title: item.name,
                      amount: item.price,
                      category_id: targetCategory.id,
                      group_id: selectedGroup || undefined,
                      description: `AI Imported. Tags: ${item.keywords?.join(', ')}`
                  };
                  await apiClient.post('/expenses', payload);
                  successCount++;
              }
          }

          alert(`Successfully saved ${successCount} items!`);
          triggerRefresh();
          setSubpage('chooseAdd');
          setAnalysisData(null);
          setSelectedGroup(null);

      } catch (error: any) {
          console.error("Batch save failed:", error);
          alert("An error occurred while saving expenses.");
      } finally {
          setIsProcessing(false);
          setStatusMessage("");
      }
  };

  return (
    <div style={{ marginTop: 12 }}>
      {/* Header */}
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center' }}>
        <div className="bp-title">Receipts</div>
        <div style={{ color:'var(--muted-dark)' }}>History & uploads</div>
      </div>

      {/* Loading Overlay */}
      {isProcessing && (
        <div style={{
            position: 'fixed', top:0, left:0, right:0, bottom:0, 
            background: 'rgba(255,255,255,0.9)', zIndex: 999, 
            display:'flex', alignItems:'center', justifyContent:'center',
            flexDirection: 'column'
        }}>
            <div className="spinner"></div> 
            <div style={{marginTop: 15, fontWeight: 'bold'}}>{statusMessage || "Processing..."}</div>
        </div>
      )}

      {/* 1. CHOOSE ADD / MENU */}
      {subpage === 'chooseAdd' && (
        <div style={{ marginTop: 12, display: 'grid', gap: 12 }}>
            <div style={{ fontWeight:700 }}>Add receipt: choose type</div>
            <div style={{ display:'flex', gap:8 }}>
                <button className="bp-add-btn" onClick={() => { setSelectedGroup(null); setSubpage('addOptions'); }}>Single</button>
                <div style={{ display:'flex', gap:8, alignItems:'center' }}>
                    <select 
                        className="bp-select"
                        value={selectedGroup ?? ''} 
                        onChange={(e) => setSelectedGroup(e.target.value ? Number(e.target.value) : null)}
                    >
                        <option value="">Select group...</option>
                        {groups.map(g => <option key={g.id} value={g.id}>{g.name}</option>)}
                    </select>
                    <button className="bp-add-btn" onClick={() => {
                        if (!selectedGroup) { alert('Select a group first'); return; }
                        setSubpage('addOptions');
                    }}>Group</button>
                </div>
            </div>
        </div>
      )}

      {/* 2. ADD OPTIONS */}
      {subpage === 'addOptions' && (
         <div style={{ marginTop: 12, display: 'grid', gap: 12 }}>
            <div style={{ fontWeight:700 }}>How do you want to add the receipt?</div>
            {selectedGroup && <div style={{fontSize: 13, color: '#666'}}>Adding to group: {groups.find(g=>g.id===selectedGroup)?.name}</div>}
            
            <div style={{ display:'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px,1fr))', gap:12 }}>
                <div>
                    <button className="bp-add-btn" style={{ width:'100%' }} onClick={() => setSubpage('upload')}>Upload file</button>
                    <div style={{ marginTop:8, fontSize:13, color:'#666'}}>Upload PDF / PNG / JPG.</div>
                </div>
                <div>
                    <button className="bp-add-btn" style={{ width:'100%' }} onClick={() => setSubpage('camera')}>Use camera</button>
                    <div style={{ marginTop:8, fontSize:13, color:'#666'}}>Take a photo directly.</div>
                </div>
            </div>
            <button className="bp-add-btn" style={{width: 'fit-content'}} onClick={() => setSubpage('chooseAdd')}>Back</button>
         </div>
      )}

      {/* 3. UPLOAD & CAMERA */}
      {subpage === 'upload' && <ReceiptsUpload onFileSelected={handleFileProcess} />}
      {subpage === 'camera' && <ReceiptsCamera onPhotoTaken={handleFileProcess} />}

      {/* 4. REVIEW SCREEN */}
      {subpage === 'review' && analysisData && (
        <div style={{ marginTop:12 }}>
           <div className="bp-section-title">Review Receipt</div>
           <div style={{ marginBottom: 15, fontSize: 14, color: '#666' }}>
               Review items before saving.
           </div>
           
           <div style={{ background: '#f9f9f9', border: '1px solid #eee', padding: 15, borderRadius: 8 }}>
              {/* Items List */}
              <div style={{ display: 'grid', gap: 12 }}>
                  {analysisData.items.map((item, index) => (
                      <div key={index} style={{ 
                          display: 'grid', gridTemplateColumns: '1fr auto auto', 
                          alignItems: 'center', gap: 12,
                          borderBottom: '1px solid #eee', paddingBottom: 8 
                      }}>
                          {/* Info */}
                          <div>
                              <div style={{ fontWeight: 600 }}>{item.name}</div>
                              <div style={{ fontSize: 12, color: '#666' }}>
                                  <span style={{background:'#e6f7ff', color:'#1890ff', padding:'2px 6px', borderRadius:4, marginRight:5}}>
                                    {item.category}
                                  </span> 
                                  {item.quantity > 1 && `x${item.quantity}`}
                              </div>
                          </div>

                          {/* Price */}
                          <div style={{ fontWeight: 600 }}>
                              {item.price.toFixed(2)}
                          </div>

                          {/* DELETE BUTTON */}
                          <button 
                            onClick={() => handleDeleteItem(index)}
                            title="Remove item"
                            style={{ 
                                background: '#fff1f0', color: '#ff4d4f', 
                                border: '1px solid #ffccc7', borderRadius: 4, 
                                width: 28, height: 28, cursor: 'pointer',
                                display: 'flex', alignItems: 'center', justifyContent: 'center'
                            }}
                          >
                            ✕
                          </button>
                      </div>
                  ))}
              </div>

              {/* Total */}
              <div style={{ marginTop: 20, paddingTop: 15, borderTop: '2px solid #333', display: 'flex', justifyContent: 'space-between', fontSize: 18, fontWeight: 'bold' }}>
                  <span>Total</span>
                  <span>{analysisData.total.toFixed(2)}</span>
              </div>
           </div>

           {/* Actions */}
           <div style={{ display: 'flex', gap: 12, marginTop: 20 }}>
              <button 
                className="bp-add-btn" 
                style={{ flex: 1, backgroundColor: '#ff4d4f', color: 'white', borderColor: 'transparent', opacity: 0.9 }} 
                onClick={() => { setAnalysisData(null); setSubpage('chooseAdd'); }}
              >
                  Discard
              </button>
              <button 
                className="bp-add-btn" 
                style={{ flex: 1, backgroundColor: '#52c41a', color: 'white', borderColor: 'transparent' }} 
                onClick={handleSaveConfirmed}
              >
                  Confirm & Save
              </button>
           </div>
        </div>
      )}
    </div>
  );
};

export default Receipts;