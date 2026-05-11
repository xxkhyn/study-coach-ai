import { NavLink, Outlet } from 'react-router-dom';

export default function App() {
  return (
    <div className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">MVP Phase 1</p>
          <h1>Study Coach AI</h1>
          <p className="topbar-copy">資格と技術学習を、今日やることに落とし込むための小さな管理アプリ。</p>
        </div>
        <nav className="nav">
          <NavLink to="/">ダッシュボード</NavLink>
          <NavLink to="/targets">学習対象</NavLink>
          <NavLink to="/tasks">タスク</NavLink>
        </nav>
      </header>
      <main>
        <Outlet />
      </main>
    </div>
  );
}
