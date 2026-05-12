import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import EmptyState from '../components/EmptyState';
import { api } from '../services/api';
import type { Question } from '../types';

export default function WrongQuestionListPage() {
  const [questions, setQuestions] = useState<Question[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    void loadWrongQuestions();
  }, []);

  async function loadWrongQuestions() {
    setLoading(true);
    setError('');
    try {
      setQuestions(await api.listWrongQuestions());
    } catch (err) {
      setError(err instanceof Error ? err.message : '間違えた問題の取得に失敗しました。');
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="panel">
      <div className="section-heading horizontal">
        <div>
          <p className="eyebrow">Review</p>
          <h2>間違えた問題</h2>
        </div>
        <div className="heading-actions">
          <Link className="button-link secondary-link" to="/questions/practice">
            演習する
          </Link>
          <Link className="button-link secondary-link" to="/questions">
            問題一覧へ
          </Link>
        </div>
      </div>

      {error && <p className="error">{error}</p>}
      {loading ? (
        <p className="muted">読み込み中...</p>
      ) : questions.length === 0 ? (
        <EmptyState title="まだ間違えた問題がありません" message="演習結果がここに復習用として表示されます。" />
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
                <span className="status danger-status">復習対象</span>
              </div>
              <p className="muted">{question.explanation || '解説は登録されていません。'}</p>
              <div className="button-row">
                <Link className="button-link secondary-link" to={`/questions/${question.id}/edit`}>
                  編集
                </Link>
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
