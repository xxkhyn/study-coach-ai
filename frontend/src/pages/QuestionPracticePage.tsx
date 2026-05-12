import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import EmptyState from '../components/EmptyState';
import { api } from '../services/api';
import type { Question, QuestionAnswerResponse } from '../types';

const choiceLabels = ['ア', 'イ', 'ウ', 'エ'];

export default function QuestionPracticePage() {
  const [question, setQuestion] = useState<Question | null>(null);
  const [selectedIndex, setSelectedIndex] = useState<number | null>(null);
  const [result, setResult] = useState<QuestionAnswerResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [answering, setAnswering] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    void loadRandomQuestion();
  }, []);

  async function loadRandomQuestion() {
    setLoading(true);
    setError('');
    setResult(null);
    setSelectedIndex(null);
    try {
      setQuestion(await api.getRandomQuestion());
    } catch (err) {
      setQuestion(null);
      setError(err instanceof Error ? err.message : '問題の取得に失敗しました。');
    } finally {
      setLoading(false);
    }
  }

  async function handleAnswer() {
    if (!question || selectedIndex === null) {
      setError('選択肢を選んでください。');
      return;
    }

    setAnswering(true);
    setError('');
    try {
      setResult(await api.answerQuestion(question.id, selectedIndex));
    } catch (err) {
      setError(err instanceof Error ? err.message : '採点に失敗しました。');
    } finally {
      setAnswering(false);
    }
  }

  return (
    <section className="panel narrow-panel">
      <div className="section-heading horizontal">
        <div>
          <p className="eyebrow">Practice</p>
          <h2>問題演習</h2>
        </div>
        <div className="heading-actions">
          <button className="secondary" type="button" onClick={() => void loadRandomQuestion()}>
            次の問題
          </button>
          <Link className="button-link secondary-link" to="/questions">
            一覧へ
          </Link>
        </div>
      </div>

      {error && <p className={question ? 'error' : 'notice'}>{error}</p>}
      {loading ? (
        <p className="muted">読み込み中...</p>
      ) : !question ? (
        <EmptyState title="演習できる問題がありません" message="問題を登録するとランダム演習できます。" />
      ) : (
        <div className="practice-card">
          <div>
            <p className="muted">
              {question.studyTargetName ?? '学習対象未設定'} / {question.field || '分野未設定'}
            </p>
            <h3>{question.questionText}</h3>
          </div>

          <div className="choice-list">
            {question.choices.map((choice, index) => {
              const isCorrect = result && index === result.answerIndex;
              const isSelectedWrong = result && index === result.selectedIndex && !result.correct;
              return (
                <button
                  className={[
                    'choice-option',
                    selectedIndex === index ? 'is-selected' : '',
                    isCorrect ? 'is-correct' : '',
                    isSelectedWrong ? 'is-wrong' : '',
                  ].join(' ')}
                  disabled={Boolean(result)}
                  key={`${choice}-${index}`}
                  onClick={() => setSelectedIndex(index)}
                  type="button"
                >
                  <span>{choiceLabels[index]}</span>
                  <strong>{choice}</strong>
                </button>
              );
            })}
          </div>

          {!result && (
            <button type="button" disabled={answering || selectedIndex === null} onClick={() => void handleAnswer()}>
              {answering ? '採点中...' : '回答する'}
            </button>
          )}

          {result && (
            <div className={result.correct ? 'answer-result is-correct' : 'answer-result is-wrong'}>
              <h3>{result.correct ? '正解です' : '不正解です'}</h3>
              <p>
                正解: {choiceLabels[result.answerIndex]} / あなたの回答: {choiceLabels[result.selectedIndex]}
              </p>
              {result.explanation && (
                <>
                  <p className="eyebrow">解説</p>
                  <p>{result.explanation}</p>
                </>
              )}
            </div>
          )}

          <div className="source-info">
            <p className="eyebrow">出典</p>
            <p>
              {question.sourceLabel || question.sourceType}
              {question.sourceUrl && (
                <>
                  {' '}
                  / <a href={question.sourceUrl} rel="noreferrer" target="_blank">URL</a>
                </>
              )}
            </p>
          </div>
        </div>
      )}
    </section>
  );
}
