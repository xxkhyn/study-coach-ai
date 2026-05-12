import EmptyState from './EmptyState';

export type DonutChartItem = {
  label: string;
  value: number;
  valueLabel?: string;
  color: string;
};

type DonutChartProps = {
  items: DonutChartItem[];
  emptyMessage: string;
};

export default function DonutChart({ items, emptyMessage }: DonutChartProps) {
  if (items.length === 0) {
    return <EmptyState title="まだデータがありません" message={emptyMessage} />;
  }

  const total = items.reduce((sum, item) => sum + item.value, 0);
  if (total === 0) {
    return <EmptyState title="まだデータがありません" message={emptyMessage} />;
  }

  let cursor = 0;
  const gradient = items
    .map((item) => {
      const start = cursor;
      const end = cursor + (item.value / total) * 100;
      cursor = end;
      return `${item.color} ${start}% ${end}%`;
    })
    .join(', ');

  return (
    <div className="donut-chart-wrap">
      <div className="donut-chart" style={{ background: `conic-gradient(${gradient})` }}>
        <div>
          <strong>{total}</strong>
          <span>分</span>
        </div>
      </div>
      <div className="donut-legend">
        {items.map((item) => (
          <div className="donut-legend-item" key={item.label}>
            <span style={{ background: item.color }} />
            <strong>{item.label}</strong>
            <em>{item.valueLabel ?? item.value}</em>
          </div>
        ))}
      </div>
    </div>
  );
}
