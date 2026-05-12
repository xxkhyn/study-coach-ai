import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import EmptyState from '../components/EmptyState';
import { api } from '../services/api';
import type { StudyTask } from '../types';

function isToday(dateText?: string | null) {
  if (!dateText) return false;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const date = new Date(`${dateText}T00:00:00`);
  return date.getTime() === today.getTime();
}

function getTaskStatus(task: StudyTask) {
  if (task.completed) {
    return { label: '完了', className: 'status done' };
  }
  if (!task.dueDate) {
    return { label: '期限なし', className: 'status' };
  }
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const dueDate = new Date(`${task.dueDate}T00:00:00`);
  if (dueDate.getTime() < today.getTime()) {
    return { label: '期限超過', className: 'status danger-status' };
  }
  if (dueDate.getTime() === today.getTime()) {
    return { label: '今日が期限', className: 'status today-status' };
  }
  return { label: '未完了', className: 'status' };
}

function TaskCard({
  task,
  onComplete,
  onDelete,
}: {
  task: StudyTask;
  onComplete: (task: StudyTask) => void;
  onDelete: (id: number) => void;
}) {
  const status = getTaskStatus(task);

  return (
    <article className={`item-card ${task.completed ? 'is-completed' : ''} ${isToday(task.dueDate) ? 'is-due-today' : ''}`}>
      <div className="card-title-row">
        <div>
          <h3>{task.title}</h3>
          <p className="muted">{task.studyTargetName || `学習対象ID: ${task.studyTargetId}`}</p>
        </div>
        <span className={status.className}>{status.label}</span>
      </div>
      <dl className="meta-grid">
        <div>
          <dt>分野</dt>
          <dd>{task.field || '-'}</dd>
        </div>
        <div>
          <dt>予定</dt>
          <dd>{task.plannedMinutes ?? 0}分</dd>
        </div>
        <div>
          <dt>期限</dt>
          <dd>{task.dueDate || '-'}</dd>
        </div>
      </dl>
      <div className="button-row">
        <button type="button" className="secondary" onClick={() => onComplete(task)}>
          {task.completed ? '未完了に戻す' : '完了にする'}
        </button>
        <Link className="button-link secondary-link" to={`/tasks/${task.id}/edit`}>
          編集
        </Link>
        <button type="button" className="danger" onClick={() => onDelete(task.id)}>
          削除
        </button>
      </div>
    </article>
  );
}

export default function StudyTaskListPage() {
  const [tasks, setTasks] = useState<StudyTask[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const incompleteTasks = useMemo(() => tasks.filter((task) => !task.completed), [tasks]);
  const completedTasks = useMemo(() => tasks.filter((task) => task.completed), [tasks]);
  const todayTasks = useMemo(() => incompleteTasks.filter((task) => isToday(task.dueDate)), [incompleteTasks]);
  const completionSummary = `${completedTasks.length}/${tasks.length} 完了`;

  useEffect(() => {
    void loadTasks();
  }, []);

  async function loadTasks() {
    setLoading(true);
    setError('');
    try {
      setTasks(await api.listTasks());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'タスク情報の取得に失敗しました。');
    } finally {
      setLoading(false);
    }
  }

  async function toggleTask(task: StudyTask) {
    setError('');
    try {
      await api.completeTask(task.id, !task.completed);
      await loadTasks();
    } catch (err) {
      setError(err instanceof Error ? err.message : '完了状態の更新に失敗しました。');
    }
  }

  async function handleDelete(id: number) {
    if (!window.confirm('このタスクを削除しますか？')) {
      return;
    }
    setError('');
    try {
      await api.deleteTask(id);
      await loadTasks();
    } catch (err) {
      setError(err instanceof Error ? err.message : '削除に失敗しました。');
    }
  }

  return (
    <section className="panel">
      <div className="section-heading horizontal">
        <div>
          <p className="eyebrow">Study Tasks</p>
          <h2>学習タスク一覧</h2>
        </div>
        <div className="heading-actions">
          <span className="pill">{completionSummary}</span>
          <span className="pill today-pill">今日が期限 {todayTasks.length}件</span>
          <Link className="button-link" to="/tasks/new">
            タスクを登録
          </Link>
        </div>
      </div>

      {error && <p className="error">{error}</p>}
      {loading ? (
        <p className="muted">読み込み中...</p>
      ) : tasks.length === 0 ? (
        <EmptyState title="まだタスクがありません" message="今日進めたい学習タスクを登録しましょう。" />
      ) : (
        <div className="task-sections">
          <section>
            <div className="sub-heading">
              <h3>未完了タスク</h3>
              <span className="pill">{incompleteTasks.length}件</span>
            </div>
            {incompleteTasks.length === 0 ? (
              <EmptyState title="未完了タスクはありません" message="完了済みのタスクは下にまとまっています。" />
            ) : (
              <div className="item-list">
                {incompleteTasks.map((task) => (
                  <TaskCard task={task} key={task.id} onComplete={(nextTask) => void toggleTask(nextTask)} onDelete={(taskId) => void handleDelete(taskId)} />
                ))}
              </div>
            )}
          </section>

          <section>
            <div className="sub-heading">
              <h3>完了済みタスク</h3>
              <span className="pill">{completedTasks.length}件</span>
            </div>
            {completedTasks.length === 0 ? (
              <EmptyState title="完了済みタスクはまだありません" message="終わったタスクを完了にするとここに表示されます。" />
            ) : (
              <div className="item-list">
                {completedTasks.map((task) => (
                  <TaskCard task={task} key={task.id} onComplete={(nextTask) => void toggleTask(nextTask)} onDelete={(taskId) => void handleDelete(taskId)} />
                ))}
              </div>
            )}
          </section>
        </div>
      )}
    </section>
  );
}
