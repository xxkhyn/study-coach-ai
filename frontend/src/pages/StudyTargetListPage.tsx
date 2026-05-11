import { FormEvent, useEffect, useMemo, useState } from 'react';
import EmptyState from '../components/EmptyState';
import { api } from '../services/api';
import type { StudyTarget, StudyTargetRequest } from '../types';

const emptyForm: StudyTargetRequest = {
  name: '',
  category: '',
  examDate: '',
  goalDate: '',
  memo: '',
};

function daysUntil(dateText?: string | null) {
  if (!dateText) {
    return null;
  }
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const targetDate = new Date(`${dateText}T00:00:00`);
  return Math.ceil((targetDate.getTime() - today.getTime()) / 86_400_000);
}

export default function StudyTargetListPage() {
  const [targets, setTargets] = useState<StudyTarget[]>([]);
  const [form, setForm] = useState<StudyTargetRequest>(emptyForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const sortedTargets = useMemo(
    () =>
      [...targets].sort((a, b) => {
        const aDate = a.examDate ?? a.goalDate ?? '9999-12-31';
        const bDate = b.examDate ?? b.goalDate ?? '9999-12-31';
        return aDate.localeCompare(bDate);
      }),
    [targets],
  );

  useEffect(() => {
    void loadTargets();
  }, []);

  async function loadTargets() {
    setLoading(true);
    setError('');
    try {
      setTargets(await api.listTargets());
    } catch (err) {
      setError(err instanceof Error ? err.message : '学習対象の取得に失敗しました');
    } finally {
      setLoading(false);
    }
  }

  function startEdit(target: StudyTarget) {
    setEditingId(target.id);
    setForm({
      name: target.name,
      category: target.category ?? '',
      examDate: target.examDate ?? '',
      goalDate: target.goalDate ?? '',
      memo: target.memo ?? '',
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function resetForm() {
    setEditingId(null);
    setForm(emptyForm);
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      if (editingId) {
        await api.updateTarget(editingId, form);
      } else {
        await api.createTarget(form);
      }
      resetForm();
      await loadTargets();
    } catch (err) {
      setError(err instanceof Error ? err.message : '保存に失敗しました');
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(id: number) {
    if (!window.confirm('この学習対象を削除しますか？紐づくタスクがある場合は先にタスクを削除してください。')) {
      return;
    }
    setError('');
    try {
      await api.deleteTarget(id);
      await loadTargets();
    } catch (err) {
      setError(err instanceof Error ? err.message : '削除に失敗しました');
    }
  }

  return (
    <section className="page-grid">
      <div className="panel sticky-panel">
        <div className="section-heading">
          <p className="eyebrow">Study Targets</p>
          <h2>{editingId ? '学習対象を編集' : '学習対象を登録'}</h2>
        </div>
        <form className="form-stack" onSubmit={handleSubmit}>
          <label>
            名称
            <input
              required
              value={form.name}
              onChange={(event) => setForm({ ...form, name: event.target.value })}
              placeholder="応用情報技術者試験"
            />
          </label>
          <label>
            種別
            <input
              value={form.category}
              onChange={(event) => setForm({ ...form, category: event.target.value })}
              placeholder="資格 / 技術 / クラウド"
            />
          </label>
          <div className="form-row">
            <label>
              試験日
              <input
                type="date"
                value={form.examDate}
                onChange={(event) => setForm({ ...form, examDate: event.target.value })}
              />
            </label>
            <label>
              目標日
              <input
                type="date"
                value={form.goalDate}
                onChange={(event) => setForm({ ...form, goalDate: event.target.value })}
              />
            </label>
          </div>
          <label>
            メモ
            <textarea
              value={form.memo}
              onChange={(event) => setForm({ ...form, memo: event.target.value })}
              placeholder="午前対策を先に終わらせる"
            />
          </label>
          <div className="button-row">
            <button type="submit" disabled={saving}>
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
            <h2>学習対象一覧</h2>
          </div>
          <span className="pill">{targets.length}件</span>
        </div>
        {error && <p className="error">{error}</p>}
        {loading ? (
          <p className="muted">読み込み中...</p>
        ) : sortedTargets.length === 0 ? (
          <EmptyState title="まだ登録がありません" message="最初の資格や技術テーマを登録しましょう。" />
        ) : (
          <div className="item-list">
            {sortedTargets.map((target) => {
              const remainingDays = daysUntil(target.examDate ?? target.goalDate);
              return (
                <article className="item-card" key={target.id}>
                  <div className="card-title-row">
                    <div>
                      <h3>{target.name}</h3>
                      <p className="muted">{target.category || '種別未設定'}</p>
                    </div>
                    {remainingDays !== null && (
                      <span className={remainingDays < 0 ? 'status danger-status' : 'status'}>
                        {remainingDays < 0 ? `${Math.abs(remainingDays)}日経過` : `あと${remainingDays}日`}
                      </span>
                    )}
                  </div>
                  <dl className="meta-grid two-cols">
                    <div>
                      <dt>試験日</dt>
                      <dd>{target.examDate || '-'}</dd>
                    </div>
                    <div>
                      <dt>目標日</dt>
                      <dd>{target.goalDate || '-'}</dd>
                    </div>
                  </dl>
                  {target.memo && <p>{target.memo}</p>}
                  <div className="button-row">
                    <button type="button" className="secondary" onClick={() => startEdit(target)}>
                      編集
                    </button>
                    <button type="button" className="danger" onClick={() => void handleDelete(target.id)}>
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
