import EmptyState from './EmptyState';

export type BarChartItem = {
  label: string;
  value: number;
  valueLabel?: string;
};

type BarChartProps = {
  items: BarChartItem[];
  emptyMessage: string;
};

export default function BarChart({ items, emptyMessage }: BarChartProps) {
  if (items.length === 0) {
    return <EmptyState title="まだデータがありません" message={emptyMessage} />;
  }

  const maxValue = Math.max(1, ...items.map((item) => item.value));

  return (
    <div className="chart-bars">
      {items.map((item) => (
        <article className="chart-bar-row" key={item.label}>
          <div className="chart-bar-label">
            <strong>{item.label}</strong>
            <span>{item.valueLabel ?? item.value}</span>
          </div>
          <div className="chart-bar-track">
            <div className="chart-bar-fill" style={{ width: `${Math.max(4, (item.value / maxValue) * 100)}%` }} />
          </div>
        </article>
      ))}
    </div>
  );
}
