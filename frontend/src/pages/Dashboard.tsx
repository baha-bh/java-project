import React, { useEffect, useState } from 'react';
import { Navbar } from '../components/Navbar';
import { supabase } from '../lib/supabase';
import axios from 'axios';
import { FolderGit2, Plus, ArrowRight, Upload, Trash2, Code, FileDown } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

interface Project {
  id: string;
  name: string;
  repositoryUrl: string;
  createdAt: string;
}

export const Dashboard = () => {
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(true);
  
  // Create Project Modal State
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [newProjectName, setNewProjectName] = useState('');
  const [newProjectRepo, setNewProjectRepo] = useState('');
  const [newProjectBranch, setNewProjectBranch] = useState('main');
  const [isCreating, setIsCreating] = useState(false);
  
  // Test Analysis State
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [testReport, setTestReport] = useState<any | null>(null);
  const [isTesting, setIsTesting] = useState(false);
  
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProjects = async () => {
      try {
        const { data: { session } } = await supabase.auth.getSession();
        if (!session) {
          navigate('/login');
          return;
        }

        const response = await axios.get('/api/v1/projects', {
          headers: {
            Authorization: `Bearer ${session.access_token}`
          }
        });
        setProjects(Array.isArray(response.data) ? response.data : []);
      } catch (error) {
        console.error('Error fetching projects:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchProjects();
  }, [navigate]);

  const handleCreateProject = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsCreating(true);
    try {
      const { data: { session } } = await supabase.auth.getSession();
      if (!session) {
        navigate('/login');
        return;
      }

      const response = await axios.post('/api/v1/projects', {
        name: newProjectName,
        repositoryUrl: newProjectRepo,
        branch: newProjectBranch
      }, {
        headers: {
          Authorization: `Bearer ${session.access_token}`
        }
      });
      
      setProjects(prev => [response.data, ...prev]);
      setIsCreateModalOpen(false);
      setNewProjectName('');
      setNewProjectRepo('');
      setNewProjectBranch('main');
    } catch (error: any) {
      console.error('Error creating project:', error);
      const message = error.response?.data?.message || error.message || 'Произошла неизвестная ошибка';
      alert(`Ошибка при создании проекта: ${message}`);
    } finally {
      setIsCreating(false);
    }
  };

  const handleDeleteProject = async (e: React.MouseEvent, projectId: string) => {
    e.stopPropagation(); // Prevent navigation
    if (!window.confirm('Вы уверены, что хотите удалить этот проект? Все результаты проверок будут удалены.')) {
      return;
    }

    try {
      const { data: { session } } = await supabase.auth.getSession();
      await axios.delete(`/api/v1/projects/${projectId}`, {
        headers: {
          Authorization: `Bearer ${session?.access_token}`
        }
      });
      setProjects(prev => prev.filter(p => p.id !== projectId));
    } catch (error) {
      console.error('Error deleting project:', error);
      alert('Не удалось удалить проект');
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
      setTestResults([]);
    }
  };

  const handleTestAnalysis = async () => {
    if (!selectedFile) return;
    setIsTesting(true);
    setTestReport(null);
    try {
      const { data: { session } } = await supabase.auth.getSession();
      
      const formData = new FormData();
      formData.append('file', selectedFile);

      const response = await axios.post('/api/v1/analysis/test-file', formData, {
        headers: {
          Authorization: `Bearer ${session?.access_token}`,
          'Content-Type': 'multipart/form-data'
        }
      });
      setTestReport(response.data);
    } catch (error: any) {
      console.error('Error during test analysis:', error);
      alert(`Ошибка при анализе файла: ${error.response?.data?.message || error.message}`);
      setTestReport({ violations: [], details: [] });
    } finally {
      setIsTesting(false);
    }
  };

  const handleDownloadPdf = async () => {
    if (!selectedFile) return;
    try {
      const { data: { session } } = await supabase.auth.getSession();
      const formData = new FormData();
      formData.append('file', selectedFile);

      const response = await axios.post('/api/v1/analysis/test-file/download', formData, {
        responseType: 'blob',
        headers: {
          Authorization: `Bearer ${session?.access_token}`,
          'Content-Type': 'multipart/form-data'
        }
      });

      // Check if response is actually a JSON error hidden in a blob
      if (response.data.type === 'application/json') {
        const text = await response.data.text();
        const error = JSON.parse(text);
        alert(`Ошибка генерации PDF: ${error.message || 'Неизвестная ошибка'}`);
        return;
      }

      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `report-${selectedFile.name}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (error: any) {
      console.error('Error downloading PDF:', error);
      alert('Не удалось скачать PDF-отчет. Проверьте соединение с сервером.');
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Navbar />
      
      <main style={{ flex: 1, padding: '2rem', maxWidth: '1200px', margin: '0 auto', width: '100%' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
          <div>
            <h1 style={{ fontSize: '2rem', fontWeight: 'bold' }}>Ваши проекты</h1>
            <p style={{ color: 'var(--text-color-muted)' }}>Управление проектами и анализ кода</p>
          </div>
          <button className="btn btn-primary" onClick={() => setIsCreateModalOpen(true)}>
            <Plus size={18} />
            Добавить проект
          </button>
        </div>

        {loading ? (
          <div style={{ display: 'flex', justifyContent: 'center', padding: '3rem', color: 'var(--text-color-muted)' }}>
            Загрузка проектов...
          </div>
        ) : projects.length === 0 ? (
          <div className="glass-panel" style={{ padding: '3rem', textAlign: 'center' }}>
            <FolderGit2 size={48} style={{ color: 'var(--border-color)', margin: '0 auto 1rem' }} />
            <h3 style={{ fontSize: '1.25rem', marginBottom: '0.5rem' }}>Нет активных проектов</h3>
            <p style={{ color: 'var(--text-color-muted)', marginBottom: '1.5rem' }}>Добавьте свой первый проект для проведения нормоконтроля.</p>
            <button className="btn btn-primary" onClick={() => setIsCreateModalOpen(true)}>Создать проект</button>
          </div>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1.5rem' }}>
            {projects.map((project) => (
              <div key={project.id} className="glass-panel" style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', transition: 'transform 0.2s ease', cursor: 'pointer' }} 
                   onClick={() => navigate(`/projects/${project.id}`)}
                   onMouseEnter={(e) => e.currentTarget.style.transform = 'translateY(-4px)'}
                   onMouseLeave={(e) => e.currentTarget.style.transform = 'none'}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1rem' }}>
                  <div style={{ padding: '0.75rem', backgroundColor: 'var(--bg-color-tertiary)', borderRadius: 'var(--radius-md)' }}>
                    <FolderGit2 size={24} style={{ color: 'var(--primary-color)' }} />
                  </div>
                  <div>
                    <h3 style={{ fontWeight: '600', fontSize: '1.125rem' }}>{project.name}</h3>
                    <p style={{ fontSize: '0.75rem', color: 'var(--text-color-muted)' }}>{new Date(project.createdAt).toLocaleDateString()}</p>
                  </div>
                </div>
                <div style={{ color: 'var(--text-color-muted)', fontSize: '0.875rem', marginBottom: '1.5rem', flex: 1, wordBreak: 'break-all' }}>
                  {project.repositoryUrl}
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderTop: '1px solid var(--glass-border)', paddingTop: '1rem' }}>
                  <button 
                    className="btn btn-outline" 
                    onClick={(e) => handleDeleteProject(e, project.id)}
                    style={{ border: 'none', color: 'var(--danger-color)', padding: 0, display: 'flex', alignItems: 'center', gap: '0.25rem' }}
                  >
                    <Trash2 size={16} /> Удалить
                  </button>
                  <button className="btn btn-outline" style={{ border: 'none', color: 'var(--primary-color)', padding: 0 }}>
                    Анализ кода <ArrowRight size={16} />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Testing Sections */}
        <div style={{ marginTop: '4rem', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem', borderTop: '1px solid var(--glass-border)', paddingTop: '2rem' }}>
          
          {/* Code Analysis Card */}
          <div className="glass-panel" style={{ padding: '2rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1rem' }}>
              <Code size={24} style={{ color: 'var(--primary-color)' }} />
              <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold' }}>Проверка кода</h2>
            </div>
            <p style={{ color: 'var(--text-color-muted)', marginBottom: '1.5rem', fontSize: '0.875rem' }}>
              Мгновенный анализ Java-файлов на соответствие Clean Architecture и стандартам именования.
            </p>
            <div style={{ position: 'relative', marginBottom: '1.5rem' }}>
              <input 
                type="file" 
                accept=".java"
                className="input-field" 
                onChange={handleFileChange}
                style={{ opacity: 0, position: 'absolute', inset: 0, cursor: 'pointer', zIndex: 10 }}
              />
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.625rem 1rem', border: '2px dashed var(--glass-border)', borderRadius: 'var(--radius-md)', color: selectedFile?.name?.endsWith('.java') ? 'var(--text-color)' : 'var(--text-color-muted)' }}>
                <Upload size={18} />
                {selectedFile?.name?.endsWith('.java') ? selectedFile.name : 'Выбрать .java файл...'}
              </div>
            </div>
            <button 
              className="btn btn-primary" 
              style={{ width: '100%', justifyContent: 'center' }}
              onClick={handleTestAnalysis}
              disabled={isTesting || !selectedFile?.name?.endsWith('.java')}
            >
              {isTesting && selectedFile?.name?.endsWith('.java') ? 'Анализ...' : 'Проверить код'}
            </button>
          </div>

          {/* Document Normocontrol Card */}
          <div className="glass-panel" style={{ padding: '2rem', border: '1px solid rgba(99, 102, 241, 0.3)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1rem' }}>
              <Upload size={24} style={{ color: 'var(--secondary-color)' }} />
              <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold' }}>Нормоконтроль (Диплом)</h2>
              <div style={{ fontSize: '0.65rem', backgroundColor: 'var(--secondary-color)', color: 'white', padding: '0.125rem 0.4rem', borderRadius: '1rem' }}>NEW</div>
            </div>
            <p style={{ color: 'var(--text-color-muted)', marginBottom: '1.5rem', fontSize: '0.875rem' }}>
              Проверка оформления по ГОСТ: шрифты (Times New Roman, 14pt), отступы (1.25 см), интервалы.
            </p>
            <div style={{ position: 'relative', marginBottom: '1.5rem' }}>
              <input 
                type="file" 
                accept=".docx,.md"
                className="input-field" 
                onChange={handleFileChange}
                style={{ opacity: 0, position: 'absolute', inset: 0, cursor: 'pointer', zIndex: 10 }}
              />
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.625rem 1rem', border: '2px dashed var(--glass-border)', borderRadius: 'var(--radius-md)', color: (selectedFile?.name?.endsWith('.docx') || selectedFile?.name?.endsWith('.md')) ? 'var(--text-color)' : 'var(--text-color-muted)' }}>
                <Upload size={18} />
                {(selectedFile?.name?.endsWith('.docx') || selectedFile?.name?.endsWith('.md')) ? selectedFile.name : 'Выбрать .docx или .md файл...'}
              </div>
            </div>
            <button 
              className="btn btn-secondary" 
              style={{ width: '100%', justifyContent: 'center', backgroundColor: 'var(--secondary-color)', color: 'white' }}
              onClick={handleTestAnalysis}
              disabled={isTesting || (!selectedFile?.name?.endsWith('.docx') && !selectedFile?.name?.endsWith('.md'))}
            >
              {isTesting && selectedFile && !selectedFile.name.endsWith('.java') ? 'Проверка...' : 'Начать нормоконтроль'}
            </button>
          </div>
        </div>

        {/* Unified Results Section */}
        {testReport && (
          <div className="glass-panel" style={{ marginTop: '2rem', padding: '2rem', animation: 'fadeIn 0.5s ease-out' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem', paddingBottom: '1rem', borderBottom: '1px solid var(--glass-border)' }}>
              <div>
                <h3 style={{ fontSize: '1.25rem', fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <FolderGit2 size={20} className="text-primary" />
                  Отчет нормоконтроля
                </h3>
                <p style={{ fontSize: '0.875rem', color: 'var(--text-color-muted)' }}>Файл: <strong>{testReport.fileName}</strong></p>
              </div>
              <div style={{ textAlign: 'right' }}>
                <div style={{ 
                  fontSize: '2rem', 
                  fontWeight: 'bold', 
                  color: testReport.score >= 90 ? 'var(--success-color)' : testReport.score >= 70 ? 'var(--warning-color)' : 'var(--danger-color)' 
                }}>
                  {testReport.score}%
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-color-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Соответствие</div>
                <button 
                  onClick={handleDownloadPdf}
                  className="btn btn-outline"
                  style={{ marginTop: '0.5rem', padding: '0.4rem 0.75rem', fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.4rem', marginLeft: 'auto' }}
                >
                  <FileDown size={14} /> Скачать PDF
                </button>
              </div>
            </div>

            {/* Check Details (Table) - Only for Docs or if provided */}
            {testReport.details && testReport.details.length > 0 && (
              <div style={{ marginBottom: '2.5rem' }}>
                <h4 style={{ fontSize: '1rem', fontWeight: '600', marginBottom: '1rem', color: 'var(--text-color-muted)' }}>Критерии проверки (ГОСТ/Диплом)</h4>
                <div style={{ overflow: 'hidden', borderRadius: 'var(--radius-md)', border: '1px solid var(--glass-border)', backgroundColor: 'rgba(255,255,255,0.02)' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.875rem' }}>
                    <thead>
                      <tr style={{ backgroundColor: 'rgba(255,255,255,0.05)' }}>
                        <th style={{ textAlign: 'left', padding: '1rem' }}>Параметр</th>
                        <th style={{ textAlign: 'left', padding: '1rem' }}>Обнаружено</th>
                        <th style={{ textAlign: 'right', padding: '1rem' }}>Статус</th>
                      </tr>
                    </thead>
                    <tbody>
                      {testReport.details.map((d: any, i: number) => (
                        <tr key={i} style={{ borderTop: '1px solid var(--glass-border)' }}>
                          <td style={{ padding: '1rem', fontWeight: '500' }}>{d.criteria}</td>
                          <td style={{ padding: '1rem', color: 'var(--text-color-muted)', fontStyle: 'italic' }}>{d.foundValue}</td>
                          <td style={{ padding: '1rem', textAlign: 'right' }}>
                            <span style={{ 
                              padding: '0.25rem 0.625rem', 
                              borderRadius: '1rem', 
                              fontSize: '0.75rem',
                              fontWeight: '600',
                              backgroundColor: d.passed ? 'rgba(16, 185, 129, 0.1)' : 'rgba(239, 68, 68, 0.1)',
                              color: d.passed ? 'var(--success-color)' : 'var(--danger-color)'
                            }}>
                              {d.passed ? '✓ ПРОЙДЕНО' : '✗ ЕСТЬ ЗАМЕЧАНИЯ'}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            {/* Violations List */}
            <div>
              <h4 style={{ fontSize: '1rem', fontWeight: '600', marginBottom: '1rem', color: 'var(--text-color-muted)' }}>
                Детальный список замечаний {testReport.violations?.length > 0 ? `(${testReport.violations.length})` : ''}
              </h4>
              
              {testReport.violations && testReport.violations.length > 0 ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                  {testReport.violations.map((v: any, i: number) => (
                    <div key={i} className="glass-panel" style={{ padding: '1rem', borderLeft: '4px solid var(--warning-color)', backgroundColor: 'rgba(245, 158, 11, 0.03)' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                        <span style={{ fontWeight: '600', color: 'var(--warning-color)', fontSize: '0.875rem' }}>
                          {v.rule?.name || 'Нарушение регламента'}
                        </span>
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-color-muted)' }}>
                          Абзац/Строка: {v.lineNumber}
                        </span>
                      </div>
                      <div style={{ fontSize: '0.875rem', color: 'var(--text-color)', lineHeight: '1.5' }}>
                        {v.message}
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div style={{ textAlign: 'center', padding: '3rem', backgroundColor: 'rgba(16, 185, 129, 0.02)', borderRadius: 'var(--radius-md)', border: '1px dashed var(--success-color)' }}>
                  <div style={{ fontSize: '2rem', marginBottom: '1rem' }}>🏆</div>
                  <h3 style={{ fontWeight: '600', color: 'var(--success-color)', marginBottom: '0.5rem' }}>Идеальное соответствие!</h3>
                  <p style={{ fontSize: '0.875rem', color: 'var(--text-color-muted)' }}>
                    Нарушений не выявлено. Документ полностью соответствует академическим стандартам.
                  </p>
                </div>
              )}
            </div>
          </div>
        )}
      </main>

      {/* Create Project Modal */}
      {isCreateModalOpen && (
        <div style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(0, 0, 0, 0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 50, backdropFilter: 'blur(4px)' }}>
          <div className="glass-panel" style={{ width: '100%', maxWidth: '500px', padding: '2rem', position: 'relative' }}>
            <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold', marginBottom: '1.5rem' }}>Новый проект</h2>
            
            <form onSubmit={handleCreateProject} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', color: 'var(--text-color-muted)' }}>Название проекта</label>
                <input 
                  type="text" 
                  className="input-field" 
                  value={newProjectName}
                  onChange={(e) => setNewProjectName(e.target.value)}
                  placeholder="Например: normocontrol-backend"
                  required 
                />
              </div>
              
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', color: 'var(--text-color-muted)' }}>URL репозитория (Git)</label>
                <input 
                  type="text" 
                  className="input-field" 
                  value={newProjectRepo}
                  onChange={(e) => setNewProjectRepo(e.target.value)}
                  placeholder="https://github.com/user/repo.git"
                  required 
                />
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', color: 'var(--text-color-muted)' }}>Ветка по умолчанию</label>
                <input 
                  type="text" 
                  className="input-field" 
                  value={newProjectBranch}
                  onChange={(e) => setNewProjectBranch(e.target.value)}
                  placeholder="main"
                />
              </div>

              <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem', justifyContent: 'flex-end' }}>
                <button type="button" className="btn btn-outline" onClick={() => setIsCreateModalOpen(false)} disabled={isCreating}>
                  Отмена
                </button>
                <button type="submit" className="btn btn-primary" disabled={isCreating}>
                  {isCreating ? 'Создание...' : 'Создать'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
