import React, { useEffect, useState } from 'react';
import { Navbar } from '../components/Navbar';
import { supabase } from '../lib/supabase';
import axios from 'axios';
import { FolderGit2, Plus, ArrowRight } from 'lucide-react';
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
        setProjects(response.data);
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
      
      setProjects([response.data, ...projects]);
      setIsCreateModalOpen(false);
      setNewProjectName('');
      setNewProjectRepo('');
      setNewProjectBranch('main');
    } catch (error) {
      console.error('Error creating project:', error);
      alert('Ошибка при создании проекта');
    } finally {
      setIsCreating(false);
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
                <div style={{ display: 'flex', justifyContent: 'flex-end', borderTop: '1px solid var(--glass-border)', paddingTop: '1rem' }}>
                  <button className="btn btn-outline" style={{ border: 'none', color: 'var(--primary-color)', padding: 0 }}>
                    Анализ кода <ArrowRight size={16} />
                  </button>
                </div>
              </div>
            ))}
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
                  type="url" 
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
