import { FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import { auth } from '../services/auth';

export default function RegisterPage() {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    setIsSubmitting(true);
    try {
      const response = await api.register({ username, email, password });
      auth.saveSession(response.token, response.user);
      navigate('/dashboard', { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'ユーザー登録に失敗しました。');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="auth-page">
      <section className="panel auth-card">
        <p className="eyebrow">Study Coach AI</p>
        <h1>ユーザー登録</h1>
        <p className="muted">自分の学習データだけを管理できるアカウントを作成します。</p>
        {error ? <div className="error">{error}</div> : null}
        <form className="form-stack" onSubmit={handleSubmit}>
          <label>
            ユーザー名
            <input value={username} onChange={(event) => setUsername(event.target.value)} required maxLength={80} />
          </label>
          <label>
            メールアドレス
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
              maxLength={160}
            />
          </label>
          <label>
            パスワード
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
              minLength={8}
            />
          </label>
          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? '登録中...' : '登録する'}
          </button>
        </form>
        <p className="auth-switch">
          すでにアカウントがある場合は <Link to="/login">ログイン</Link>
        </p>
      </section>
    </main>
  );
}
