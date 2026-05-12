import { useEffect, useMemo, useState } from 'react';
import BarChart from '../components/BarChart';
import DonutChart from '../components/DonutChart';
import EmptyState from '../components/EmptyState';
import { api } from '../services/api';
import type { Analytics, DailyStudyTime, FieldAccuracy, TargetStudyMinutes, WeakField } from '../types';

const chartColors = ['#23674f', '#dfbd40', '#4b7f9f', '#9f5b4f', '#6d6aa8', '#6f8652'];

const minutesLabel = (minutes: number) => {
  const hours = Math.floor(minutes / 60);
  const rest = minutes % 60;
  if (hours === 0) return `${rest}分`;
  if (rest === 0) return `${hours}時間`;
  return `${hours}時間${rest}分`;
};

const percentLabel = (value: number) => `${Number(value).toFixed(1)}%`;
const targetLabel = (value?: string | null) => value ?? '学習対象未設定';

function toDailyBars(items: DailyStudyTime[]) {
  return items.map((item) => ({
    label: item.studiedDate.slice(5),
    value: item.totalMinutes,
    valueLabel: minutesLabel(item.totalMinutes),
  }));
}

function toTargetBars(items: TargetStudyMinutes[]) {
  return items.map((item) => ({
    label: targetLabel(item.studyTargetName),
    value: item.totalMinutes,
    valueLabel: minutesLabel(item.totalMinutes),
  }));
}

function toAccuracyBars(items: FieldAccuracy[]) {
  return items.map((item) => ({
    label: item.field,
    value: Number(item.accuracyRate),
    valueLabel: `${percentLabel(item.accuracyRate)} (${item.correctCount}/${item.solvedCount}問)`,
  }));
}

function WeakFieldList({ items }: { items: WeakField[] }) {
  if (items.length === 0) {
    return <EmptyState title="まだデータがありません" message="演習ログを登録すると苦手分野が表示されます。" />;
  }

  return (
    <div className="rank-list">
      {items.map((item, index) => (
        <article className="rank-item" key={item.field}>
          <span>{index + 1}</span>
          <div>
            <h3>{item.field}</h3>
            <p className="muted">
              正答率 {percentLabel(item.accuracyRate)} / {item.correctCount}/{item.solvedCount}問
            </p>
          </div>
        </article>
      ))}
    </div>
  );
}

export default function AnalyticsPage() {
  const [analytics, setAnalytics] = useState<Analytics | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([
      api.getDailyStudyTime(),
      api.getStudyTimeByTarget(),
      api.getAccuracyByField(),
      api.getWeakFields(),
    ])
      .then(([dailyStudyTime, studyTimeByTarget, accuracyByField, weakFields]) => {
        setAnalytics({ dailyStudyTime, studyTimeByTarget, accuracyByField, weakFields });
      })
      .catch((err: Error) => setError(err.message))
      .finally(() => setIsLoading(false));
  }, []);

  const targetDonutItems = useMemo(
    () =>
      (analytics?.studyTimeByTarget ?? []).map((item, index) => ({
        label: targetLabel(item.studyTargetName),
        value: item.totalMinutes,
        valueLabel: minutesLabel(item.totalMinutes),
        color: chartColors[index % chartColors.length],
      })),
    [analytics],
  );

  if (isLoading) {
    return <section className="panel">読み込み中...</section>;
  }

  if (error) {
    return <div className="error">{error}</div>;
  }

  if (!analytics) {
    return <EmptyState title="まだデータがありません" message="分析データを表示できませんでした。" />;
  }

  return (
    <div className="dashboard">
      <section className="panel">
        <div className="section-heading">
          <p className="eyebrow">Analytics</p>
          <h2>学習分析</h2>
          <p className="muted">学習時間と正答率を視覚的に確認できます。</p>
        </div>
      </section>

      <section className="dashboard-grid">
        <div className="panel">
          <div className="section-heading">
            <p className="eyebrow">Daily</p>
            <h2>日別学習時間</h2>
          </div>
          <BarChart items={toDailyBars(analytics.dailyStudyTime)} emptyMessage="学習ログを登録すると日別の棒グラフが表示されます。" />
        </div>

        <div className="panel">
          <div className="section-heading">
            <p className="eyebrow">Targets</p>
            <h2>学習対象別の学習時間</h2>
          </div>
          <DonutChart items={targetDonutItems} emptyMessage="学習ログを登録すると対象別の比率が表示されます。" />
        </div>
      </section>

      <section className="dashboard-grid">
        <div className="panel">
          <div className="section-heading">
            <p className="eyebrow">Accuracy</p>
            <h2>分野別正答率</h2>
          </div>
          <BarChart items={toAccuracyBars(analytics.accuracyByField)} emptyMessage="演習ログを登録すると分野別正答率が表示されます。" />
        </div>

        <div className="panel">
          <div className="section-heading">
            <p className="eyebrow">Weak Fields</p>
            <h2>苦手分野ランキング</h2>
          </div>
          <WeakFieldList items={analytics.weakFields} />
        </div>
      </section>

      <section className="panel">
        <div className="section-heading">
          <p className="eyebrow">Breakdown</p>
          <h2>学習対象別の学習時間</h2>
        </div>
        <BarChart items={toTargetBars(analytics.studyTimeByTarget)} emptyMessage="学習ログを登録すると対象別の学習時間が表示されます。" />
      </section>
    </div>
  );
}
