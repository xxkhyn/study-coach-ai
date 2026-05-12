import { FormEvent, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../services/api';
import type { QuestionLogRequest, StudyTarget } from '../types';

function todayText() {
  return new Date().toISOString().slice(0, 10);
}

const emptyForm: QuestionLogRequest = {
  studyTargetId: 0,
  field: '',
  practicedDate: todayText(),
  solvedCount: 10,
  correctCount: 0,
  memo: '',
};

export default function QuestionLogFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const editingId = id ? Number(id) : null;
  const [targets, setTargets] = useState<StudyTarget[]>([]);
  const [form, setForm] = useState<QuestionLogRequest>(emptyForm);
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
        const log = await api.getQuestionLog(editingId);
        setForm({
          studyTargetId: log.studyTargetId,
          field: log.field ?? '',
          practicedDate: log.practicedDate,
          solvedCount: log.solvedCount,
          correctCount: log.correctCount,
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
    if (form.correctCount > form.solvedCount) {
      setError('正解数は解いた問題数以下にしてください。');
      return;
    }

    setSaving(true);
    setError('');
    try {
      if (editingId) {
        await api.updateQuestionLog(editingId, form);
      } else {
        await api.createQuestionLog(form);
      }
      navigate('/question-logs');
    } catch (err) {
      setError(err instanceof Error ? err.message : '保存に失敗しました。');
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="panel narrow-panel">
      <div className="section-heading">
        <p className="eyebrow">Question Log</p>
        <h2>{editingId ? '演習ログを編集' : '演習ログを登録'}</h2>
      </div>

      {error && <p className="error">{error}</p>}
      {loading ? (
        <p className="muted">読み込み中...</p>
      ) : (
        <form className="form-stack" onSubmit={handleSubmit}>
          {targets.length === 0 && <p className="notice">先に学習対象を登録してください。</p>}
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
              <input value={form.field} onChange={(event) => setForm({ ...form, field: event.target.value })} placeholder="ネットワーク" />
            </label>
            <label>
              演習日
              <input
                required
                type="date"
                value={form.practicedDate}
                onChange={(event) => setForm({ ...form, practicedDate: event.target.value })}
              />
            </label>
          </div>

          <div className="form-row">
            <label>
              解いた問題数
              <input
                required
                type="number"
                min="0"
                value={form.solvedCount}
                onChange={(event) => setForm({ ...form, solvedCount: Number(event.target.value) })}
              />
            </label>
            <label>
              正解数
              <input
                required
                type="number"
                min="0"
                value={form.correctCount}
                onChange={(event) => setForm({ ...form, correctCount: Number(event.target.value) })}
              />
            </label>
          </div>

          <label>
            メモ
            <textarea
              value={form.memo}
              onChange={(event) => setForm({ ...form, memo: event.target.value })}
              placeholder="間違えた問題や復習したい内容"
            />
          </label>

          <div className="button-row">
            <button type="submit" disabled={saving || targets.length === 0}>
              {saving ? '保存中...' : '保存する'}
            </button>
            <Link className="button-link secondary-link" to="/question-logs">
              戻る
            </Link>
          </div>
        </form>
      )}
    </section>
  );
}
