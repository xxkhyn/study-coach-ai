import type {
  AiAdvice,
  AuthResponse,
  Dashboard,
  DailyStudyTime,
  FieldAccuracy,
  LoginRequest,
  Question,
  QuestionAnswerResponse,
  QuestionAttempt,
  QuestionGenerationLog,
  QuestionGenerationRequest,
  QuestionImportResult,
  QuestionLog,
  QuestionLogRequest,
  QuestionRequest,
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

const API_ORIGIN = (import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080')
  .replace(/\/+$/, '')
  .replace(/\/api$/, '');
const API_BASE_URL = API_ORIGIN;

async function request<T>(path: string, options?: RequestInit, requireAuth = true): Promise<T> {
  const token = auth.getToken();
  const isFormData = options?.body instanceof FormData;
  const headers: Record<string, string> = {
    ...(isFormData ? {} : { 'Content-Type': 'application/json' }),
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

function cleanQuestionLog(body: QuestionLogRequest): QuestionLogRequest {
  return {
    ...body,
    practicedDate: cleanDate(body.practicedDate) ?? body.practicedDate,
  };
}

function cleanQuestion(body: QuestionRequest): QuestionRequest {
  return {
    ...body,
    year: body.year || undefined,
    choices: body.choices.map((choice) => choice.trim()),
  };
}

export const api = {
  register: (body: RegisterRequest) =>
    request<AuthResponse>(
      '/api/auth/register',
      {
        method: 'POST',
        body: JSON.stringify(body),
      },
      false,
    ),
  login: (body: LoginRequest) =>
    request<AuthResponse>(
      '/api/auth/login',
      {
        method: 'POST',
        body: JSON.stringify(body),
      },
      false,
    ),
  me: () => request<User>('/api/auth/me'),

  getDashboard: () => request<Dashboard>('/api/dashboard'),
  getDailyStudyTime: () => request<DailyStudyTime[]>('/api/analytics/study-time/daily'),
  getStudyTimeByTarget: () => request<TargetStudyMinutes[]>('/api/analytics/study-time/by-target'),
  getAccuracyByField: () => request<FieldAccuracy[]>('/api/analytics/accuracy/by-field'),
  getWeakFields: () => request<WeakField[]>('/api/analytics/weak-fields'),

  getTodayAiAdvice: () => request<AiAdvice | undefined>('/api/ai/advice/today'),
  listAiAdviceHistory: () => request<AiAdvice[]>('/api/ai/advice/history'),
  generateDailyAiAdvice: (force = false) =>
    request<AiAdvice>('/api/ai/advice/daily', {
      method: 'POST',
      body: JSON.stringify({ force }),
    }),

  listTargets: () => request<StudyTarget[]>('/api/study-targets'),
  getTarget: (id: number) => request<StudyTarget>(`/api/study-targets/${id}`),
  createTarget: (body: StudyTargetRequest) =>
    request<StudyTarget>('/api/study-targets', {
      method: 'POST',
      body: JSON.stringify(cleanTarget(body)),
    }),
  updateTarget: (id: number, body: StudyTargetRequest) =>
    request<StudyTarget>(`/api/study-targets/${id}`, {
      method: 'PUT',
      body: JSON.stringify(cleanTarget(body)),
    }),
  deleteTarget: (id: number) =>
    request<void>(`/api/study-targets/${id}`, {
      method: 'DELETE',
    }),

  listTasks: () => request<StudyTask[]>('/api/study-tasks'),
  getTask: (id: number) => request<StudyTask>(`/api/study-tasks/${id}`),
  createTask: (body: StudyTaskRequest) =>
    request<StudyTask>('/api/study-tasks', {
      method: 'POST',
      body: JSON.stringify(cleanTask(body)),
    }),
  updateTask: (id: number, body: StudyTaskRequest) =>
    request<StudyTask>(`/api/study-tasks/${id}`, {
      method: 'PUT',
      body: JSON.stringify(cleanTask(body)),
    }),
  completeTask: (id: number, completed = true) =>
    request<StudyTask>(`/api/study-tasks/${id}/complete?completed=${completed}`, {
      method: 'PATCH',
    }),
  deleteTask: (id: number) =>
    request<void>(`/api/study-tasks/${id}`, {
      method: 'DELETE',
    }),

  listStudyLogs: () => request<StudyLog[]>('/api/study-logs'),
  getStudyLog: (id: number) => request<StudyLog>(`/api/study-logs/${id}`),
  listStudyLogsByTarget: (studyTargetId: number) => request<StudyLog[]>(`/api/study-logs/by-target/${studyTargetId}`),
  getStudyLogWeeklySummary: () => request<StudyLogWeeklySummary>('/api/study-logs/weekly-summary'),
  createStudyLog: (body: StudyLogRequest) =>
    request<StudyLog>('/api/study-logs', {
      method: 'POST',
      body: JSON.stringify(cleanLog(body)),
    }),
  updateStudyLog: (id: number, body: StudyLogRequest) =>
    request<StudyLog>(`/api/study-logs/${id}`, {
      method: 'PUT',
      body: JSON.stringify(cleanLog(body)),
    }),
  deleteStudyLog: (id: number) =>
    request<void>(`/api/study-logs/${id}`, {
      method: 'DELETE',
    }),

  listQuestionLogs: () => request<QuestionLog[]>('/api/question-logs'),
  getQuestionLog: (id: number) => request<QuestionLog>(`/api/question-logs/${id}`),
  getQuestionLogAccuracyByField: () => request<FieldAccuracy[]>('/api/question-logs/accuracy-by-field'),
  getQuestionLogWeakFields: () => request<WeakField[]>('/api/question-logs/weak-fields'),
  createQuestionLog: (body: QuestionLogRequest) =>
    request<QuestionLog>('/api/question-logs', {
      method: 'POST',
      body: JSON.stringify(cleanQuestionLog(body)),
    }),
  updateQuestionLog: (id: number, body: QuestionLogRequest) =>
    request<QuestionLog>(`/api/question-logs/${id}`, {
      method: 'PUT',
      body: JSON.stringify(cleanQuestionLog(body)),
    }),
  deleteQuestionLog: (id: number) =>
    request<void>(`/api/question-logs/${id}`, {
      method: 'DELETE',
    }),

  listQuestions: () => request<Question[]>('/api/questions'),
  getQuestion: (id: number) => request<Question>(`/api/questions/${id}`),
  getRandomQuestion: () => request<Question>('/api/questions/random'),
  listWrongQuestions: () => request<Question[]>('/api/questions/wrong'),
  createQuestion: (body: QuestionRequest) =>
    request<Question>('/api/questions', {
      method: 'POST',
      body: JSON.stringify(cleanQuestion(body)),
    }),
  importQuestionsCsv: (studyTargetId: number, file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return request<QuestionImportResult>(`/api/questions/import-csv?studyTargetId=${studyTargetId}`, {
      method: 'POST',
      body: formData,
    });
  },
  generateAiQuestions: (body: QuestionGenerationRequest) =>
    request<Question[]>('/api/questions/generate-ai', {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  listQuestionGenerationLogs: () => request<QuestionGenerationLog[]>('/api/question-generation-logs'),
  updateQuestion: (id: number, body: QuestionRequest) =>
    request<Question>(`/api/questions/${id}`, {
      method: 'PUT',
      body: JSON.stringify(cleanQuestion(body)),
    }),
  deleteQuestion: (id: number) =>
    request<void>(`/api/questions/${id}`, {
      method: 'DELETE',
    }),
  answerQuestion: (id: number, selectedIndex: number) =>
    request<QuestionAnswerResponse>(`/api/questions/${id}/answer`, {
      method: 'POST',
      body: JSON.stringify({ selectedIndex }),
    }),
  listQuestionAttempts: () => request<QuestionAttempt[]>('/api/question-attempts'),
  getQuestionAttemptAccuracyByField: () => request<FieldAccuracy[]>('/api/question-attempts/accuracy-by-field'),
};
