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
  const [newRuleSev, setNewRuleSev] = useState('MEDIUM');
  const [isSaving, setIsSaving] = useState(false);
  const [generatedScript, setGeneratedScript] = useState('');

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
        severity: newRuleSev,
        scriptLogic: generatedScript,
        isActive: true
      }, {
        headers: { Authorization: `Bearer ${session?.access_token}` }
      });
      setNewRuleName('');
      setNewRuleDesc('');
      setGeneratedScript('');
      fetchRules();
      setIsAddModalOpen(false);
    } catch (error) {
      console.error('Error creating rule:', error);
      alert('Ошибка при добавлении правила');
    } finally {
      setIsSaving(false);
    }
  };

  const toggleAll = async (active: boolean) => {
    try {
      const { data: { session } } = await supabase.auth.getSession();
      await axios.post(`/api/v1/rules/toggle-all?active=${active}`, {}, {
        headers: { Authorization: `Bearer ${session?.access_token}` }
      });
      fetchRules();
    } catch (error) {
      console.error('Error toggling all rules:', error);
      alert('Ошибка при изменении статуса правил');
    }
  };

  const projectCategories = ['CODE_STYLE', 'ARCHITECTURE', 'NAMING', 'SECURITY'];
  const documentCategories = ['DOCUMENT_STRUCTURE', 'GOST_STANDARDS', 'FORMATTING'];

  const projectRules = rules.filter(r => projectCategories.includes(r.category) || !documentCategories.includes(r.category));
  const documentRules = rules.filter(r => documentCategories.includes(r.category));

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

  const handleToggleRule = async (id: string) => {
    try {
      const { data: { session } } = await supabase.auth.getSession();
      if (!session) return;

      const response = await axios.patch(`/api/v1/rules/${id}/toggle`, {}, {
        headers: { Authorization: `Bearer ${session.access_token}` }
      });
      
      setRules(rules.map(r => r.id === id ? response.data : r));
    } catch (error) {
      console.error('Error toggling rule:', error);
      alert('Ошибка при переключении правила');
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Navbar />
      
      <main style={{ flex: 1, padding: '2rem', maxWidth: '1200px', margin: '0 auto', width: '100%' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
          <h1 style={{ fontSize: '2rem', fontWeight: 'bold' }}>Правила валидации</h1>
          <div style={{ display: 'flex', gap: '1rem' }}>
            <button 
              onClick={() => toggleAll(true)}
              className="btn btn-secondary"
              style={{ fontSize: '0.875rem' }}
            >
              Включить все
            </button>
            <button 
              onClick={() => toggleAll(false)}
              className="btn btn-secondary"
              style={{ fontSize: '0.875rem' }}
            >
              Выключить все
            </button>
            <button 
              onClick={() => setIsAddModalOpen(true)}
              className="btn btn-primary"
            >
              + Новое правило
            </button>
          </div>
        </div>

        {loading ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-color-muted)' }}>Загрузка правил...</div>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
            {/* Project Rules Section */}
            <section>
              <h2 style={{ fontSize: '1.5rem', marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <span style={{ color: 'var(--primary-color)' }}>💻</span> Проекты
                <span style={{ fontSize: '0.875rem', color: 'var(--text-color-muted)', marginLeft: 'auto' }}>
                  {projectRules.length} правил
                </span>
              </h2>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                {projectRules.length > 0 ? (
                  projectRules.map(rule => (
                    <div key={rule.id} className="glass-panel" style={{ padding: '1.25rem' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                        <div style={{ flex: 1 }}>
                          <h3 style={{ fontSize: '1.125rem', fontWeight: '600', marginBottom: '0.25rem' }}>{rule.name}</h3>
                          <p style={{ color: 'var(--text-color-muted)', fontSize: '0.875rem', marginBottom: '0.75rem' }}>{rule.description}</p>
                          <div style={{ display: 'flex', gap: '0.5rem' }}>
                            <span style={{ fontSize: '0.75rem', color: 'var(--primary-color)', backgroundColor: 'rgba(99, 102, 241, 0.1)', padding: '0.125rem 0.375rem', borderRadius: '4px' }}>
                              {rule.category}
                            </span>
                          </div>
                        </div>
                        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', marginLeft: '1rem' }}>
                          <button 
                            onClick={() => handleToggleRule(rule.id)}
                            style={{ background: 'none', border: 'none', color: rule.isActive ? 'var(--success-color)' : 'var(--text-color-muted)', cursor: 'pointer' }}
                          >
                            {rule.isActive ? <ToggleRight size={24} /> : <ToggleLeft size={24} />}
                          </button>
                          <button 
                            onClick={() => handleDeleteRule(rule.id)}
                            style={{ background: 'none', border: 'none', color: 'var(--danger-color)', cursor: 'pointer' }}
                          >
                            <Trash2 size={18} />
                          </button>
                        </div>
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="glass-panel" style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-color-muted)' }}>Нет активных правил</div>
                )}
              </div>
            </section>

            {/* Document Rules Section */}
            <section>
              <h2 style={{ fontSize: '1.5rem', marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <span style={{ color: 'var(--primary-color)' }}>📄</span> Документы
                <span style={{ fontSize: '0.875rem', color: 'var(--text-color-muted)', marginLeft: 'auto' }}>
                  {documentRules.length} правил
                </span>
              </h2>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                {documentRules.length > 0 ? (
                  documentRules.map(rule => (
                    <div key={rule.id} className="glass-panel" style={{ padding: '1.25rem' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                        <div style={{ flex: 1 }}>
                          <h3 style={{ fontSize: '1.125rem', fontWeight: '600', marginBottom: '0.25rem' }}>{rule.name}</h3>
                          <p style={{ color: 'var(--text-color-muted)', fontSize: '0.875rem', marginBottom: '0.75rem' }}>{rule.description}</p>
                          <div style={{ display: 'flex', gap: '0.5rem' }}>
                            <span style={{ fontSize: '0.75rem', color: 'var(--primary-color)', backgroundColor: 'rgba(99, 102, 241, 0.1)', padding: '0.125rem 0.375rem', borderRadius: '4px' }}>
                              {rule.category}
                            </span>
                          </div>
                        </div>
                        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', marginLeft: '1rem' }}>
                          <button 
                            onClick={() => handleToggleRule(rule.id)}
                            style={{ background: 'none', border: 'none', color: rule.isActive ? 'var(--success-color)' : 'var(--text-color-muted)', cursor: 'pointer' }}
                          >
                            {rule.isActive ? <ToggleRight size={24} /> : <ToggleLeft size={24} />}
                          </button>
                          <button 
                            onClick={() => handleDeleteRule(rule.id)}
                            style={{ background: 'none', border: 'none', color: 'var(--danger-color)', cursor: 'pointer' }}
                          >
                            <Trash2 size={18} />
                          </button>
                        </div>
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="glass-panel" style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-color-muted)' }}>Нет активных правил</div>
                )}
              </div>
            </section>
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
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', color: 'var(--text-color-muted)' }}>Важность</label>
                <select 
                  className="input-field" 
                  value={newRuleSev}
                  onChange={(e) => setNewRuleSev(e.target.value)}
                  style={{ width: '100%' }}
                >
                  <option value="LOW">LOW</option>
                  <option value="MEDIUM">MEDIUM</option>
                  <option value="HIGH">HIGH</option>
                </select>
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', color: 'var(--text-color-muted)' }}>Описание (или промпт для AI)</label>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <textarea 
                    className="input-field" 
                    value={newRuleDesc}
                    onChange={(e) => setNewRuleDesc(e.target.value)}
                    placeholder="Например: Методы не должны быть длиннее 50 строк"
                    rows={3}
                    style={{ flex: 1 }}
                    required 
                  />
                  <button 
                    type="button" 
                    className="btn btn-outline" 
                    style={{ 
                      alignSelf: 'flex-start', 
                      padding: '0.75rem',
                      borderColor: generatedScript ? 'var(--success-color)' : 'var(--border-color)',
                      color: generatedScript ? 'var(--success-color)' : 'inherit'
                    }}
                    onClick={async () => {
                      if (!newRuleDesc) {
                        alert('Сначала введите описание правила для AI');
                        return;
                      }
                      setIsSaving(true);
                      try {
                        const { data: { session } } = await supabase.auth.getSession();
                        const response = await axios.post('/api/v1/rules/generate-ai', {
                          description: newRuleDesc
                        }, {
                          headers: { Authorization: `Bearer ${session?.access_token}` }
                        });
                        if (response.data.script) {
                          setGeneratedScript(response.data.script);
                        } else {
                          alert('AI вернул пустой результат');
                        }
                      } catch (error: any) {
                        console.error('AI Error:', error);
                        alert('Ошибка AI: ' + (error.response?.data?.message || error.message));
                      } finally {
                        setIsSaving(false);
                      }
                    }}
                    disabled={isSaving}
                  >
                    <ShieldCheck size={18} /> {generatedScript ? 'Обновить AI' : 'AI'}
                  </button>
                </div>
              </div>

              {generatedScript && (
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', color: 'var(--text-color-muted)' }}>Сгенерированный скрипт (Groovy)</label>
                  <pre style={{ 
                    backgroundColor: 'rgba(0,0,0,0.2)', 
                    padding: '1rem', 
                    borderRadius: '8px', 
                    fontSize: '0.75rem', 
                    overflowX: 'auto',
                    maxHeight: '200px',
                    border: '1px solid var(--border-color)'
                  }}>
                    {generatedScript}
                  </pre>
                </div>
              )}

              <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem', justifyContent: 'flex-end' }}>
                <button type="button" className="btn btn-outline" onClick={() => {
                  setIsAddModalOpen(false);
                  setGeneratedScript('');
                }}>
                  Отмена
                </button>
                <button type="submit" className="btn btn-primary" disabled={isSaving}>
                  {isSaving ? 'Обработка...' : 'Добавить'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
