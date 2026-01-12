// src/components/Categories.tsx
import React, { useEffect, useState } from 'react';
import '../App.css';
import categoryService, { Category } from '../services/category-service';
import { useAuth } from '../hooks/useAuth';

const Categories: React.FC = () => {
  const { user } = useAuth();
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // --- Create State ---
  const [newCategory, setNewCategory] = useState('');
  const [newKeywords, setNewKeywords] = useState('');

  // --- Edit State ---
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editTitle, setEditTitle] = useState('');
  const [editKeywords, setEditKeywords] = useState('');

  const fetchCategories = async () => {
    setLoading(true);
    try {
      const data = await categoryService.getCategories(user?.id);
      if (Array.isArray(data)) {
        setCategories(data);
      } else {
        setCategories([]);
      }
    } catch (err) {
      console.error("Categories Page: Error", err);
      setError('Failed to load categories');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, []);

  const parseKeywords = (str: string): string[] => {
    return str.split(',').map(s => s.trim()).filter(s => s.length > 0);
  };

  // --- Handlers ---

  const handleAdd = async (e: React.FormEvent) => {
    e.preventDefault();
    const titleToAdd = newCategory.trim();
    if (!titleToAdd) return;

    const keywordsArray = parseKeywords(newKeywords);

    try {
      const created = await categoryService.createCategory(titleToAdd, keywordsArray);
      
      const newCategoryObj: Category = {
        id: created.id || created.data?.id || Date.now(),
        title: created.title || titleToAdd,
        keywords: created.keywords || keywordsArray,
      } as Category;

      setCategories(prev => [...prev, newCategoryObj]);
      
      setNewCategory('');
      setNewKeywords('');
      setError(null);
    } catch (err: any) {
      console.error(err);
      setError('Failed to create category');
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this category?')) return;
    try {
      await categoryService.deleteCategory(id);
      setCategories(prev => prev.filter(c => c.id !== id));
      setError(null);
    } catch (err: any) {
      console.error(err);
      alert('Failed to delete category.');
    }
  };

  // --- Edit Logic ---

  const startEdit = (cat: Category) => {
    setEditingId(cat.id);
    setEditTitle(cat.title);
    setEditKeywords(cat.keywords ? cat.keywords.join(', ') : '');
    setError(null);
  };

  const cancelEdit = () => {
    setEditingId(null);
    setEditTitle('');
    setEditKeywords('');
  };

  const saveEdit = async (id: number) => {
    const titleToSave = editTitle.trim();
    if (!titleToSave) return;

    const keywordsToSave = parseKeywords(editKeywords);

    try {
      await categoryService.updateCategory(id, titleToSave, keywordsToSave);
      
      setCategories(prev => prev.map(c => 
        c.id === id ? { ...c, title: titleToSave, keywords: keywordsToSave } : c
      ));
      
      cancelEdit();
    } catch (err) {
      console.error(err);
      setError('Failed to update category');
    }
  };

  // --- Styles Helper ---
  // Shared props to ensure buttons match the 'Add' button size exactly
  const btnStyleBase = { fontSize: 13, padding: '6px 14px', border: 'none' };

  return (
    <div style={{ marginTop: 12 }}>
      <div className="bp-title">Manage Categories</div>
      <div style={{ color: 'var(--muted-dark)', marginBottom: 16 }}>
        Add categories and keywords to auto-tag your transactions.
      </div>

      {/* CREATE Form */}
      <form onSubmit={handleAdd} className="bp-box" style={{ display: 'flex', flexDirection: 'column', gap: 10, marginBottom: 20, padding: 16 }}>
        <div style={{ fontWeight: 600, fontSize: 14 }}>Add New</div>
        <div style={{ display: 'flex', gap: 10 }}>
          <input 
            placeholder="Name (e.g. Gym)" 
            value={newCategory} 
            onChange={e => setNewCategory(e.target.value)}
            style={{ flex: 1, padding: 10, borderRadius: 8, border: '1px solid #e4e4ee' }}
          />
          <input 
            placeholder="Keywords (e.g. fitness, trainer)" 
            value={newKeywords} 
            onChange={e => setNewKeywords(e.target.value)}
            style={{ flex: 2, padding: 10, borderRadius: 8, border: '1px solid #e4e4ee' }}
          />
          <button type="submit" className="bp-add-btn" disabled={!newCategory.trim()}>
            Add
          </button>
        </div>
      </form>

      {/* Global Error */}
      {error && <div style={{ color: '#ef4444', marginBottom: 10 }}>{error}</div>}

      {/* List */}
      <div style={{ display: 'grid', gap: 10 }}>
        {loading && <div style={{ color: 'var(--muted-dark)' }}>Loading...</div>}
        
        {!loading && categories.length > 0 ? (
          categories.map((cat) => (
            <div key={cat.id || Math.random()} className="bp-box" style={{ padding: '12px 16px', minHeight: 60 }}>
              
              {editingId === cat.id ? (
                /* --- EDIT MODE --- */
                <div style={{ display: 'flex', gap: 8, width: '100%', alignItems: 'center' }}>
                  <input 
                    value={editTitle}
                    onChange={e => setEditTitle(e.target.value)}
                    placeholder="Name"
                    autoFocus
                    style={{ flex: 1, padding: '8px 10px', borderRadius: 8, border: '1px solid #6366f1' }}
                  />
                  <input 
                    value={editKeywords}
                    onChange={e => setEditKeywords(e.target.value)}
                    placeholder="Keywords (comma separated)"
                    style={{ flex: 2, padding: '8px 10px', borderRadius: 8, border: '1px solid #6366f1' }}
                  />
                  
                  {/* Save Button (Green-ish) */}
                  <button 
                    onClick={() => saveEdit(cat.id)}
                    className="bp-add-btn"
                    style={{ 
                      ...btnStyleBase, 
                      background: 'linear-gradient(135deg, #10b981, #059669)' // Green gradient
                    }}
                  >
                    Save
                  </button>

                  {/* Cancel Button (Gray) */}
                  <button 
                    onClick={cancelEdit}
                    className="bp-add-btn"
                    style={{ 
                      ...btnStyleBase,
                      background: 'linear-gradient(135deg, #94a3b8, #64748b)' // Slate gradient
                    }}
                  >
                    Cancel
                  </button>
                </div>
              ) : (
                /* --- VIEW MODE --- */
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
                    <div style={{ fontWeight: 600, fontSize: 16 }}>{cat.title || 'Unnamed'}</div>
                    
                    {/* Keywords Tags */}
                    {cat.keywords && cat.keywords.length > 0 && (
                      <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap' }}>
                        {cat.keywords.map((k, idx) => (
                          <span key={idx} style={{ 
                            fontSize: 11, 
                            backgroundColor: '#f1f5f9', 
                            color: '#475569', 
                            padding: '2px 8px', 
                            borderRadius: 12, // Pill shape
                            border: '1px solid #e2e8f0',
                            fontWeight: 500
                          }}>
                            {k}
                          </span>
                        ))}
                      </div>
                    )}
                  </div>

                  <div style={{ display: 'flex', gap: 8 }}>
                    {/* Edit Button (Blue) */}
                    <button 
                      onClick={() => startEdit(cat)}
                      className="bp-add-btn"
                      style={{ 
                        ...btnStyleBase,
                        background: 'linear-gradient(135deg, #3b82f6, #2563eb)' // Blue gradient
                      }}
                    >
                      Edit
                    </button>

                    {/* Delete Button (Red) */}
                    <button 
                      onClick={() => handleDelete(cat.id)}
                      className="bp-add-btn"
                      style={{ 
                        ...btnStyleBase,
                        background: 'linear-gradient(135deg, #ef4444, #dc2626)' // Red gradient
                      }}
                    >
                      Delete
                    </button>
                  </div>
                </div>
              )}
            </div>
          ))
        ) : (
          !loading && (
            <div style={{ textAlign: 'center', padding: 20, color: 'var(--muted-dark)' }}>
              No categories found. Add one above!
            </div>
          )
        )}
      </div>
    </div>
  );
};

export default Categories;