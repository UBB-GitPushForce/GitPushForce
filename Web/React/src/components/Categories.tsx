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
  const [showCreateForm, setShowCreateForm] = useState(false);

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
      setShowCreateForm(false);
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

  return (
    <>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 12 }}>
        <div className="bp-title">Categories</div>
      </div>

      {/* Global Error */}
      {error && (
        <div style={{ 
          marginTop: 12, 
          padding: '8px 12px', 
          backgroundColor: '#ffebee', 
          color: '#c62828', 
          borderRadius: 8,
          fontSize: 14
        }}>
          {error}
        </div>
      )}

      <div className="bp-section-title" style={{ marginTop: 12, fontSize: 15 }}>My Categories</div>
      <div style={{ color: 'var(--muted-dark)', marginBottom: 12, fontSize: 13 }}>
        Add categories and keywords to auto-tag your transactions.
      </div>

      {/* List */}
      <div style={{ marginTop: 10, display: 'flex', flexDirection: 'column', gap: 10 }}>
        {loading ? (
          <div style={{ textAlign: 'center', padding: 20, color: 'var(--muted-dark)' }}>
            Loading categories...
          </div>
        ) : categories.length === 0 ? (
          <div style={{ textAlign: 'center', padding: 20, color: 'var(--muted-dark)' }}>
            No categories yet. Create your first category!
          </div>
        ) : (
          categories.map((cat) => (
            <div 
              key={cat.id || Math.random()} 
              className="bp-group-card"
              style={{ cursor: 'default' }}
            >
              {editingId === cat.id ? (
                /* --- EDIT MODE --- */
                <div style={{ display: 'flex', flexDirection: 'column', gap: 12, width: '100%' }}>
                  <div>
                    <label style={{ display: 'block', marginBottom: 6, fontSize: 14, fontWeight: 600 }}>
                      Name
                    </label>
                    <input 
                      value={editTitle}
                      onChange={e => setEditTitle(e.target.value)}
                      placeholder="Category name"
                      autoFocus
                      style={{
                        width: '100%',
                        padding: '10px 12px',
                        borderRadius: 8,
                        border: '1px solid rgba(0,0,0,0.1)',
                        fontSize: 14,
                      }}
                    />
                  </div>
                  <div>
                    <label style={{ display: 'block', marginBottom: 6, fontSize: 14, fontWeight: 600 }}>
                      Keywords (comma separated)
                    </label>
                    <input 
                      value={editKeywords}
                      onChange={e => setEditKeywords(e.target.value)}
                      placeholder="e.g., fitness, gym, trainer"
                      style={{
                        width: '100%',
                        padding: '10px 12px',
                        borderRadius: 8,
                        border: '1px solid rgba(0,0,0,0.1)',
                        fontSize: 14,
                      }}
                    />
                  </div>
                  <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
                    <button 
                      onClick={cancelEdit}
                      style={{ 
                        fontSize: 14,
                        padding: '8px 16px',
                        border: '1px solid rgba(0,0,0,0.1)',
                        borderRadius: 8,
                        background: 'var(--card-bg)',
                        cursor: 'pointer',
                      }}
                    >
                      Cancel
                    </button>
                    <button 
                      onClick={() => saveEdit(cat.id)}
                      className="bp-add-btn"
                      style={{ fontSize: 14, padding: '8px 16px' }}
                    >
                      Save
                    </button>
                  </div>
                </div>
              ) : (
                /* --- VIEW MODE --- */
                <>
                  <div className="bp-group-thumb">{cat.title?.[0]?.toUpperCase() || 'C'}</div>
                  <div style={{ display: 'flex', flexDirection: 'column', flex: 1 }}>
                    <div style={{ fontWeight: 800 }}>{cat.title || 'Unnamed'}</div>
                    
                    {/* Keywords Tags */}
                    {cat.keywords && cat.keywords.length > 0 && (
                      <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', marginTop: 6 }}>
                        {cat.keywords.map((k, idx) => (
                          <span key={idx} style={{ 
                            fontSize: 11, 
                            backgroundColor: 'rgba(99, 102, 241, 0.1)', 
                            color: 'var(--primary)', 
                            padding: '2px 8px', 
                            borderRadius: 12,
                            fontWeight: 500
                          }}>
                            {k}
                          </span>
                        ))}
                      </div>
                    )}
                  </div>

                  <div style={{ display: 'flex', gap: 8 }}>
                    <button 
                      onClick={() => startEdit(cat)}
                      className="bp-add-btn"
                      style={{ padding: '6px 12px' }}
                    >
                      Edit
                    </button>
                    <button 
                      onClick={() => handleDelete(cat.id)}
                      className="bp-add-btn"
                      style={{ padding: '6px 12px' }}
                    >
                      Delete
                    </button>
                  </div>
                </>
              )}
            </div>
          ))
        )}
      </div>

      {/* Add Button */}
      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginTop: 12 }}>
        <button 
          className="bp-add-btn" 
          onClick={() => setShowCreateForm(!showCreateForm)}
          style={{ fontSize: 14, padding: '8px 16px' }}
        >
          {showCreateForm ? 'Cancel' : '+ Add Category'}
        </button>
      </div>

      {/* CREATE Form */}
      {showCreateForm && (
        <div style={{ 
          marginTop: 15, 
          padding: 20, 
          background: 'var(--card-bg)', 
          borderRadius: 12, 
          border: '1px solid rgba(0,0,0,0.08)' 
        }}>
          <div style={{ fontWeight: 700, fontSize: 16, marginBottom: 15 }}>Add New Category</div>
          
          <form onSubmit={handleAdd} style={{ display: 'grid', gap: 12 }}>
            <div>
              <label style={{ display: 'block', marginBottom: 6, fontSize: 14, fontWeight: 600 }}>
                Category Name
              </label>
              <input 
                type="text"
                placeholder="e.g., Gym" 
                value={newCategory} 
                onChange={e => setNewCategory(e.target.value)}
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
                Keywords (Optional)
              </label>
              <input 
                type="text"
                placeholder="e.g., fitness, trainer, membership" 
                value={newKeywords} 
                onChange={e => setNewKeywords(e.target.value)}
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  borderRadius: 8,
                  border: '1px solid rgba(0,0,0,0.1)',
                  fontSize: 14,
                }}
              />
              <div style={{ fontSize: 12, color: 'var(--muted-dark)', marginTop: 4 }}>
                Separate keywords with commas
              </div>
            </div>

            <button
              type="submit"
              className="bp-add-btn"
              style={{ marginTop: 8 }}
              disabled={!newCategory.trim()}
            >
              Add Category
            </button>
          </form>
        </div>
      )}
    </>
  );
};

export default Categories;