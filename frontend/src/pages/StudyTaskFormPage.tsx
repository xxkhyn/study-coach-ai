import { FormEvent, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../services/api';
import type { StudyTarget, StudyTaskRequest } from '../types';

const emptyForm: StudyTaskRequest = {
  studyTargetId: 0,
  title: '',
  field: '',
  plannedMinutes: 30,
  dueDate: '',
  completed: false,
};

export default function StudyTaskFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const editingId = id ? Number(id) : null;
  const [targets, setTargets] = useState<StudyTarget[]>([]);
  const [form, setForm] = useState<StudyTaskRequest>(emptyForm);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    void loadPage();
  }, [editingId]);

  async function loadPage() {
    setLoading(true);
    setError('');
    try {
      const nextTargets = await api.listTargets();
      setTargets(nextTargets);

      if (editingId) {
        const task = await api.getTask(editingId);
        setForm({
          studyTargetId: task.studyTargetId,
          title: task.title,
          field: task.field ?? '',
          plannedMinutes: task.plannedMinutes ?? 30,
          dueDate: task.dueDate ?? '',
          completed: task.completed,
        });
      } else {
        setForm({
          ...emptyForm,
          studyTargetId: nextTargets[0]?.id ?? 0,
        });
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : '画面情報の取得に失敗しました。');
    } finally {
      setLoading(false);
    }
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (form.studyTargetId <= 0) {
      setError('学習対象を選択してください。');
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
      navigate('/tasks');
    } catch (err) {
      setError(err instanceof Error ? err.message : '保存に失敗しました。');
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="panel narrow-panel">
      <div className="section-heading">
        <p className="eyebrow">Study Task</p>
        <h2>{editingId ? '学習タスクを編集' : '学習タスクを登録'}</h2>
      </div>

      {error && <p className="error">{error}</p>}
      {loading ? (
        <p className="muted">読み込み中...</p>
      ) : (
        <form className="form-stack" onSubmit={handleSubmit}>
          {targets.length === 0 && (
            <p className="notice">先に学習対象を登録すると、タスクを追加できます。</p>
          )}
          <label>
            学習対象
            <select
              required
              value={form.studyTargetId}
              onChange={(event) => setForm({ ...form, studyTargetId: Number(event.target.value) })}
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
                value={form.field}
                onChange={(event) => setForm({ ...form, field: event.target.value })}
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
            <button type="submit" disabled={saving || targets.length === 0}>
              {saving ? '保存中...' : '保存する'}
            </button>
            <Link className="button-link secondary-link" to="/tasks">
              戻る
            </Link>
          </div>
        </form>
      )}
    </section>
  );
}
