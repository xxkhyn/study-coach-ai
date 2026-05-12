import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import EmptyState from '../components/EmptyState';
import { api } from '../services/api';
import type { FieldAccuracy, QuestionLog, WeakField } from '../types';

function formatRate(value: number) {
  return `${Number(value).toFixed(1)}%`;
}

export default function QuestionLogListPage() {
  const [logs, setLogs] = useState<QuestionLog[]>([]);
  const [accuracies, setAccuracies] = useState<FieldAccuracy[]>([]);
  const [weakFields, setWeakFields] = useState<WeakField[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    void loadLogs();
  }, []);

  async function loadLogs() {
    setLoading(true);
    setError('');
    try {
      const [nextLogs, nextAccuracies, nextWeakFields] = await Promise.all([
        api.listQuestionLogs(),
        api.getQuestionLogAccuracyByField(),
        api.getQuestionLogWeakFields(),
      ]);
      setLogs(nextLogs);
      setAccuracies(nextAccuracies);
      setWeakFields(nextWeakFields);
    } catch (err) {
      setError(err instanceof Error ? err.message : '演習ログの取得に失敗しました。');
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(id: number) {
    if (!window.confirm('この演習ログを削除しますか？')) {
      return;
    }
    setError('');
    try {
      await api.deleteQuestionLog(id);
      await loadLogs();
    } catch (err) {
      setError(err instanceof Error ? err.message : '削除に失敗しました。');
    }
  }

  return (
    <section className="panel">
      <div className="section-heading horizontal">
        <div>
          <p className="eyebrow">Question Logs</p>
          <h2>演習ログ一覧</h2>
        </div>
        <Link className="button-link" to="/question-logs/new">
          演習ログを登録
        </Link>
      </div>

      <div className="summary-grid">
        <article className="summary-card">
          <span>演習ログ</span>
          <strong>{logs.length}件</strong>
          <p>{logs[0]?.studyTargetName ?? 'まだデータがありません'}</p>
        </article>
        <article className="summary-card">
          <span>分野別正答率</span>
          <strong>{accuracies.length}件</strong>
          <p>{accuracies[0] ? `${accuracies[0].field}: ${formatRate(accuracies[0].accuracyRate)}` : 'まだデータがありません'}</p>
        </article>
        <article className="summary-card">
          <span>苦手分野</span>
          <strong>{weakFields.length}件</strong>
          <p>{weakFields[0] ? `${weakFields[0].field}: ${formatRate(weakFields[0].accuracyRate)}` : 'まだデータがありません'}</p>
        </article>
      </div>

      {error && <p className="error">{error}</p>}
      {loading ? (
        <p className="muted">読み込み中...</p>
      ) : logs.length === 0 ? (
        <EmptyState title="まだ演習ログがありません" message="解いた問題数と正解数を記録すると、分野別正答率を確認できます。" />
      ) : (
        <div className="item-list">
          {logs.map((log) => (
            <article className="item-card" key={log.id}>
              <div className="card-title-row">
                <div>
                  <h3>{log.studyTargetName || `学習対象ID: ${log.studyTargetId}`}</h3>
                  <p className="muted">{log.memo || 'メモなし'}</p>
                </div>
                <span className="status">{formatRate(log.accuracyRate)}</span>
              </div>
              <dl className="meta-grid">
                <div>
                  <dt>演習日</dt>
                  <dd>{log.practicedDate}</dd>
                </div>
                <div>
                  <dt>分野</dt>
                  <dd>{log.field || '-'}</dd>
                </div>
                <div>
                  <dt>問題数 / 正解数</dt>
                  <dd>
                    {log.solvedCount}問 / {log.correctCount}問
                  </dd>
                </div>
              </dl>
              <div className="button-row">
                <Link className="button-link secondary-link" to={`/question-logs/${log.id}/edit`}>
                  編集
                </Link>
                <button type="button" className="danger" onClick={() => void handleDelete(log.id)}>
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
