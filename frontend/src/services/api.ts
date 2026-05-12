import type {
  AiAdvice,
  AuthResponse,
  Dashboard,
  DailyStudyTime,
  FieldAccuracy,
  LoginRequest,
  RegisterRequest,
  StudyLog,
  StudyLogRequest,
  StudyLogWeeklySummary,
  StudyTarget,
  StudyTargetRequest,
  StudyTask,
  StudyTaskRequest,
  TargetStudyMinutes,
  User,
  WeakField,
} from '../types';
import { auth } from './auth';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api';

async function request<T>(path: string, options?: RequestInit, requireAuth = true): Promise<T> {
  const token = auth.getToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options?.headers as Record<string, string> | undefined),
  };

  if (requireAuth && token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    auth.clearSession();
    if (!window.location.pathname.startsWith('/login') && !window.location.pathname.startsWith('/register')) {
      window.location.assign('/login');
    }
    throw new Error('ログインが必要です。');
  }

  if (!response.ok) {
    const contentType = response.headers.get('content-type') ?? '';
    const message = contentType.includes('application/json')
      ? await response.json().then((body) => body.details?.join(' / ') ?? body.error ?? response.statusText)
      : await response.text();
    throw new Error(message || `API request failed: ${response.status}`);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

const cleanDate = (value?: string) => (value && value.length > 0 ? value : undefined);

function cleanTarget(body: StudyTargetRequest): StudyTargetRequest {
  return {
    ...body,
    targetDate: cleanDate(body.targetDate),
  };
}

function cleanTask(body: StudyTaskRequest): StudyTaskRequest {
  return {
    ...body,
    dueDate: cleanDate(body.dueDate),
  };
}

function cleanLog(body: StudyLogRequest): StudyLogRequest {
  return {
    ...body,
    studiedDate: cleanDate(body.studiedDate) ?? body.studiedDate,
  };
}

export const api = {
  register: (body: RegisterRequest) =>
    request<AuthResponse>(
      '/auth/register',
      {
        method: 'POST',
        body: JSON.stringify(body),
      },
      false,
    ),
  login: (body: LoginRequest) =>
    request<AuthResponse>(
      '/auth/login',
      {
        method: 'POST',
        body: JSON.stringify(body),
      },
      false,
    ),
  me: () => request<User>('/auth/me'),

  getDashboard: () => request<Dashboard>('/dashboard'),
  getDailyStudyTime: () => request<DailyStudyTime[]>('/analytics/study-time/daily'),
  getStudyTimeByTarget: () => request<TargetStudyMinutes[]>('/analytics/study-time/by-target'),
  getAccuracyByField: () => request<FieldAccuracy[]>('/analytics/accuracy/by-field'),
  getWeakFields: () => request<WeakField[]>('/analytics/weak-fields'),

  getTodayAiAdvice: () => request<AiAdvice | undefined>('/ai/advice/today'),
  listAiAdviceHistory: () => request<AiAdvice[]>('/ai/advice/history'),
  generateDailyAiAdvice: (force = false) =>
    request<AiAdvice>('/ai/advice/daily', {
      method: 'POST',
      body: JSON.stringify({ force }),
    }),

  listTargets: () => request<StudyTarget[]>('/study-targets'),
  getTarget: (id: number) => request<StudyTarget>(`/study-targets/${id}`),
  createTarget: (body: StudyTargetRequest) =>
    request<StudyTarget>('/study-targets', {
      method: 'POST',
      body: JSON.stringify(cleanTarget(body)),
    }),
  updateTarget: (id: number, body: StudyTargetRequest) =>
    request<StudyTarget>(`/study-targets/${id}`, {
      method: 'PUT',
      body: JSON.stringify(cleanTarget(body)),
    }),
  deleteTarget: (id: number) =>
    request<void>(`/study-targets/${id}`, {
      method: 'DELETE',
    }),

  listTasks: () => request<StudyTask[]>('/study-tasks'),
  getTask: (id: number) => request<StudyTask>(`/study-tasks/${id}`),
  createTask: (body: StudyTaskRequest) =>
    request<StudyTask>('/study-tasks', {
      method: 'POST',
      body: JSON.stringify(cleanTask(body)),
    }),
  updateTask: (id: number, body: StudyTaskRequest) =>
    request<StudyTask>(`/study-tasks/${id}`, {
      method: 'PUT',
      body: JSON.stringify(cleanTask(body)),
    }),
  completeTask: (id: number, completed = true) =>
    request<StudyTask>(`/study-tasks/${id}/complete?completed=${completed}`, {
      method: 'PATCH',
    }),
  deleteTask: (id: number) =>
    request<void>(`/study-tasks/${id}`, {
      method: 'DELETE',
    }),

  listStudyLogs: () => request<StudyLog[]>('/study-logs'),
  getStudyLog: (id: number) => request<StudyLog>(`/study-logs/${id}`),
  listStudyLogsByTarget: (studyTargetId: number) => request<StudyLog[]>(`/study-logs/by-target/${studyTargetId}`),
  getStudyLogWeeklySummary: () => request<StudyLogWeeklySummary>('/study-logs/weekly-summary'),
  createStudyLog: (body: StudyLogRequest) =>
    request<StudyLog>('/study-logs', {
      method: 'POST',
      body: JSON.stringify(cleanLog(body)),
    }),
  updateStudyLog: (id: number, body: StudyLogRequest) =>
    request<StudyLog>(`/study-logs/${id}`, {
      method: 'PUT',
      body: JSON.stringify(cleanLog(body)),
    }),
  deleteStudyLog: (id: number) =>
    request<void>(`/study-logs/${id}`, {
      method: 'DELETE',
    }),
};
