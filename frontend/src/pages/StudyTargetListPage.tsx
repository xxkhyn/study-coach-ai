import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import EmptyState from '../components/EmptyState';
import { api } from '../services/api';
import type { StudyTarget } from '../types';

function getRemainingDays(targetDate?: string | null) {
  if (!targetDate) return null;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const target = new Date(`${targetDate}T00:00:00`);
  return Math.ceil((target.getTime() - today.getTime()) / 86_400_000);
}

export default function StudyTargetListPage() {
  const [targets, setTargets] = useState<StudyTarget[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    void loadTargets();
  }, []);

  async function loadTargets() {
    setLoading(true);
    setError('');
    try {
      setTargets(await api.listTargets());
    } catch (err) {
      setError(err instanceof Error ? err.message : '学習対象の取得に失敗しました。');
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(id: number) {
    if (!window.confirm('この学習対象を削除しますか？関連するタスクやログがある場合は先に削除してください。')) {
      return;
    }
    setError('');
    try {
      await api.deleteTarget(id);
      await loadTargets();
    } catch (err) {
      setError(err instanceof Error ? err.message : '削除に失敗しました。');
    }
  }

  return (
    <section className="panel">
      <div className="section-heading horizontal">
        <div>
          <p className="eyebrow">Study Targets</p>
          <h2>学習対象一覧</h2>
        </div>
        <Link className="button-link" to="/targets/new">
          学習対象を登録
        </Link>
      </div>

      {error && <p className="error">{error}</p>}
      {loading ? (
        <p className="muted">読み込み中...</p>
      ) : targets.length === 0 ? (
        <EmptyState title="まだ登録がありません" message="資格や技術テーマを登録しましょう。" />
      ) : (
        <div className="item-list">
          {targets.map((target) => {
            const remainingDays = getRemainingDays(target.targetDate);
            return (
              <article className="item-card" key={target.id}>
                <div className="card-title-row">
                  <div>
                    <h3>{target.name}</h3>
                    <p className="muted">{target.description || '説明なし'}</p>
                  </div>
                  {remainingDays !== null && (
                    <span className={remainingDays < 0 ? 'status danger-status' : 'status'}>
                      {remainingDays < 0 ? `${Math.abs(remainingDays)}日経過` : `あと${remainingDays}日`}
                    </span>
                  )}
                </div>
                <dl className="meta-grid two-cols">
                  <div>
                    <dt>目標日</dt>
                    <dd>{target.targetDate || '-'}</dd>
                  </div>
                  <div>
                    <dt>更新日</dt>
                    <dd>{new Date(target.updatedAt).toLocaleDateString('ja-JP')}</dd>
                  </div>
                </dl>
                <div className="button-row">
                  <Link className="button-link secondary-link" to={`/targets/${target.id}/edit`}>
                    編集
                  </Link>
                  <button type="button" className="danger" onClick={() => void handleDelete(target.id)}>
                    削除
                  </button>
                </div>
              </article>
            );
          })}
        </div>
      )}
    </section>
  );
}
