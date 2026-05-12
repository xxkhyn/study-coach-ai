import { FormEvent, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../services/api';
import type { StudyLogRequest, StudyTarget } from '../types';

function todayText() {
  return new Date().toISOString().slice(0, 10);
}

const emptyForm: StudyLogRequest = {
  studyTargetId: 0,
  field: '',
  studiedDate: todayText(),
  minutes: 30,
  memo: '',
};

export default function StudyLogFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const editingId = id ? Number(id) : null;
  const [targets, setTargets] = useState<StudyTarget[]>([]);
  const [form, setForm] = useState<StudyLogRequest>(emptyForm);
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
        const log = await api.getStudyLog(editingId);
        setForm({
          studyTargetId: log.studyTargetId,
          field: log.field ?? '',
          studiedDate: log.studiedDate,
          minutes: log.minutes,
          memo: log.memo ?? '',
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
        await api.updateStudyLog(editingId, form);
      } else {
        await api.createStudyLog(form);
      }
      navigate('/logs');
    } catch (err) {
      setError(err instanceof Error ? err.message : '保存に失敗しました。');
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="panel narrow-panel">
      <div className="section-heading">
        <p className="eyebrow">Study Log</p>
        <h2>{editingId ? '学習ログを編集' : '学習ログを登録'}</h2>
      </div>

      {error && <p className="error">{error}</p>}
      {loading ? (
        <p className="muted">読み込み中...</p>
      ) : (
        <form className="form-stack" onSubmit={handleSubmit}>
          {targets.length === 0 && (
            <p className="notice">先に学習対象を登録すると、学習ログを追加できます。</p>
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
              学習日
              <input
                required
                type="date"
                value={form.studiedDate}
                onChange={(event) => setForm({ ...form, studiedDate: event.target.value })}
              />
            </label>
          </div>
          <label>
            学習時間
            <input
              required
              type="number"
              min="1"
              value={form.minutes}
              onChange={(event) => setForm({ ...form, minutes: Number(event.target.value) })}
            />
          </label>
          <label>
            メモ
            <textarea
              value={form.memo}
              onChange={(event) => setForm({ ...form, memo: event.target.value })}
              placeholder="午後問題の解説を読み直した"
            />
          </label>
          <div className="button-row">
            <button type="submit" disabled={saving || targets.length === 0}>
              {saving ? '保存中...' : '保存する'}
            </button>
            <Link className="button-link secondary-link" to="/logs">
              戻る
            </Link>
          </div>
        </form>
      )}
    </section>
  );
}
