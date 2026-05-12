import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import EmptyState from '../components/EmptyState';
import { api } from '../services/api';
import type { Question } from '../types';

const sourceLabels: Record<string, string> = {
  IPA_PAST_EXAM: 'IPA過去問',
  AI_GENERATED: 'AI生成',
  USER_CREATED: '手動作成',
  PRIVATE_NOTE: '個人メモ',
};

export default function QuestionListPage() {
  const [questions, setQuestions] = useState<Question[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    void loadQuestions();
  }, []);

  async function loadQuestions() {
    setLoading(true);
    setError('');
    try {
      setQuestions(await api.listQuestions());
    } catch (err) {
      setError(err instanceof Error ? err.message : '問題の取得に失敗しました。');
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(id: number) {
    if (!window.confirm('この問題を削除しますか？解答履歴も削除されます。')) {
      return;
    }
    setError('');
    try {
      await api.deleteQuestion(id);
      await loadQuestions();
    } catch (err) {
      setError(err instanceof Error ? err.message : '削除に失敗しました。');
    }
  }

  return (
    <section className="panel">
      <div className="section-heading horizontal">
        <div>
          <p className="eyebrow">Questions</p>
          <h2>問題一覧</h2>
        </div>
        <div className="heading-actions">
          <Link className="button-link secondary-link" to="/questions/practice">
            演習する
          </Link>
          <Link className="button-link secondary-link" to="/questions/wrong">
            間違えた問題
          </Link>
          <Link className="button-link secondary-link" to="/questions/import">
            CSVインポート
          </Link>
          <Link className="button-link secondary-link" to="/questions/generate-ai">
            AI問題生成
          </Link>
          <Link className="button-link" to="/questions/new">
            問題を登録
          </Link>
        </div>
      </div>

      {error && <p className="error">{error}</p>}
      {loading ? (
        <p className="muted">読み込み中...</p>
      ) : questions.length === 0 ? (
        <EmptyState title="まだ問題がありません" message="4択問題を手動登録するか、CSVからまとめてインポートできます。" />
      ) : (
        <div className="item-list">
          {questions.map((question) => (
            <article className="item-card" key={question.id}>
              <div className="card-title-row">
                <div>
                  <h3>{question.questionText}</h3>
                  <p className="muted">
                    {question.studyTargetName ?? '学習対象未設定'} / {question.field || '分野未設定'}
                  </p>
                </div>
                <span className="status">{sourceLabels[question.sourceType] ?? question.sourceType}</span>
              </div>
              <dl className="meta-grid">
                <div>
                  <dt>試験</dt>
                  <dd>{question.examType || '-'}</dd>
                </div>
                <div>
                  <dt>年度</dt>
                  <dd>{question.year ?? '-'}</dd>
                </div>
                <div>
                  <dt>難易度</dt>
                  <dd>{question.difficulty || '-'}</dd>
                </div>
              </dl>
              <div className="button-row">
                <Link className="button-link secondary-link" to={`/questions/${question.id}/edit`}>
                  編集
                </Link>
                <button type="button" className="danger" onClick={() => void handleDelete(question.id)}>
                  削除
                </button>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
