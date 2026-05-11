import { useEffect, useMemo, useState } from 'react';
import EmptyState from '../components/EmptyState';
import { api } from '../services/api';
import type { Dashboard, StudyTask } from '../types';

function formatMinutes(minutes: number) {
  if (minutes < 60) {
    return `${minutes}分`;
  }
  const hours = Math.floor(minutes / 60);
  const rest = minutes % 60;
  return rest === 0 ? `${hours}時間` : `${hours}時間${rest}分`;
}

function TaskMiniList({ tasks }: { tasks: StudyTask[] }) {
  if (tasks.length === 0) {
    return <EmptyState title="該当タスクなし" message="期限つきのタスクを登録するとここに表示されます。" />;
  }

  return (
    <div className="mini-list">
      {tasks.map((task) => (
        <article className="mini-item" key={task.id}>
          <div>
            <h3>{task.title}</h3>
            <p className="muted">
              {task.targetName}
              {task.fieldName ? ` / ${task.fieldName}` : ''}
            </p>
          </div>
          <span className={task.completed ? 'status done' : 'status'}>{task.completed ? '完了' : '未完了'}</span>
        </article>
      ))}
    </div>
  );
}

export default function DashboardPage() {
  const [dashboard, setDashboard] = useState<Dashboard | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    void loadDashboard();
  }, []);

  async function loadDashboard() {
    setLoading(true);
    setError('');
    try {
      setDashboard(await api.getDashboard());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'ダッシュボードの取得に失敗しました');
    } finally {
      setLoading(false);
    }
  }

  const completionRate = useMemo(() => {
    if (!dashboard || dashboard.taskCount === 0) {
      return 0;
    }
    return Math.round((dashboard.completedTaskCount / dashboard.taskCount) * 100);
  }, [dashboard]);

  if (loading) {
    return <p className="muted">読み込み中...</p>;
  }

  if (error) {
    return <p className="error">{error}</p>;
  }

  if (!dashboard) {
    return <EmptyState title="データがありません" message="学習対象とタスクを登録して始めましょう。" />;
  }

  return (
    <section className="dashboard">
      <div className="stats-grid">
        <article className="stat-card">
          <span>学習対象</span>
          <strong>{dashboard.targetCount}</strong>
          <p>資格・技術テーマ</p>
        </article>
        <article className="stat-card">
          <span>未完了タスク</span>
          <strong>{dashboard.openTaskCount}</strong>
          <p>今動いている学習</p>
        </article>
        <article className="stat-card">
          <span>今週の予定</span>
          <strong>{formatMinutes(dashboard.plannedMinutesThisWeek)}</strong>
          <p>期限ベースの合計</p>
        </article>
        <article className="stat-card attention">
          <span>期限超過</span>
          <strong>{dashboard.overdueTaskCount}</strong>
          <p>早めに片づけたいもの</p>
        </article>
      </div>

      <div className="dashboard-grid">
        <section className="panel">
          <div className="section-heading horizontal">
            <div>
              <p className="eyebrow">Today</p>
              <h2>今日やるタスク</h2>
            </div>
            <span className="pill">{dashboard.todayTasks.length}件</span>
          </div>
          <TaskMiniList tasks={dashboard.todayTasks} />
        </section>

        <section className="panel">
          <div className="section-heading horizontal">
            <div>
              <p className="eyebrow">Progress</p>
              <h2>全体の進捗</h2>
            </div>
            <span className="pill">{completionRate}%</span>
          </div>
          <div className="progress-track">
            <div className="progress-bar" style={{ width: `${completionRate}%` }} />
          </div>
          <dl className="meta-grid two-cols">
            <div>
              <dt>完了</dt>
              <dd>{dashboard.completedTaskCount}件</dd>
            </div>
            <div>
              <dt>合計</dt>
              <dd>{dashboard.taskCount}件</dd>
            </div>
          </dl>
          <p className="coach-note">
            まずは期限が近いタスクを小さく終わらせると、次に積む学習ログやAIアドバイス機能が効きやすくなります。
          </p>
        </section>
      </div>

      <section className="panel">
        <div className="section-heading horizontal">
          <div>
            <p className="eyebrow">Upcoming</p>
            <h2>直近のタスク</h2>
          </div>
          <span className="pill">最大5件</span>
        </div>
        <TaskMiniList tasks={dashboard.upcomingTasks} />
      </section>
    </section>
  );
}
