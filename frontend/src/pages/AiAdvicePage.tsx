import { useEffect, useState } from 'react';
import EmptyState from '../components/EmptyState';
import { api } from '../services/api';
import type { AiAdvice } from '../types';

const minutesLabel = (minutes: number) => `${minutes}分`;

function AdviceDetail({ advice }: { advice: AiAdvice }) {
  return (
    <div className="advice-detail">
      <article className="panel advice-summary">
        <p className="eyebrow">{advice.adviceDate}</p>
        <h2>今日のAIアドバイス</h2>
        <p>{advice.summary}</p>
      </article>

      <section className="dashboard-grid">
        <div className="panel">
          <div className="section-heading">
            <p className="eyebrow">Menu</p>
            <h2>推奨タスク</h2>
          </div>
          {advice.tasks.length === 0 ? (
            <EmptyState title="まだデータがありません" message="推奨タスクがありません。" />
          ) : (
            <div className="mini-list">
              {advice.tasks.map((task, index) => (
                <article className="mini-item" key={`${task.title}-${index}`}>
                  <div>
                    <h3>{task.title}</h3>
                    <p className="muted">{task.reason}</p>
                  </div>
                  <span className="pill">{minutesLabel(task.minutes)}</span>
                </article>
              ))}
            </div>
          )}
        </div>

        <div className="panel">
          <div className="section-heading">
            <p className="eyebrow">Weak Points</p>
            <h2>苦手分野へのアドバイス</h2>
          </div>
          {advice.weakPoints.length === 0 ? (
            <EmptyState title="まだデータがありません" message="苦手分野へのアドバイスがありません。" />
          ) : (
            <div className="mini-list">
              {advice.weakPoints.map((weakPoint, index) => (
                <article className="item-card" key={`${weakPoint.field}-${index}`}>
                  <h3>{weakPoint.field}</h3>
                  <p className="muted">{weakPoint.advice}</p>
                </article>
              ))}
            </div>
          )}
        </div>
      </section>

      <article className="panel">
        <div className="section-heading">
          <p className="eyebrow">Overall</p>
          <h2>全体アドバイス</h2>
        </div>
        <p className="muted">{advice.overallAdvice}</p>
      </article>
    </div>
  );
}

export default function AiAdvicePage() {
  const [todayAdvice, setTodayAdvice] = useState<AiAdvice | null>(null);
  const [history, setHistory] = useState<AiAdvice[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isGenerating, setIsGenerating] = useState(false);
  const [error, setError] = useState('');

  const loadAdvice = async () => {
    setError('');
    const [today, adviceHistory] = await Promise.all([api.getTodayAiAdvice(), api.listAiAdviceHistory()]);
    setTodayAdvice(today ?? null);
    setHistory(adviceHistory);
  };

  useEffect(() => {
    loadAdvice()
      .catch((err: Error) => setError(err.message))
      .finally(() => setIsLoading(false));
  }, []);

  const handleGenerate = async () => {
    setIsGenerating(true);
    setError('');
    try {
      const advice = await api.generateDailyAiAdvice(false);
      setTodayAdvice(advice);
      setHistory(await api.listAiAdviceHistory());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'AIアドバイスの生成に失敗しました。');
    } finally {
      setIsGenerating(false);
    }
  };

  if (isLoading) {
    return <section className="panel">読み込み中...</section>;
  }

  return (
    <div className="dashboard">
      <section className="panel">
        <div className="section-heading horizontal">
          <div>
            <p className="eyebrow">AI Coach</p>
            <h2>AIアドバイス</h2>
            <p className="muted">学習ログ、未完了タスク、正答率から今日の勉強メニューを生成します。</p>
          </div>
          <button onClick={handleGenerate} disabled={isGenerating}>
            {isGenerating ? '生成中...' : todayAdvice ? '今日のアドバイスを表示' : 'アドバイス生成'}
          </button>
        </div>
        {error ? <div className="error">{error}</div> : null}
        {!todayAdvice && !error ? (
          <EmptyState title="まだデータがありません" message="アドバイス生成ボタンを押すと、今日の勉強メニューを作成します。" />
        ) : null}
      </section>

      {todayAdvice ? <AdviceDetail advice={todayAdvice} /> : null}

      <section className="panel">
        <div className="section-heading">
          <p className="eyebrow">History</p>
          <h2>過去のアドバイス履歴</h2>
        </div>
        {history.length === 0 ? (
          <EmptyState title="まだデータがありません" message="生成したAIアドバイスが履歴として表示されます。" />
        ) : (
          <div className="mini-list">
            {history.map((advice) => (
              <article className="mini-item" key={advice.id}>
                <div>
                  <h3>{advice.adviceDate}</h3>
                  <p className="muted">{advice.summary}</p>
                </div>
                <span className="pill">{advice.tasks.length}件</span>
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}
