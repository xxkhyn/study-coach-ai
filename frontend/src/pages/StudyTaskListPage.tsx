import { FormEvent, useEffect, useMemo, useState } from 'react';
import EmptyState from '../components/EmptyState';
import { api } from '../services/api';
import type { StudyTarget, StudyTask, StudyTaskRequest } from '../types';

const emptyForm: StudyTaskRequest = {
  targetId: 0,
  title: '',
  fieldName: '',
  plannedMinutes: 30,
  dueDate: '',
  completed: false,
};

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
    return { label: '今日', className: 'status today-status' };
  }
  return { label: '未完了', className: 'status' };
}

export default function StudyTaskListPage() {
  const [targets, setTargets] = useState<StudyTarget[]>([]);
  const [tasks, setTasks] = useState<StudyTask[]>([]);
  const [form, setForm] = useState<StudyTaskRequest>(emptyForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const canSubmit = form.targetId > 0 && form.title.trim().length > 0;

  const completionSummary = useMemo(() => {
    const done = tasks.filter((task) => task.completed).length;
    return `${done}/${tasks.length} 完了`;
  }, [tasks]);

  useEffect(() => {
    void loadData();
  }, []);

  async function loadData() {
    setLoading(true);
    setError('');
    try {
      const [nextTargets, nextTasks] = await Promise.all([api.listTargets(), api.listTasks()]);
      setTargets(nextTargets);
      setTasks(nextTasks);
      setForm((current) => ({
        ...current,
        targetId: current.targetId || nextTargets[0]?.id || 0,
      }));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'タスク情報の取得に失敗しました');
    } finally {
      setLoading(false);
    }
  }

  function startEdit(task: StudyTask) {
    setEditingId(task.id);
    setForm({
      targetId: task.targetId,
      title: task.title,
      fieldName: task.fieldName ?? '',
      plannedMinutes: task.plannedMinutes ?? 30,
      dueDate: task.dueDate ?? '',
      completed: task.completed,
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function resetForm() {
    setEditingId(null);
    setForm({
      ...emptyForm,
      targetId: targets[0]?.id || 0,
    });
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!canSubmit) {
      return;
    }
    setSaving(true);
    setError('');
    try {
      if (editingId) {
        await api.updateTask(editingId, form);
      } else {
        await api.createTask(form);
      }
      resetForm();
      await loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : '保存に失敗しました');
    } finally {
      setSaving(false);
    }
  }

  async function toggleTask(task: StudyTask) {
    setError('');
    try {
      await api.updateTask(task.id, {
        targetId: task.targetId,
        title: task.title,
        fieldName: task.fieldName ?? '',
        plannedMinutes: task.plannedMinutes ?? undefined,
        dueDate: task.dueDate ?? '',
        completed: !task.completed,
      });
      await loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : '完了状態の更新に失敗しました');
    }
  }

  async function handleDelete(id: number) {
    if (!window.confirm('このタスクを削除しますか？')) {
      return;
    }
    setError('');
    try {
      await api.deleteTask(id);
      await loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : '削除に失敗しました');
    }
  }

  return (
    <section className="page-grid">
      <div className="panel sticky-panel">
        <div className="section-heading">
          <p className="eyebrow">Study Tasks</p>
          <h2>{editingId ? 'タスクを編集' : 'タスクを登録'}</h2>
        </div>
        {targets.length === 0 && (
          <p className="notice">先に学習対象を登録すると、タスクを追加できます。</p>
        )}
        <form className="form-stack" onSubmit={handleSubmit}>
          <label>
            学習対象
            <select
              required
              value={form.targetId}
              onChange={(event) => setForm({ ...form, targetId: Number(event.target.value) })}
            >
              <option value={0}>選択してください</option>
              {targets.map((target) => (
                <option value={target.id} key={target.id}>
                  {target.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            タイトル
            <input
              required
              value={form.title}
              onChange={(event) => setForm({ ...form, title: event.target.value })}
              placeholder="ネットワーク午前問題を20問解く"
            />
          </label>
          <div className="form-row">
            <label>
              分野
              <input
                value={form.fieldName}
                onChange={(event) => setForm({ ...form, fieldName: event.target.value })}
                placeholder="ネットワーク"
              />
            </label>
            <label>
              予定時間
              <input
                type="number"
                min="0"
                value={form.plannedMinutes}
                onChange={(event) => setForm({ ...form, plannedMinutes: Number(event.target.value) })}
              />
            </label>
          </div>
          <div className="form-row align-end">
            <label>
              期限
              <input
                type="date"
                value={form.dueDate}
                onChange={(event) => setForm({ ...form, dueDate: event.target.value })}
              />
            </label>
            <label className="checkbox-label">
              <input
                type="checkbox"
                checked={form.completed}
                onChange={(event) => setForm({ ...form, completed: event.target.checked })}
              />
              完了済み
            </label>
          </div>
          <div className="button-row">
            <button type="submit" disabled={!canSubmit || saving}>
              {saving ? '保存中...' : editingId ? '更新する' : '登録する'}
            </button>
            {editingId && (
              <button type="button" className="secondary" onClick={resetForm}>
                キャンセル
              </button>
            )}
          </div>
        </form>
      </div>

      <div className="panel">
        <div className="section-heading horizontal">
          <div>
            <p className="eyebrow">List</p>
            <h2>タスク一覧</h2>
          </div>
          <span className="pill">{completionSummary}</span>
        </div>
        {error && <p className="error">{error}</p>}
        {loading ? (
          <p className="muted">読み込み中...</p>
        ) : tasks.length === 0 ? (
          <EmptyState title="まだタスクがありません" message="今日進めたい学習タスクを登録しましょう。" />
        ) : (
          <div className="item-list">
            {tasks.map((task) => {
              const status = getTaskStatus(task);
              return (
                <article className={`item-card ${task.completed ? 'is-completed' : ''}`} key={task.id}>
                  <div className="card-title-row">
                    <div className="task-title-row">
                      <label className="compact-check">
                        <input type="checkbox" checked={task.completed} onChange={() => void toggleTask(task)} />
                        <span>{task.completed ? '戻す' : '完了'}</span>
                      </label>
                      <h3>{task.title}</h3>
                    </div>
                    <span className={status.className}>{status.label}</span>
                  </div>
                  <p className="muted">{task.targetName}</p>
                  <dl className="meta-grid">
                    <div>
                      <dt>分野</dt>
                      <dd>{task.fieldName || '-'}</dd>
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
                    <button type="button" className="secondary" onClick={() => startEdit(task)}>
                      編集
                    </button>
                    <button type="button" className="danger" onClick={() => void handleDelete(task.id)}>
                      削除
                    </button>
                  </div>
                </article>
              );
            })}
          </div>
        )}
      </div>
    </section>
  );
}
