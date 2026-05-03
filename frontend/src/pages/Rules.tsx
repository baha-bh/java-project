import { useEffect, useState } from 'react';
import { Navbar } from '../components/Navbar';
import { supabase } from '../lib/supabase';
import axios from 'axios';
import { 
  ShieldCheck, 
  Plus, 
  Trash2, 
  AlertCircle,
  ToggleLeft,
  ToggleRight
} from 'lucide-react';

interface Rule {
  id: string;
  name: string;
  description: string;
  isActive: boolean;
  category: string;
}

export const Rules = () => {
  const [rules, setRules] = useState<Rule[]>([]);
  const [loading, setLoading] = useState(true);
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [newRuleName, setNewRuleName] = useState('');
  const [newRuleDesc, setNewRuleDesc] = useState('');
  const [newRuleCat, setNewRuleCat] = useState('CODE_STYLE');
  const [isSaving, setIsSaving] = useState(false);

  const fetchRules = async () => {
    try {
      const { data: { session } } = await supabase.auth.getSession();
      if (!session) return;

      const response = await axios.get('/api/v1/rules', {
        headers: { Authorization: `Bearer ${session.access_token}` }
      });
      setRules(response.data);
    } catch (error) {
      console.error('Error fetching rules:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRules();
  }, []);

  const handleAddRule = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSaving(true);
    try {
      const { data: { session } } = await supabase.auth.getSession();
      if (!session) return;

      await axios.post('/api/v1/rules', {
        name: newRuleName,
        description: newRuleDesc,
        category: newRuleCat,
        isActive: true
      }, {
        headers: { Authorization: `Bearer ${session.access_token}` }
      });
      
      fetchRules();
      setIsAddModalOpen(false);
      setNewRuleName('');
      setNewRuleDesc('');
    } catch (error) {
      console.error('Error adding rule:', error);
      alert('Ошибка при добавлении правила');
    } finally {
      setIsSaving(false);
    }
  };

  const handleDeleteRule = async (id: string) => {
    if (!confirm('Вы уверены, что хотите удалить это правило?')) return;
    
    try {
      const { data: { session } } = await supabase.auth.getSession();
      if (!session) return;

      await axios.delete(`/api/v1/rules/${id}`, {
        headers: { Authorization: `Bearer ${session.access_token}` }
      });
      setRules(rules.filter(r => r.id !== id));
    } catch (error) {
      console.error('Error deleting rule:', error);
      alert('Ошибка при удалении правила');
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Navbar />
      
      <main style={{ flex: 1, padding: '2rem', maxWidth: '1200px', margin: '0 auto', width: '100%' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
          <div>
            <h1 style={{ fontSize: '2rem', fontWeight: 'bold' }}>Правила нормоконтроля</h1>
            <p style={{ color: 'var(--text-color-muted)' }}>Управление стандартами и проверками кода</p>
          </div>
          <button className="btn btn-primary" onClick={() => setIsAddModalOpen(true)}>
            <Plus size={18} />
            Добавить правило
          </button>
        </div>

        {loading ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-color-muted)' }}>Загрузка правил...</div>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '1.5rem' }}>
            {rules.map((rule) => (
              <div key={rule.id} className="glass-panel" style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1rem' }}>
                  <div style={{ padding: '0.5rem', backgroundColor: 'var(--bg-color-tertiary)', borderRadius: 'var(--radius-md)', color: 'var(--primary-color)' }}>
                    <ShieldCheck size={20} />
                  </div>
                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <button 
                      style={{ background: 'none', border: 'none', color: rule.isActive ? 'var(--success-color)' : 'var(--text-color-muted)', cursor: 'pointer' }}
                      title={rule.isActive ? 'Активно' : 'Выключено'}
                    >
                      {rule.isActive ? <ToggleRight size={24} /> : <ToggleLeft size={24} />}
                    </button>
                    <button 
                      onClick={() => handleDeleteRule(rule.id)}
                      style={{ background: 'none', border: 'none', color: 'var(--danger-color)', cursor: 'pointer', opacity: 0.7 }}
                    >
                      <Trash2 size={18} />
                    </button>
                  </div>
                </div>
                
                <h3 style={{ fontWeight: '600', fontSize: '1.125rem', marginBottom: '0.5rem' }}>{rule.name}</h3>
                <div style={{ marginBottom: '1rem', fontSize: '0.75rem', color: 'var(--primary-color)', backgroundColor: 'rgba(99, 102, 241, 0.1)', padding: '0.25rem 0.5rem', borderRadius: '4px', alignSelf: 'flex-start' }}>
                  {rule.category}
                </div>
                <p style={{ fontSize: '0.875rem', color: 'var(--text-color-muted)', flex: 1, lineHeight: '1.5' }}>
                  {rule.description}
                </p>
              </div>
            ))}
          </div>
        )}

        {rules.length === 0 && !loading && (
          <div className="glass-panel" style={{ padding: '4rem', textAlign: 'center' }}>
            <AlertCircle size={48} style={{ color: 'var(--border-color)', margin: '0 auto 1rem' }} />
            <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>Правила не найдены</h3>
            <p style={{ color: 'var(--text-color-muted)' }}>Добавьте первое правило для начала работы.</p>
          </div>
        )}
      </main>

      {/* Add Rule Modal */}
      {isAddModalOpen && (
        <div style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(0, 0, 0, 0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 50, backdropFilter: 'blur(4px)' }}>
          <div className="glass-panel" style={{ width: '100%', maxWidth: '500px', padding: '2rem' }}>
            <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold', marginBottom: '1.5rem' }}>Новое правило</h2>
            
            <form onSubmit={handleAddRule} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', color: 'var(--text-color-muted)' }}>Название</label>
                <input 
                  type="text" 
                  className="input-field" 
                  value={newRuleName}
                  onChange={(e) => setNewRuleName(e.target.value)}
                  placeholder="Например: Clean Architecture"
                  required 
                />
              </div>
              
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', color: 'var(--text-color-muted)' }}>Категория</label>
                <select 
                  className="input-field" 
                  value={newRuleCat}
                  onChange={(e) => setNewRuleCat(e.target.value)}
                  style={{ width: '100%' }}
                >
                  <option value="CODE_STYLE">Стиль кода</option>
                  <option value="ARCHITECTURE">Архитектура</option>
                  <option value="SECURITY">Безопасность</option>
                  <option value="PERFORMANCE">Производительность</option>
                </select>
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', color: 'var(--text-color-muted)' }}>Описание</label>
                <textarea 
                  className="input-field" 
                  value={newRuleDesc}
                  onChange={(e) => setNewRuleDesc(e.target.value)}
                  placeholder="Подробное описание правила и как его соблюдать"
                  rows={4}
                  required 
                />
              </div>

              <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem', justifyContent: 'flex-end' }}>
                <button type="button" className="btn btn-outline" onClick={() => setIsAddModalOpen(false)}>
                  Отмена
                </button>
                <button type="submit" className="btn btn-primary" disabled={isSaving}>
                  {isSaving ? 'Сохранение...' : 'Добавить'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
