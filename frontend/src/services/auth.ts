import type { User } from '../types';

const TOKEN_KEY = 'studyCoachAiToken';
const USER_KEY = 'studyCoachAiUser';

export const auth = {
  getToken: () => localStorage.getItem(TOKEN_KEY),
  getUser: (): User | null => {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as User) : null;
  },
  saveSession: (token: string, user: User) => {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  },
  clearSession: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  },
  isAuthenticated: () => Boolean(localStorage.getItem(TOKEN_KEY)),
};
