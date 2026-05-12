import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { auth } from './services/auth';

export default function App() {
  const navigate = useNavigate();
  const user = auth.getUser();

  const handleLogout = () => {
    auth.clearSession();
    navigate('/login', { replace: true });
  };

  return (
    <div className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">Study Coach AI</p>
          <h1>資格・技術学習の管理</h1>
          <p className="topbar-copy">今日やること、今週の学習量、苦手分野をまとめて確認できます。</p>
        </div>
        <div className="topbar-actions">
          <nav className="nav">
            <NavLink to="/dashboard">ダッシュボード</NavLink>
            <NavLink to="/analytics">分析</NavLink>
            <NavLink to="/ai-advice">AIアドバイス</NavLink>
            <NavLink to="/targets">学習対象</NavLink>
            <NavLink to="/tasks">タスク</NavLink>
            <NavLink to="/logs">学習ログ</NavLink>
          </nav>
          <div className="user-menu">
            <span>{user?.username ?? 'ログイン中'}</span>
            <button className="secondary" onClick={handleLogout} type="button">
              ログアウト
            </button>
          </div>
        </div>
      </header>
      <main>
        <Outlet />
      </main>
    </div>
  );
}
