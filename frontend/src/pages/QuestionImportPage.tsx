import { FormEvent, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../services/api';
import type { QuestionImportResult, StudyTarget } from '../types';

const sampleCsv = `examType,year,season,timeCategory,questionNumber,field,difficulty,questionText,choiceA,choiceB,choiceC,choiceD,answer,explanation,sourceType,sourceLabel,sourceUrl
応用情報技術者試験,2026,春期,午前,問1,ネットワーク,標準,サブネットマスクの説明として正しいものはどれか,ネットワーク部とホスト部を識別する,CPU使用率を測る,暗号鍵を生成する,DNS名を登録する,ア,サブネットマスクはIPアドレスのネットワーク部とホスト部を識別します,USER_CREATED,ダミー問題,`;

export default function QuestionImportPage() {
  const [targets, setTargets] = useState<StudyTarget[]>([]);
  const [studyTargetId, setStudyTargetId] = useState(0);
  const [file, setFile] = useState<File | null>(null);
  const [result, setResult] = useState<QuestionImportResult | null>(null);
  const [loading, setLoading] = useState(true);
  const [importing, setImporting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    void loadTargets();
  }, []);

  async function loadTargets() {
    setLoading(true);
    setError('');
    try {
      const nextTargets = await api.listTargets();
      setTargets(nextTargets);
      setStudyTargetId(nextTargets[0]?.id ?? 0);
    } catch (err) {
      setError(err instanceof Error ? err.message : '学習対象の取得に失敗しました。');
    } finally {
      setLoading(false);
    }
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (studyTargetId <= 0) {
      setError('学習対象を選択してください。');
      return;
    }
    if (!file) {
      setError('CSVファイルを選択してください。');
      return;
    }

    setImporting(true);
    setError('');
    setResult(null);
    try {
      setResult(await api.importQuestionsCsv(studyTargetId, file));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'CSVインポートに失敗しました。');
    } finally {
      setImporting(false);
    }
  }

  return (
    <section className="page-grid">
      <div className="panel sticky-panel">
        <div className="section-heading">
          <p className="eyebrow">CSV Import</p>
          <h2>問題CSVインポート</h2>
        </div>

        {error && <p className="error">{error}</p>}
        {loading ? (
          <p className="muted">読み込み中...</p>
        ) : (
          <form className="form-stack" onSubmit={handleSubmit}>
            {targets.length === 0 && <p className="notice">先に学習対象を登録してください。</p>}
            <label>
              学習対象
              <select value={studyTargetId} onChange={(event) => setStudyTargetId(Number(event.target.value))} required>
                <option value={0}>選択してください</option>
                {targets.map((target) => (
                  <option key={target.id} value={target.id}>
                    {target.name}
                  </option>
                ))}
              </select>
            </label>
            <label>
              CSVファイル
              <input
                accept=".csv,text/csv"
                type="file"
                onChange={(event) => setFile(event.target.files?.[0] ?? null)}
              />
            </label>
            <div className="button-row">
              <button type="submit" disabled={importing || targets.length === 0}>
                {importing ? 'インポート中...' : 'インポート'}
              </button>
              <Link className="button-link secondary-link" to="/questions">
                問題一覧へ
              </Link>
            </div>
          </form>
        )}

        {result && (
          <div className="import-result">
            <h3>インポート結果</h3>
            <dl className="meta-grid">
              <div>
                <dt>登録</dt>
                <dd>{result.importedCount}件</dd>
              </div>
              <div>
                <dt>スキップ</dt>
                <dd>{result.skippedCount}件</dd>
              </div>
              <div>
                <dt>エラー</dt>
                <dd>{result.errorCount}件</dd>
              </div>
            </dl>
            {result.errors.length > 0 && (
              <ul className="error-list">
                {result.errors.map((message) => (
                  <li key={message}>{message}</li>
                ))}
              </ul>
            )}
          </div>
        )}
      </div>

      <div className="panel">
        <div className="section-heading">
          <p className="eyebrow">Format</p>
          <h2>CSV形式</h2>
        </div>
        <p className="muted">
          `answer` は ア/イ/ウ/エ または 0/1/2/3 に対応しています。`sourceType` が空の場合は USER_CREATED として登録されます。
        </p>
        <pre className="sample-csv">{sampleCsv}</pre>
      </div>
    </section>
  );
}
