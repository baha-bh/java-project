import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Navbar } from '../components/Navbar';
import { supabase } from '../lib/supabase';
import axios from 'axios';
import { 
  ArrowLeft, 
  AlertTriangle, 
  FileCode, 
  CheckCircle2, 
  Hash,
  MessageSquare,
  ShieldCheck
} from 'lucide-react';

interface Rule {
  name: string;
  severity: string;
}

interface Violation {
  id: string;
  checkResultId: string;
  rule: Rule;
  filePath: string;
  lineNumber: number;
  message: string;
}

interface CheckResult {
  id: string;
  projectId: string;
  status: string;
  score: number | null;
  startedAt: string;
  completedAt: string | null;
}

export const CheckDetails = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [check, setCheck] = useState<CheckResult | null>(null);
  const [violations, setViolations] = useState<Violation[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchCheckData = async () => {
      try {
        const { data: { session } } = await supabase.auth.getSession();
        if (!session) return;

        const headers = { Authorization: `Bearer ${session.access_token}` };
        
        const checkRes = await axios.get(`/api/v1/checks/${id}`, { headers });
        setCheck(checkRes.data);

        const violationsRes = await axios.get('/api/v1/violations', { headers });
        const filteredViolations = violationsRes.data.filter((v: any) => v.checkResultId === id);
        setViolations(filteredViolations);

      } catch (error) {
        console.error('Error fetching check data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchCheckData();
  }, [id]);

  if (loading) return <div className="glass-panel" style={{ margin: '2rem', padding: '2rem' }}>Загрузка результатов...</div>;
  if (!check) return <div className="glass-panel" style={{ margin: '2rem', padding: '2rem', color: 'var(--danger-color)' }}>Результат проверки не найден</div>;

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      <Navbar />
      
      <main style={{ flex: 1, padding: '2rem', maxWidth: '1200px', margin: '0 auto', width: '100%' }}>
        <button 
          onClick={() => navigate(-1)}
          style={{ background: 'none', border: 'none', color: 'var(--text-color-muted)', display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer', marginBottom: '1.5rem' }}
        >
          <ArrowLeft size={16} /> К деталям проекта
        </button>

        <div className="glass-panel" style={{ padding: '2rem', marginBottom: '2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <h1 style={{ fontSize: '1.5rem', fontWeight: 'bold', marginBottom: '0.5rem' }}>Результаты анализа #{check.id.slice(0, 8)}</h1>
            <p style={{ color: 'var(--text-color-muted)', fontSize: '0.875rem' }}>
              Проверка завершена {new Date(check.completedAt || check.startedAt).toLocaleString()}
            </p>
          </div>
          <div style={{ textAlign: 'right' }}>
            <div style={{ fontSize: '2.5rem', fontWeight: 'bold', color: check.score && check.score > 80 ? 'var(--success-color)' : 'var(--warning-color)' }}>
              {check.score || 0}%
            </div>
            <div style={{ fontSize: '0.875rem', color: 'var(--text-color-muted)' }}>Итоговая оценка</div>
          </div>
        </div>

        <h2 style={{ fontSize: '1.25rem', fontWeight: '600', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <AlertTriangle size={20} style={{ color: 'var(--warning-color)' }} />
          Нарушения ({violations.length})
        </h2>

        {violations.length === 0 ? (
          <div className="glass-panel" style={{ padding: '3rem', textAlign: 'center', color: 'var(--success-color)' }}>
            <CheckCircle2 size={48} style={{ margin: '0 auto 1rem' }} />
            <h3 style={{ fontSize: '1.25rem' }}>Нарушений не найдено!</h3>
            <p style={{ color: 'var(--text-color-muted)' }}>Ваш код полностью соответствует правилам нормоконтроля.</p>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {violations.map((v) => (
              <div key={v.id} className="glass-panel" style={{ padding: '1.5rem', borderLeft: '4px solid var(--warning-color)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--primary-color)', fontWeight: '600' }}>
                    <ShieldCheck size={18} />
                    {v.rule.name}
                  </div>
                  <div style={{ fontSize: '0.75rem', backgroundColor: 'rgba(245, 158, 11, 0.1)', color: 'var(--warning-color)', padding: '0.25rem 0.5rem', borderRadius: '4px' }}>
                    {v.rule.severity || 'MEDIUM'}
                  </div>
                </div>
                
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-color)', fontSize: '0.875rem', backgroundColor: 'var(--bg-color-tertiary)', padding: '0.5rem', borderRadius: 'var(--radius-md)' }}>
                    <FileCode size={16} style={{ color: 'var(--text-color-muted)' }} />
                    <span style={{ fontWeight: '500' }}>{v.filePath}</span>
                    <span style={{ color: 'var(--text-color-muted)', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                      <Hash size={14} /> строка {v.lineNumber}
                    </span>
                  </div>
                  
                  <div style={{ display: 'flex', gap: '0.5rem', color: 'var(--text-color-muted)', fontSize: '0.875rem' }}>
                    <MessageSquare size={16} style={{ flexShrink: 0, marginTop: '2px' }} />
                    <p style={{ lineHeight: '1.4' }}>{v.message}</p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
};
