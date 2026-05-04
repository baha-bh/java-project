import { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Navbar } from '../components/Navbar';
import { supabase } from '../lib/supabase';
import axios from 'axios';
import { 
  ArrowLeft, 
  Code, 
  GitBranch, 
  Calendar, 
  Play, 
  CheckCircle2, 
  Clock, 
  AlertCircle,
  ExternalLink,
  ChevronRight,
  FolderOpen,
  Trash2,
  FileDown
} from 'lucide-react';

interface Project {
  id: string;
  name: string;
  repositoryUrl: string;
  branch: string;
  createdAt: string;
}

interface CheckResult {
  id: string;
  projectId: string;
  status: 'IN_PROGRESS' | 'PASSED' | 'FAILED';
  score: number | null;
  targetPath: string;
  startedAt: string;
  completedAt: string | null;
}

export const ProjectDetails = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [project, setProject] = useState<Project | null>(null);
  const [checks, setChecks] = useState<CheckResult[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [analyzing, setAnalyzing] = useState(false);
  const [targetPath, setTargetPath] = useState('src/main/java');
  const [selectedErrorMessage, setSelectedErrorMessage] = useState<string | null>(null);
  const pollingInterval = useRef<NodeJS.Timeout | null>(null);

  const fetchProjectData = async (silent = false) => {
    if (!silent) setLoading(true);
    try {
      const { data: { session } } = await supabase.auth.getSession();
      if (!session) {
        navigate('/login');
        return;
      }

      const headers = { Authorization: `Bearer ${session.access_token}` };
      
      // Fetch project details
      const projectRes = await axios.get(`/api/v1/projects/${id}`, { headers });
      setProject(projectRes.data);

      const checksRes = await axios.get('/api/v1/checks', { headers });
      const projectChecks = (checksRes.data || []).filter((c: any) => c.projectId === id);
      const sortedChecks = projectChecks.sort((a: any, b: any) => 
        new Date(b.startedAt || 0).getTime() - new Date(a.startedAt || 0).getTime()
      );
      
      setChecks(sortedChecks);

      // If any check is in progress, start/continue polling
      const hasInProgress = sortedChecks.some((c: CheckResult) => c.status === 'IN_PROGRESS');
      if (hasInProgress) {
        startPolling();
      } else {
        stopPolling();
      }

    } catch (err) {
      console.error('Error fetching project data:', err);
      if (!silent) setError('Не удалось загрузить данные проекта');
    } finally {
      if (!silent) setLoading(false);
    }
  };

  const startPolling = () => {
    if (pollingInterval.current) return;
    pollingInterval.current = setInterval(() => {
      fetchProjectData(true);
    }, 3000);
  };

  const stopPolling = () => {
    if (pollingInterval.current) {
      clearInterval(pollingInterval.current);
      pollingInterval.current = null;
    }
  };

  useEffect(() => {
    fetchProjectData();
    return () => stopPolling();
  }, [id, navigate]);

  const handleAnalyze = async () => {
    setAnalyzing(true);
    try {
      const { data: { session } } = await supabase.auth.getSession();
      if (!session) return;

      await axios.post(`/api/v1/projects/${id}/analyze`, {
        targetPath: targetPath
      }, {
        headers: { Authorization: `Bearer ${session.access_token}` }
      });
      
      // Refresh data and start polling
      await fetchProjectData(true);
    } catch (err) {
      console.error('Error starting analysis:', err);
      alert('Ошибка при запуске анализа');
    } finally {
      setAnalyzing(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Вы уверены, что хотите полностью удалить этот проект и всю историю его проверок?')) {
      return;
    }

    try {
      const { data: { session } } = await supabase.auth.getSession();
      if (!session) return;

      await axios.delete(`/api/v1/projects/${id}`, {
        headers: { Authorization: `Bearer ${session.access_token}` }
      });
      
      navigate('/dashboard');
    } catch (err) {
      console.error('Error deleting project:', err);
      alert('Ошибка при удалении проекта');
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'PASSED': return <CheckCircle2 size={18} style={{ color: 'var(--success-color)' }} />;
      case 'FAILED': return <AlertCircle size={18} style={{ color: 'var(--danger-color)' }} />;
      case 'IN_PROGRESS': return <Clock size={18} className="spin" style={{ color: 'var(--primary-color)' }} />;
      default: return <Clock size={18} style={{ color: 'var(--text-color-muted)' }} />;
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status) {
      case 'PASSED': return 'Завершено';
      case 'FAILED': return 'Ошибка';
      case 'IN_PROGRESS': return 'Выполняется';
      default: return status;
    }
  };

  if (loading) {
    return (
      <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
        <Navbar />
        <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
          <div className="glass-panel" style={{ padding: '2rem' }}>Загрузка...</div>
        </div>
      </div>
    );
  }

  if (error || !project) {
    return (
      <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
        <Navbar />
        <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', flexDirection: 'column', gap: '1rem' }}>
          <h2 style={{ color: 'var(--danger-color)' }}>{error || 'Проект не найден'}</h2>
          <button className="btn btn-primary" onClick={() => navigate('/dashboard')}>
            Вернуться на дашборд
          </button>
        </div>
      </div>
    );
  }

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Navbar />
      
      <main style={{ flex: 1, padding: '2rem', maxWidth: '1200px', margin: '0 auto', width: '100%' }}>
        {/* Header / Breadcrumbs */}
        <div style={{ marginBottom: '2rem' }}>
          <button 
            onClick={() => navigate('/dashboard')}
            style={{ 
              background: 'none', 
              border: 'none', 
              color: 'var(--text-color-muted)', 
              display: 'flex', 
              alignItems: 'center', 
              gap: '0.5rem', 
              cursor: 'pointer',
              marginBottom: '1rem',
              fontSize: '0.875rem'
            }}
          >
            <ArrowLeft size={16} /> Назад к проектам
          </button>
          
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <div>
              <h1 style={{ fontSize: '2.5rem', fontWeight: 'bold', marginBottom: '0.5rem' }}>{project.name}</h1>
              <div style={{ display: 'flex', gap: '1.5rem', flexWrap: 'wrap' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-color-muted)', fontSize: '0.875rem' }}>
                  <Code size={16} />
                  <a href={project.repositoryUrl} target="_blank" rel="noopener noreferrer" style={{ color: 'inherit', textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                    {(project.repositoryUrl ?? '').replace('https://github.com/', '')} <ExternalLink size={12} />
                  </a>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-color-muted)', fontSize: '0.875rem' }}>
                  <GitBranch size={16} />
                  {project.branch ?? 'main'}
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-color-muted)', fontSize: '0.875rem' }}>
                  <Calendar size={16} />
                  Создан {new Date(project.createdAt ?? Date.now()).toLocaleDateString()}
                </div>
              </div>
            </div>
            <div style={{ display: 'flex', gap: '1rem' }}>
              <button 
                className="btn btn-outline" 
                onClick={handleDelete}
                style={{ color: 'var(--danger-color)', border: '1px solid var(--danger-color)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}
              >
                <Trash2 size={18} /> Удалить проект
              </button>
            </div>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '2rem' }}>
          {/* Main Content: History of Checks */}
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
              <h2 style={{ fontSize: '1.25rem', fontWeight: '600' }}>История проверок</h2>
            </div>
            
            <div className="glass-panel" style={{ padding: 0, overflow: 'hidden' }}>
              {checks.length === 0 ? (
                <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-color-muted)' }}>
                  Проверок еще не проводилось
                </div>
              ) : (
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid var(--glass-border)', textAlign: 'left' }}>
                      <th style={{ padding: '1rem', color: 'var(--text-color-muted)', fontSize: '0.875rem' }}>Статус</th>
                      <th style={{ padding: '1rem', color: 'var(--text-color-muted)', fontSize: '0.875rem' }}>Дата</th>
                      <th style={{ padding: '1rem', color: 'var(--text-color-muted)', fontSize: '0.875rem' }}>Путь</th>
                      <th style={{ padding: '1rem', color: 'var(--text-color-muted)', fontSize: '0.875rem' }}>Оценка</th>
                      <th style={{ padding: '1rem' }}></th>
                    </tr>
                  </thead>
                  <tbody>
                    {checks.map((check) => (
                      <tr key={check.id} style={{ borderBottom: '1px solid var(--glass-border)', transition: 'background 0.2s' }} className="table-row-hover">
                        <td style={{ padding: '1rem' }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                            {getStatusIcon(check.status)}
                            <div style={{ display: 'flex', flexDirection: 'column' }}>
                              <span style={{ fontSize: '0.875rem' }}>{getStatusLabel(check.status)}</span>
                              {check.message && (
                                <span 
                                  onClick={() => setSelectedErrorMessage(check.message)}
                                  style={{ 
                                    fontSize: '0.75rem', 
                                    color: check.status === 'FAILED' ? 'var(--danger-color)' : 'var(--text-color-muted)', 
                                    maxWidth: '200px', 
                                    whiteSpace: 'nowrap', 
                                    overflow: 'hidden', 
                                    textOverflow: 'ellipsis',
                                    cursor: 'pointer',
                                    textDecoration: 'underline dotted'
                                  }} 
                                  title="Нажмите, чтобы увидеть полный текст"
                                >
                                  {check.message}
                                </span>
                              )}
                            </div>
                          </div>
                        </td>
                        <td style={{ padding: '1rem', fontSize: '0.875rem' }}>
                          {new Date(check.startedAt).toLocaleString()}
                        </td>
                        <td style={{ padding: '1rem', fontSize: '0.75rem', color: 'var(--text-color-muted)' }}>
                          <code>{check.targetPath || '/'}</code>
                        </td>
                        <td style={{ padding: '1rem' }}>
                          {check.score !== null ? (
                            <div style={{ 
                              display: 'inline-block', 
                              padding: '0.25rem 0.75rem', 
                              borderRadius: '1rem', 
                              backgroundColor: check.score > 80 ? 'rgba(16, 185, 129, 0.1)' : 'rgba(245, 158, 11, 0.1)',
                              color: check.score > 80 ? 'var(--success-color)' : 'var(--warning-color)',
                              fontWeight: '600',
                              fontSize: '0.875rem'
                            }}>
                              {check.score}%
                            </div>
                          ) : '-'}
                        </td>
                        <td style={{ padding: '1rem', textAlign: 'right' }}>
                          <button 
                            onClick={() => navigate(`/checks/${check.id}`)}
                            style={{ background: 'none', border: 'none', color: 'var(--primary-color)', cursor: 'pointer', marginRight: '1rem' }}
                            title="Детали"
                          >
                            <ChevronRight size={16} />
                          </button>
                          {check.status === 'PASSED' && (
                            <button 
                              onClick={(e) => handleDownloadPdf(e, check.id)}
                              style={{ background: 'none', border: 'none', color: 'var(--text-color-muted)', cursor: 'pointer' }}
                              title="Скачать PDF"
                              onMouseEnter={(e) => e.currentTarget.style.color = 'var(--primary-color)'}
                              onMouseLeave={(e) => e.currentTarget.style.color = 'var(--text-color-muted)'}
                            >
                              <FileDown size={16} />
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>

          {/* Sidebar: New Analysis & Stats */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            <div className="glass-panel" style={{ padding: '1.5rem' }}>
              <h3 style={{ fontSize: '1.125rem', fontWeight: '600', marginBottom: '1.25rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Play size={18} style={{ color: 'var(--primary-color)' }} /> Запуск анализа
              </h3>
              
              <div style={{ marginBottom: '1.5rem' }}>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', color: 'var(--text-color-muted)' }}>Путь для анализа</label>
                <div style={{ position: 'relative' }}>
                  <FolderOpen size={16} style={{ position: 'absolute', left: '0.75rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-color-muted)' }} />
                  <input 
                    type="text" 
                    className="input-field" 
                    style={{ paddingLeft: '2.5rem' }}
                    value={targetPath}
                    onChange={(e) => setTargetPath(e.target.value)}
                    placeholder="напр. src/main/java"
                  />
                </div>
                <p style={{ marginTop: '0.5rem', fontSize: '0.75rem', color: 'var(--text-color-muted)' }}>
                  Оставьте пустым для анализа всего проекта.
                </p>
              </div>

              <button 
                className="btn btn-primary" 
                style={{ width: '100%', justifyContent: 'center', padding: '0.75rem' }}
                onClick={handleAnalyze}
                disabled={analyzing || checks.some(c => c.status === 'IN_PROGRESS')}
              >
                {analyzing || checks.some(c => c.status === 'IN_PROGRESS') ? (
                  <>
                    <Clock size={18} className="spin" /> Выполняется...
                  </>
                ) : (
                  <>
                    <Play size={18} /> Начать проверку
                  </>
                )}
              </button>
            </div>

            <div className="glass-panel" style={{ padding: '1.5rem' }}>
              <h3 style={{ fontSize: '1rem', fontWeight: '600', marginBottom: '1rem' }}>Статистика</h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: 'var(--text-color-muted)', fontSize: '0.875rem' }}>Всего проверок</span>
                  <span style={{ fontWeight: '600' }}>{checks.length}</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span style={{ color: 'var(--text-color-muted)', fontSize: '0.875rem' }}>Средний балл</span>
                  <span style={{ fontWeight: '600' }}>
                    {checks.length > 0 
                      ? Math.round(checks.filter(c => c.score !== null).reduce((acc, c) => acc + (c.score || 0), 0) / (checks.filter(c => c.score !== null).length || 1)) + '%' 
                      : 'N/A'}
                  </span>
                </div>
              </div>
            </div>

            <div className="glass-panel" style={{ padding: '1.5rem', background: 'linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(168, 85, 247, 0.1))' }}>
              <h3 style={{ fontSize: '1rem', fontWeight: '600', marginBottom: '0.5rem' }}>О нормоконтроле</h3>
              <p style={{ fontSize: '0.875rem', color: 'var(--text-color-muted)', lineHeight: '1.5' }}>
                Система анализирует код на соответствие стандартам оформления, архитектуры и безопасности. 
                Регулярные проверки помогают поддерживать высокое качество кодовой базы.
              </p>
            </div>
          </div>
        </div>
      </main>

      {/* Error Details Modal */}
      {selectedErrorMessage && (
        <div style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(0, 0, 0, 0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100, backdropFilter: 'blur(4px)' }}>
          <div className="glass-panel" style={{ width: '100%', maxWidth: '600px', padding: '2rem', position: 'relative' }}>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '1rem', color: 'var(--danger-color)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <AlertCircle size={24} /> Детали ошибки
            </h2>
            <div style={{ 
              backgroundColor: 'var(--bg-color-tertiary)', 
              padding: '1.5rem', 
              borderRadius: 'var(--radius-md)', 
              fontSize: '0.875rem', 
              fontFamily: 'monospace', 
              whiteSpace: 'pre-wrap', 
              maxHeight: '400px', 
              overflowY: 'auto',
              marginBottom: '1.5rem',
              border: '1px solid var(--glass-border)'
            }}>
              {selectedErrorMessage}
            </div>
            <div style={{ display: 'flex', justifyContent: 'flex-end' }}>
              <button className="btn btn-primary" onClick={() => setSelectedErrorMessage(null)}>
                Закрыть
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
