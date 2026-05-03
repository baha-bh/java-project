import React from 'react';
import { LogOut, LayoutDashboard } from 'lucide-react';
import { supabase } from '../lib/supabase';
import { useNavigate } from 'react-router-dom';

export const Navbar = () => {
  const navigate = useNavigate();

  const handleLogout = async () => {
    await supabase.auth.signOut();
    navigate('/login');
  };

  return (
    <nav className="glass-panel" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1rem 2rem', margin: '1rem', borderRadius: 'var(--radius-lg)' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '2rem' }}>
        <div 
          onClick={() => navigate('/dashboard')}
          style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 'bold', fontSize: '1.25rem', color: 'var(--primary-color)', cursor: 'pointer' }}
        >
          <LayoutDashboard size={24} />
          Normocontrol
        </div>
        
        <div style={{ display: 'flex', gap: '1rem' }}>
          <button 
            className="btn btn-outline" 
            style={{ border: 'none', color: 'var(--text-color)' }}
            onClick={() => navigate('/dashboard')}
          >
            Дашборд
          </button>
          <button 
            className="btn btn-outline" 
            style={{ border: 'none', color: 'var(--text-color)' }}
            onClick={() => navigate('/rules')}
          >
            Правила
          </button>
        </div>
      </div>
      <div>
        <button className="btn btn-outline" onClick={handleLogout}>
          <LogOut size={18} />
          Выйти
        </button>
      </div>
    </nav>
  );
};
