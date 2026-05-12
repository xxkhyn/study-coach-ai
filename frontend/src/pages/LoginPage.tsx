import { FormEvent, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import { auth } from '../services/auth';

export default function LoginPage() {
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname ?? '/dashboard';

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    setIsSubmitting(true);
    try {
      const response = await api.login({ usernameOrEmail, password });
      auth.saveSession(response.token, response.user);
      navigate(from, { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'ログインに失敗しました。');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="auth-page">
      <section className="panel auth-card">
        <p className="eyebrow">Study Coach AI</p>
        <h1>ログイン</h1>
        <p className="muted">学習データにアクセスするにはログインしてください。</p>
        {error ? <div className="error">{error}</div> : null}
        <form className="form-stack" onSubmit={handleSubmit}>
          <label>
            ユーザー名またはメールアドレス
            <input value={usernameOrEmail} onChange={(event) => setUsernameOrEmail(event.target.value)} required />
          </label>
          <label>
            パスワード
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </label>
          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'ログイン中...' : 'ログイン'}
          </button>
        </form>
        <p className="auth-switch">
          アカウントがない場合は <Link to="/register">ユーザー登録</Link>
        </p>
      </section>
    </main>
  );
}
