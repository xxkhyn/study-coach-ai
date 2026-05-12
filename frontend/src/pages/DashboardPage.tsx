import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import EmptyState from '../components/EmptyState';
import { api } from '../services/api';
import type { Dashboard, QuestionLog, StudyLog, StudyTask } from '../types';

const minutesLabel = (minutes: number) => {
  const hours = Math.floor(minutes / 60);
  const rest = minutes % 60;
  if (hours === 0) return `${rest}分`;
  if (rest === 0) return `${hours}時間`;
  return `${hours}時間${rest}分`;
};

const dateLabel = (value?: string | null) => value ?? '未設定';
const percentLabel = (value: number) => `${Number(value).toFixed(1)}%`;
const targetLabel = (value?: string | null) => value ?? '学習対象未設定';
const fieldLabel = (value?: string | null) => (value && value.trim().length > 0 ? value : '分野未設定');

function TaskList({ tasks }: { tasks: StudyTask[] }) {
  if (tasks.length === 0) {
    return <EmptyState title="まだデータがありません" message="期限付きの未完了タスクがここに表示されます。" />;
  }

  return (
    <div className="mini-list">
      {tasks.map((task) => (
        <article className="mini-item" key={task.id}>
          <div>
            <h3>{task.title}</h3>
            <p className="muted">
              {targetLabel(task.studyTargetName)} / {fieldLabel(task.field)}
            </p>
          </div>
          <span className="pill">{dateLabel(task.dueDate)}</span>
        </article>
      ))}
    </div>
  );
}

function StudyLogList({ logs }: { logs: StudyLog[] }) {
  if (logs.length === 0) {
    return <EmptyState title="まだデータがありません" message="学習ログを登録すると直近の記録が表示されます。" />;
  }

  return (
    <div className="mini-list">
      {logs.map((log) => (
        <article className="mini-item" key={log.id}>
          <div>
            <h3>{targetLabel(log.studyTargetName)}</h3>
            <p className="muted">
              {fieldLabel(log.field)} / {minutesLabel(log.minutes)}
            </p>
          </div>
          <span className="pill">{log.studiedDate}</span>
        </article>
      ))}
    </div>
  );
}

function QuestionLogList({ logs }: { logs: QuestionLog[] }) {
  if (logs.length === 0) {
    return <EmptyState title="まだデータがありません" message="演習ログを登録すると直近の記録が表示されます。" />;
  }

  return (
    <div className="mini-list">
      {logs.map((log) => (
        <article className="mini-item" key={log.id}>
          <div>
            <h3>{targetLabel(log.studyTargetName)}</h3>
            <p className="muted">
              {fieldLabel(log.field)} / {log.correctCount}/{log.solvedCount}問
            </p>
          </div>
          <span className="pill">{percentLabel(log.accuracyRate)}</span>
        </article>
      ))}
    </div>
  );
}

export default function DashboardPage() {
  const [dashboard, setDashboard] = useState<Dashboard | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api
      .getDashboard()
      .then(setDashboard)
      .catch((err: Error) => setError(err.message))
      .finally(() => setIsLoading(false));
  }, []);

  const totalMinutes = dashboard?.weeklyStudySummary.totalMinutes ?? 0;
  const targetSummaries = dashboard?.weeklyStudySummary.targetSummaries ?? [];
  const weakFields = dashboard?.weakFields ?? [];
  const fieldAccuracies = dashboard?.fieldAccuracies ?? [];
  const maxTargetMinutes = useMemo(
    () => Math.max(1, ...targetSummaries.map((summary) => summary.totalMinutes)),
    [targetSummaries],
  );

  if (isLoading) {
    return <section className="panel">読み込み中...</section>;
  }

  if (error) {
    return <div className="error">{error}</div>;
  }

  if (!dashboard) {
    return <EmptyState title="まだデータがありません" message="ダッシュボードを表示できませんでした。" />;
  }

  return (
    <div className="dashboard">
      <section className="stats-grid">
        <article className="stat-card">
          <span>今日のタスク</span>
          <strong>{dashboard.todayTasks.length}</strong>
          <p>今日が期限の未完了タスク</p>
        </article>
        <article className="stat-card attention">
          <span>期限切れ</span>
          <strong>{dashboard.overdueTasks.length}</strong>
          <p>期限を過ぎた未完了タスク</p>
        </article>
        <article className="stat-card">
          <span>今週の学習時間</span>
          <strong>{minutesLabel(totalMinutes)}</strong>
          <p>
            {dashboard.weeklyStudySummary.weekStart} から {dashboard.weeklyStudySummary.weekEnd}
          </p>
        </article>
        <article className="stat-card">
          <span>分野別正答率</span>
          <strong>{fieldAccuracies.length}</strong>
          <p>演習ログから集計</p>
        </article>
      </section>

      <section className="dashboard-grid three-cols">
        <div className="panel">
          <div className="section-heading horizontal">
            <div>
              <p className="eyebrow">Today</p>
              <h2>今日のタスク</h2>
            </div>
            <Link className="button-link secondary-link" to="/tasks">
              タスクを見る
            </Link>
          </div>
          <TaskList tasks={dashboard.todayTasks} />
        </div>

        <div className="panel">
          <div className="section-heading">
            <p className="eyebrow">Overdue</p>
            <h2>期限切れタスク</h2>
          </div>
          <TaskList tasks={dashboard.overdueTasks} />
        </div>

        <div className="panel">
          <div className="section-heading">
            <p className="eyebrow">Weak Fields</p>
            <h2>苦手分野ランキング</h2>
          </div>
          {weakFields.length === 0 ? (
            <EmptyState title="まだデータがありません" message="演習ログを登録すると正答率の低い分野が表示されます。" />
          ) : (
            <div className="rank-list">
              {weakFields.map((field, index) => (
                <article className="rank-item" key={field.field}>
                  <span>{index + 1}</span>
                  <div>
                    <h3>{field.field}</h3>
                    <p className="muted">
                      正答率 {percentLabel(field.accuracyRate)} / {field.correctCount}/{field.solvedCount}問
                    </p>
                  </div>
                </article>
              ))}
            </div>
          )}
        </div>
      </section>

      <section className="dashboard-grid">
        <div className="panel">
          <div className="section-heading">
            <p className="eyebrow">Weekly</p>
            <h2>学習対象別の学習時間</h2>
          </div>
          {targetSummaries.length === 0 ? (
            <EmptyState title="まだデータがありません" message="学習ログを登録すると対象別の学習時間が表示されます。" />
          ) : (
            <div className="bar-list">
              {targetSummaries.map((summary) => (
                <article className="bar-item" key={summary.studyTargetId}>
                  <div>
                    <strong>{targetLabel(summary.studyTargetName)}</strong>
                    <span>{minutesLabel(summary.totalMinutes)}</span>
                  </div>
                  <div className="progress-track">
                    <div
                      className="progress-bar"
                      style={{ width: `${Math.max(4, (summary.totalMinutes / maxTargetMinutes) * 100)}%` }}
                    />
                  </div>
                </article>
              ))}
            </div>
          )}
        </div>

        <div className="panel">
          <div className="section-heading">
            <p className="eyebrow">Accuracy</p>
            <h2>分野別正答率</h2>
          </div>
          {fieldAccuracies.length === 0 ? (
            <EmptyState title="まだデータがありません" message="演習ログを登録すると分野別の正答率が表示されます。" />
          ) : (
            <div className="mini-list">
              {fieldAccuracies.map((field) => (
                <article className="mini-item" key={field.field}>
                  <div>
                    <h3>{field.field}</h3>
                    <p className="muted">
                      {field.correctCount}/{field.solvedCount}問 正解
                    </p>
                  </div>
                  <span className="pill">{percentLabel(field.accuracyRate)}</span>
                </article>
              ))}
            </div>
          )}
        </div>
      </section>

      <section className="dashboard-grid">
        <div className="panel">
          <div className="section-heading horizontal">
            <div>
              <p className="eyebrow">Recent</p>
              <h2>直近の学習ログ</h2>
            </div>
            <Link className="button-link secondary-link" to="/logs">
              ログを見る
            </Link>
          </div>
          <StudyLogList logs={dashboard.recentStudyLogs} />
        </div>

        <div className="panel">
          <div className="section-heading">
            <p className="eyebrow">Practice</p>
            <h2>直近の演習ログ</h2>
          </div>
          <QuestionLogList logs={dashboard.recentQuestionLogs} />
        </div>
      </section>
    </div>
  );
}
