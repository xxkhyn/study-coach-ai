import { FormEvent, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import EmptyState from '../components/EmptyState';
import { api } from '../services/api';
import type { Question, QuestionGenerationLog, QuestionGenerationRequest, StudyTarget } from '../types';

const examTypes: QuestionGenerationRequest['examType'][] = ['応用情報技術者試験', '証券外務員一種'];
const difficulties: Array<{ value: QuestionGenerationRequest['difficulty']; label: string }> = [
  { value: 'basic', label: 'basic' },
  { value: 'standard', label: 'standard' },
  { value: 'advanced', label: 'advanced' },
];

export default function QuestionAiGeneratePage() {
  const [targets, setTargets] = useState<StudyTarget[]>([]);
  const [studyTargetId, setStudyTargetId] = useState(0);
  const [examType, setExamType] = useState<QuestionGenerationRequest['examType']>('応用情報技術者試験');
  const [field, setField] = useState('ネットワーク');
  const [difficulty, setDifficulty] = useState<QuestionGenerationRequest['difficulty']>('basic');
  const [count, setCount] = useState(3);
  const [generatedQuestions, setGeneratedQuestions] = useState<Question[]>([]);
  const [logs, setLogs] = useState<QuestionGenerationLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    void loadInitialData();
  }, []);

  async function loadInitialData() {
    setLoading(true);
    setError('');
    try {
      const [nextTargets, nextLogs] = await Promise.all([
        api.listTargets(),
        api.listQuestionGenerationLogs(),
      ]);
      setTargets(nextTargets);
      setStudyTargetId(nextTargets[0]?.id ?? 0);
      setLogs(nextLogs);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'AI問題生成画面の読み込みに失敗しました。');
    } finally {
      setLoading(false);
    }
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (studyTargetId <= 0) {
      setError('学習対象を選択してください。');
      return;
    }
    if (!field.trim()) {
      setError('分野を入力してください。');
      return;
    }

    setGenerating(true);
    setError('');
    setGeneratedQuestions([]);
    try {
      const questions = await api.generateAiQuestions({
        studyTargetId,
        examType,
        field: field.trim(),
        difficulty,
        count,
      });
      setGeneratedQuestions(questions);
      setLogs(await api.listQuestionGenerationLogs());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'AI問題生成に失敗しました。');
    } finally {
      setGenerating(false);
    }
  }

  return (
    <section className="page-grid">
      <div className="panel sticky-panel">
        <div className="section-heading">
          <p className="eyebrow">AI Question Generation</p>
          <h2>AI問題生成</h2>
        </div>

        {error && <p className="error">{error}</p>}
        {loading ? (
          <p className="muted">読み込み中...</p>
        ) : (
          <form className="form-stack" onSubmit={handleSubmit}>
            {targets.length === 0 && <p className="notice">先に学習対象を登録してください。</p>}
            <label>
              学習対象
              <select value={studyTargetId} onChange={(event) => setStudyTargetId(Number(event.target.value))} required>
                <option value={0}>選択してください</option>
                {targets.map((target) => (
                  <option key={target.id} value={target.id}>
                    {target.name}
                  </option>
                ))}
              </select>
            </label>
            <label>
              資格種別
              <select
                value={examType}
                onChange={(event) => setExamType(event.target.value as QuestionGenerationRequest['examType'])}
              >
                {examTypes.map((type) => (
                  <option key={type} value={type}>
                    {type}
                  </option>
                ))}
              </select>
            </label>
            <label>
              分野
              <input value={field} onChange={(event) => setField(event.target.value)} placeholder="ネットワーク" required />
            </label>
            <label>
              難易度
              <select
                value={difficulty}
                onChange={(event) => setDifficulty(event.target.value as QuestionGenerationRequest['difficulty'])}
              >
                {difficulties.map((item) => (
                  <option key={item.value} value={item.value}>
                    {item.label}
                  </option>
                ))}
              </select>
            </label>
            <label>
              問題数
              <input
                max={10}
                min={1}
                type="number"
                value={count}
                onChange={(event) => setCount(Number(event.target.value))}
              />
            </label>
            <div className="button-row">
              <button type="submit" disabled={generating || targets.length === 0}>
                {generating ? '生成中...' : 'AIで生成'}
              </button>
              <Link className="button-link secondary-link" to="/questions">
                問題一覧へ
              </Link>
            </div>
          </form>
        )}
      </div>

      <div className="stack">
        <div className="panel">
          <div className="section-heading horizontal">
            <div>
              <p className="eyebrow">Generated</p>
              <h2>生成結果</h2>
            </div>
            <Link className="button-link secondary-link" to="/questions/practice">
              演習する
            </Link>
          </div>

          {generatedQuestions.length === 0 ? (
            <EmptyState title="まだ生成結果がありません" message="条件を入力して、AIで問題を生成してください。" />
          ) : (
            <div className="item-list">
              {generatedQuestions.map((question) => (
                <article className="item-card" key={question.id}>
                  <div className="card-title-row">
                    <div>
                      <h3>{question.questionText}</h3>
                      <p className="muted">
                        {question.examType} / {question.field} / {question.difficulty}
                      </p>
                    </div>
                    <span className="status">AI生成</span>
                  </div>
                  <ol className="choice-list">
                    {question.choices.map((choice, index) => (
                      <li className={index === question.answerIndex ? 'correct-choice' : undefined} key={`${question.id}-${index}`}>
                        {choice}
                      </li>
                    ))}
                  </ol>
                  <p className="muted">{question.explanation}</p>
                </article>
              ))}
            </div>
          )}
        </div>

        <div className="panel">
          <div className="section-heading">
            <p className="eyebrow">History</p>
            <h2>生成履歴</h2>
          </div>
          {logs.length === 0 ? (
            <EmptyState title="まだ生成履歴がありません" message="AI問題を生成すると、ここに履歴が表示されます。" />
          ) : (
            <div className="item-list compact-list">
              {logs.map((log) => (
                <article className="item-card" key={log.id}>
                  <div className="card-title-row">
                    <div>
                      <h3>{log.examType}</h3>
                      <p className="muted">
                        {log.studyTargetName ?? '学習対象未設定'} / {log.field} / {log.difficulty}
                      </p>
                    </div>
                    <span className="status">{log.count}問</span>
                  </div>
                  <p className="muted">{new Date(log.createdAt).toLocaleString()}</p>
                </article>
              ))}
            </div>
          )}
        </div>
      </div>
    </section>
  );
}
