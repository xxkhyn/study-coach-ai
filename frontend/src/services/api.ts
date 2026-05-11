import type { Dashboard, StudyTarget, StudyTargetRequest, StudyTask, StudyTaskRequest } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api';
const DEFAULT_USER_ID = 1;

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
    ...options,
  });

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

const withUser = (path: string) => `${path}${path.includes('?') ? '&' : '?'}userId=${DEFAULT_USER_ID}`;
const cleanDate = (value?: string) => (value && value.length > 0 ? value : undefined);

function cleanTarget(body: StudyTargetRequest): StudyTargetRequest {
  return {
    ...body,
    examDate: cleanDate(body.examDate),
    goalDate: cleanDate(body.goalDate),
  };
}

function cleanTask(body: StudyTaskRequest): StudyTaskRequest {
  return {
    ...body,
    dueDate: cleanDate(body.dueDate),
  };
}

export const api = {
  listTargets: () => request<StudyTarget[]>(withUser('/study-targets')),
  createTarget: (body: StudyTargetRequest) =>
    request<StudyTarget>(withUser('/study-targets'), {
      method: 'POST',
      body: JSON.stringify(cleanTarget(body)),
    }),
  updateTarget: (id: number, body: StudyTargetRequest) =>
    request<StudyTarget>(withUser(`/study-targets/${id}`), {
      method: 'PUT',
      body: JSON.stringify(cleanTarget(body)),
    }),
  deleteTarget: (id: number) =>
    request<void>(withUser(`/study-targets/${id}`), {
      method: 'DELETE',
    }),

  listTasks: () => request<StudyTask[]>(withUser('/study-tasks')),
  createTask: (body: StudyTaskRequest) =>
    request<StudyTask>(withUser('/study-tasks'), {
      method: 'POST',
      body: JSON.stringify(cleanTask(body)),
    }),
  updateTask: (id: number, body: StudyTaskRequest) =>
    request<StudyTask>(withUser(`/study-tasks/${id}`), {
      method: 'PUT',
      body: JSON.stringify(cleanTask(body)),
    }),
  deleteTask: (id: number) =>
    request<void>(withUser(`/study-tasks/${id}`), {
      method: 'DELETE',
    }),

  getDashboard: () => request<Dashboard>(withUser('/dashboard')),
};
