import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Navbar } from '../components/Navbar';
import { supabase } from '../lib/supabase';
import axios from 'axios';
import { 
  ArrowLeft, 
  Github, 
  GitBranch, 
  Calendar, 
  Play, 
  CheckCircle2, 
  Clock, 
  AlertCircle,
  ExternalLink,
  ChevronRight
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
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED';
  score: number | null;
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

  const fetchProjectData = async () => {
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

      // Fetch checks
      const checksRes = await axios.get('/api/v1/checks', { headers });
      const projectChecks = checksRes.data.filter((c: any) => c.projectId === id);
      setChecks(projectChecks.sort((a: any, b: any) => 
        new Date(b.startedAt).getTime() - new Date(a.startedAt).getTime()
      ));

    } catch (err) {
      console.error('Error fetching project data:', err);
      setError('Не удалось загрузить данные проекта');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProjectData();
  }, [id, navigate]);

  const handleAnalyze = async () => {
    setAnalyzing(true);
    try {
      const { data: { session } } = await supabase.auth.getSession();
      if (!session) return;

      await axios.post(`/api/v1/projects/${id}/analyze`, {}, {
        headers: { Authorization: `Bearer ${session.access_token}` }
      });
      
      // Refresh data
      await fetchProjectData();
    } catch (err) {
      console.error('Error starting analysis:', err);
      alert('Ошибка при запуске анализа');
    } finally {
      setAnalyzing(false);
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'COMPLETED': return <CheckCircle2 size={18} style={{ color: 'var(--success-color)' }} />;
      case 'FAILED': return <AlertCircle size={18} style={{ color: 'var(--danger-color)' }} />;
      case 'RUNNING': return <Play size={18} className="spin" style={{ color: 'var(--primary-color)' }} />;
      default: return <Clock size={18} style={{ color: 'var(--text-color-muted)' }} />;
    }
  };

  const getStatusLabel = (status: string) => {
    switch (status) {
      case 'COMPLETED': return 'Завершено';
      case 'FAILED': return 'Ошибка';
      case 'RUNNING': return 'Выполняется';
      case 'PENDING': return 'В очереди';
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
                  <Github size={16} />
                  <a href={project.repositoryUrl} target="_blank" rel="noopener noreferrer" style={{ color: 'inherit', textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                    {project.repositoryUrl.replace('https://github.com/', '')} <ExternalLink size={12} />
                  </a>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-color-muted)', fontSize: '0.875rem' }}>
                  <GitBranch size={16} />
                  {project.branch || 'main'}
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-color-muted)', fontSize: '0.875rem' }}>
                  <Calendar size={16} />
                  Создан {new Date(project.createdAt).toLocaleDateString()}
                </div>
              </div>
            </div>
            <button 
              className="btn btn-primary" 
              style={{ padding: '0.75rem 1.5rem' }}
              onClick={handleAnalyze}
              disabled={analyzing}
            >
              {analyzing ? (
                <>
                  <Clock size={18} className="spin" /> Анализ...
                </>
              ) : (
                <>
                  <Play size={18} /> Запустить анализ
                </>
              )}
            </button>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '2rem' }}>
          {/* Main Content: History of Checks */}
          <div>
            <h2 style={{ fontSize: '1.25rem', fontWeight: '600', marginBottom: '1rem' }}>История проверок</h2>
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
                            <span style={{ fontSize: '0.875rem' }}>{getStatusLabel(check.status)}</span>
                          </div>
                        </td>
                        <td style={{ padding: '1rem', fontSize: '0.875rem' }}>
                          {new Date(check.startedAt).toLocaleString()}
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
                            style={{ background: 'none', border: 'none', color: 'var(--primary-color)', cursor: 'pointer' }}
                          >
                            Детали <ChevronRight size={16} />
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>

          {/* Sidebar: Statistics / Summary */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
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
                      ? Math.round(checks.reduce((acc, c) => acc + (c.score || 0), 0) / checks.length) + '%' 
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
    </div>
  );
};
