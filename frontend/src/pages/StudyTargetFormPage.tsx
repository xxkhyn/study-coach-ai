import { FormEvent, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../services/api';
import type { StudyTargetRequest } from '../types';

const emptyForm: StudyTargetRequest = {
  name: '',
  description: '',
  targetDate: '',
};

export default function StudyTargetFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const editingId = id ? Number(id) : null;
  const [form, setForm] = useState<StudyTargetRequest>(emptyForm);
  const [loading, setLoading] = useState(Boolean(editingId));
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (editingId) {
      void loadTarget(editingId);
    }
  }, [editingId]);

  async function loadTarget(targetId: number) {
    setLoading(true);
    setError('');
    try {
      const target = await api.getTarget(targetId);
      setForm({
        name: target.name,
        description: target.description ?? '',
        targetDate: target.targetDate ?? '',
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : '学習対象の取得に失敗しました。');
    } finally {
      setLoading(false);
    }
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
      navigate('/targets');
    } catch (err) {
      setError(err instanceof Error ? err.message : '保存に失敗しました。');
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="panel narrow-panel">
      <div className="section-heading">
        <p className="eyebrow">Study Target</p>
        <h2>{editingId ? '学習対象を編集' : '学習対象を登録'}</h2>
      </div>

      {error && <p className="error">{error}</p>}
      {loading ? (
        <p className="muted">読み込み中...</p>
      ) : (
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
            説明
            <textarea
              value={form.description}
              onChange={(event) => setForm({ ...form, description: event.target.value })}
              placeholder="午後問題を重点的に進める"
            />
          </label>
          <label>
            目標日
            <input
              type="date"
              value={form.targetDate}
              onChange={(event) => setForm({ ...form, targetDate: event.target.value })}
            />
          </label>
          <div className="button-row">
            <button type="submit" disabled={saving}>
              {saving ? '保存中...' : '保存する'}
            </button>
            <Link className="button-link secondary-link" to="/targets">
              戻る
            </Link>
          </div>
        </form>
      )}
    </section>
  );
}
