import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import EmptyState from '../components/EmptyState';
import { api } from '../services/api';
import type { StudyLog, StudyLogWeeklySummary } from '../types';

function formatMinutes(minutes: number) {
  if (minutes < 60) {
    return `${minutes}分`;
  }
  const hours = Math.floor(minutes / 60);
  const rest = minutes % 60;
  return rest === 0 ? `${hours}時間` : `${hours}時間${rest}分`;
}

export default function StudyLogListPage() {
  const [logs, setLogs] = useState<StudyLog[]>([]);
  const [summary, setSummary] = useState<StudyLogWeeklySummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    void loadLogs();
  }, []);

  async function loadLogs() {
    setLoading(true);
    setError('');
    try {
      const [nextLogs, nextSummary] = await Promise.all([api.listStudyLogs(), api.getStudyLogWeeklySummary()]);
      setLogs(nextLogs);
      setSummary(nextSummary);
    } catch (err) {
      setError(err instanceof Error ? err.message : '学習ログの取得に失敗しました。');
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(id: number) {
    if (!window.confirm('この学習ログを削除しますか？')) {
      return;
    }
    setError('');
    try {
      await api.deleteStudyLog(id);
      await loadLogs();
    } catch (err) {
      setError(err instanceof Error ? err.message : '削除に失敗しました。');
    }
  }

  return (
    <section className="panel">
      <div className="section-heading horizontal">
        <div>
          <p className="eyebrow">Study Logs</p>
          <h2>学習ログ一覧</h2>
        </div>
        <Link className="button-link" to="/logs/new">
          学習ログを登録
        </Link>
      </div>

      {summary && (
        <div className="summary-grid">
          <article className="summary-card">
            <span>今週の合計</span>
            <strong>{formatMinutes(summary.totalMinutes)}</strong>
            <p>
              {summary.weekStart} - {summary.weekEnd}
            </p>
          </article>
          <article className="summary-card">
            <span>学習対象別</span>
            <strong>{summary.targetSummaries.length}件</strong>
            <p>{summary.targetSummaries[0]?.studyTargetName ?? 'まだ集計対象がありません'}</p>
          </article>
          <article className="summary-card">
            <span>分野別</span>
            <strong>{summary.fieldSummaries.length}件</strong>
            <p>{summary.fieldSummaries[0]?.field ?? 'まだ集計対象がありません'}</p>
          </article>
        </div>
      )}

      {error && <p className="error">{error}</p>}
      {loading ? (
        <p className="muted">読み込み中...</p>
      ) : logs.length === 0 ? (
        <EmptyState title="まだ学習ログがありません" message="実際に勉強した時間を記録しましょう。" />
      ) : (
        <div className="item-list">
          {logs.map((log) => (
            <article className="item-card" key={log.id}>
              <div className="card-title-row">
                <div>
                  <h3>{log.studyTargetName || `学習対象ID: ${log.studyTargetId}`}</h3>
                  <p className="muted">{log.memo || 'メモなし'}</p>
                </div>
                <span className="status">{formatMinutes(log.minutes)}</span>
              </div>
              <dl className="meta-grid">
                <div>
                  <dt>学習日</dt>
                  <dd>{log.studiedDate}</dd>
                </div>
                <div>
                  <dt>分野</dt>
                  <dd>{log.field || '-'}</dd>
                </div>
                <div>
                  <dt>学習時間</dt>
                  <dd>{log.minutes}分</dd>
                </div>
              </dl>
              <div className="button-row">
                <Link className="button-link secondary-link" to={`/logs/${log.id}/edit`}>
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
