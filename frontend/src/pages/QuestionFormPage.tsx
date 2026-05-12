import { FormEvent, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../services/api';
import type { QuestionRequest, QuestionSourceType, StudyTarget } from '../types';

const emptyForm: QuestionRequest = {
  studyTargetId: 0,
  examType: '',
  year: undefined,
  season: '',
  timeCategory: '',
  questionNumber: '',
  field: '',
  difficulty: '',
  questionText: '',
  choices: ['', '', '', ''],
  answerIndex: 0,
  explanation: '',
  sourceType: 'USER_CREATED',
  sourceLabel: '',
  sourceUrl: '',
};

const choiceLabels = ['ア', 'イ', 'ウ', 'エ'];

export default function QuestionFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const editingId = id ? Number(id) : null;
  const [targets, setTargets] = useState<StudyTarget[]>([]);
  const [form, setForm] = useState<QuestionRequest>(emptyForm);
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
        const question = await api.getQuestion(editingId);
        setForm({
          studyTargetId: question.studyTargetId,
          examType: question.examType ?? '',
          year: question.year ?? undefined,
          season: question.season ?? '',
          timeCategory: question.timeCategory ?? '',
          questionNumber: question.questionNumber ?? '',
          field: question.field ?? '',
          difficulty: question.difficulty ?? '',
          questionText: question.questionText,
          choices: question.choices,
          answerIndex: question.answerIndex,
          explanation: question.explanation ?? '',
          sourceType: question.sourceType,
          sourceLabel: question.sourceLabel ?? '',
          sourceUrl: question.sourceUrl ?? '',
        });
      } else {
        setForm({ ...emptyForm, studyTargetId: nextTargets[0]?.id ?? 0 });
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : '画面情報の取得に失敗しました。');
    } finally {
      setLoading(false);
    }
  }

  function updateChoice(index: number, value: string) {
    const choices = [...form.choices];
    choices[index] = value;
    setForm({ ...form, choices });
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (form.studyTargetId <= 0) {
      setError('学習対象を選択してください。');
      return;
    }
    if (form.choices.some((choice) => choice.trim().length === 0)) {
      setError('選択肢4つをすべて入力してください。');
      return;
    }

    setSaving(true);
    setError('');
    try {
      if (editingId) {
        await api.updateQuestion(editingId, form);
      } else {
        await api.createQuestion(form);
      }
      navigate('/questions');
    } catch (err) {
      setError(err instanceof Error ? err.message : '保存に失敗しました。');
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="panel narrow-panel">
      <div className="section-heading">
        <p className="eyebrow">Question</p>
        <h2>{editingId ? '問題を編集' : '問題を登録'}</h2>
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
                <option key={target.id} value={target.id}>
                  {target.name}
                </option>
              ))}
            </select>
          </label>

          <div className="form-row">
            <label>
              試験種別
              <input value={form.examType} onChange={(event) => setForm({ ...form, examType: event.target.value })} placeholder="応用情報技術者試験" />
            </label>
            <label>
              年度
              <input
                type="number"
                value={form.year ?? ''}
                onChange={(event) => setForm({ ...form, year: event.target.value ? Number(event.target.value) : undefined })}
                placeholder="2025"
              />
            </label>
          </div>

          <div className="form-row">
            <label>
              季節
              <input value={form.season} onChange={(event) => setForm({ ...form, season: event.target.value })} placeholder="春期" />
            </label>
            <label>
              午前/午後
              <input value={form.timeCategory} onChange={(event) => setForm({ ...form, timeCategory: event.target.value })} placeholder="午前" />
            </label>
          </div>

          <div className="form-row">
            <label>
              問題番号
              <input value={form.questionNumber} onChange={(event) => setForm({ ...form, questionNumber: event.target.value })} placeholder="問1" />
            </label>
            <label>
              分野
              <input value={form.field} onChange={(event) => setForm({ ...form, field: event.target.value })} placeholder="ネットワーク" />
            </label>
          </div>

          <label>
            難易度
            <input value={form.difficulty} onChange={(event) => setForm({ ...form, difficulty: event.target.value })} placeholder="標準" />
          </label>

          <label>
            問題文
            <textarea required value={form.questionText} onChange={(event) => setForm({ ...form, questionText: event.target.value })} />
          </label>

          {choiceLabels.map((label, index) => (
            <label key={label}>
              選択肢{label}
              <input required value={form.choices[index] ?? ''} onChange={(event) => updateChoice(index, event.target.value)} />
            </label>
          ))}

          <label>
            正解
            <select value={form.answerIndex} onChange={(event) => setForm({ ...form, answerIndex: Number(event.target.value) })}>
              {choiceLabels.map((label, index) => (
                <option key={label} value={index}>
                  {label}
                </option>
              ))}
            </select>
          </label>

          <label>
            解説
            <textarea value={form.explanation} onChange={(event) => setForm({ ...form, explanation: event.target.value })} />
          </label>

          <div className="form-row">
            <label>
              出典種別
              <select value={form.sourceType} onChange={(event) => setForm({ ...form, sourceType: event.target.value as QuestionSourceType })}>
                <option value="IPA_PAST_EXAM">IPA過去問</option>
                <option value="AI_GENERATED">AI生成</option>
                <option value="USER_CREATED">手動作成</option>
                <option value="PRIVATE_NOTE">個人メモ</option>
              </select>
            </label>
            <label>
              出典ラベル
              <input value={form.sourceLabel} onChange={(event) => setForm({ ...form, sourceLabel: event.target.value })} placeholder="令和6年春期 午前" />
            </label>
          </div>

          <label>
            出典URL
            <input value={form.sourceUrl} onChange={(event) => setForm({ ...form, sourceUrl: event.target.value })} placeholder="https://..." />
          </label>

          <div className="button-row">
            <button type="submit" disabled={saving || targets.length === 0}>
              {saving ? '保存中...' : '保存する'}
            </button>
            <Link className="button-link secondary-link" to="/questions">
              戻る
            </Link>
          </div>
        </form>
      )}
    </section>
  );
}
