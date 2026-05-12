export type StudyTarget = {
  id: number;
  userId: number;
  name: string;
  description?: string | null;
  targetDate?: string | null;
  createdAt: string;
  updatedAt: string;
};

export type User = {
  id: number;
  username: string;
  email: string;
  createdAt: string;
  updatedAt: string;
};

export type RegisterRequest = {
  username: string;
  email: string;
  password: string;
};

export type LoginRequest = {
  usernameOrEmail: string;
  password: string;
};

export type AuthResponse = {
  token: string;
  user: User;
};

export type StudyTargetRequest = {
  name: string;
  description?: string;
  targetDate?: string;
};

export type StudyTask = {
  id: number;
  userId: number;
  studyTargetId: number;
  studyTargetName?: string | null;
  title: string;
  field?: string | null;
  plannedMinutes?: number | null;
  dueDate?: string | null;
  completed: boolean;
  createdAt: string;
  updatedAt: string;
};

export type StudyTaskRequest = {
  studyTargetId: number;
  title: string;
  field?: string;
  plannedMinutes?: number;
  dueDate?: string;
  completed: boolean;
};

export type StudyLog = {
  id: number;
  userId: number;
  studyTargetId: number;
  studyTargetName?: string | null;
  field?: string | null;
  studiedDate: string;
  minutes: number;
  memo?: string | null;
  createdAt: string;
  updatedAt: string;
};

export type StudyLogRequest = {
  studyTargetId: number;
  field?: string;
  studiedDate: string;
  minutes: number;
  memo?: string;
};

export type TargetStudyMinutes = {
  studyTargetId: number;
  studyTargetName?: string | null;
  totalMinutes: number;
};

export type FieldStudyMinutes = {
  field: string;
  totalMinutes: number;
};

export type StudyLogWeeklySummary = {
  weekStart: string;
  weekEnd: string;
  totalMinutes: number;
  targetSummaries: TargetStudyMinutes[];
  fieldSummaries: FieldStudyMinutes[];
};

export type DailyStudyTime = {
  studiedDate: string;
  totalMinutes: number;
};

export type QuestionLog = {
  id: number;
  userId: number;
  studyTargetId: number;
  studyTargetName?: string | null;
  field?: string | null;
  practicedDate: string;
  solvedCount: number;
  correctCount: number;
  accuracyRate: number;
  memo?: string | null;
  createdAt: string;
  updatedAt: string;
};

export type QuestionLogRequest = {
  studyTargetId: number;
  field?: string;
  practicedDate: string;
  solvedCount: number;
  correctCount: number;
  memo?: string;
};

export type FieldAccuracy = {
  field: string;
  solvedCount: number;
  correctCount: number;
  accuracyRate: number;
};

export type WeakField = FieldAccuracy;

export type Dashboard = {
  todayTasks: StudyTask[];
  overdueTasks: StudyTask[];
  weeklyStudySummary: StudyLogWeeklySummary;
  fieldAccuracies: FieldAccuracy[];
  weakFields: WeakField[];
  recentStudyLogs: StudyLog[];
  recentQuestionLogs: QuestionLog[];
};

export type Analytics = {
  dailyStudyTime: DailyStudyTime[];
  studyTimeByTarget: TargetStudyMinutes[];
  accuracyByField: FieldAccuracy[];
  weakFields: WeakField[];
};

export type AiAdviceTask = {
  title: string;
  minutes: number;
  reason: string;
};

export type AiAdviceWeakPoint = {
  field: string;
  advice: string;
};

export type AiAdvice = {
  id: number;
  userId: number;
  adviceDate: string;
  summary: string;
  tasks: AiAdviceTask[];
  weakPoints: AiAdviceWeakPoint[];
  overallAdvice: string;
  createdAt: string;
};

export type QuestionSourceType = 'IPA_PAST_EXAM' | 'AI_GENERATED' | 'USER_CREATED' | 'PRIVATE_NOTE';

export type Question = {
  id: number;
  userId: number;
  studyTargetId: number;
  studyTargetName?: string | null;
  examType?: string | null;
  year?: number | null;
  season?: string | null;
  timeCategory?: string | null;
  questionNumber?: string | null;
  field?: string | null;
  difficulty?: string | null;
  questionText: string;
  choices: string[];
  answerIndex: number;
  explanation?: string | null;
  sourceType: QuestionSourceType;
  sourceLabel?: string | null;
  sourceUrl?: string | null;
  createdAt: string;
  updatedAt: string;
};

export type QuestionRequest = {
  studyTargetId: number;
  examType?: string;
  year?: number;
  season?: string;
  timeCategory?: string;
  questionNumber?: string;
  field?: string;
  difficulty?: string;
  questionText: string;
  choices: string[];
  answerIndex: number;
  explanation?: string;
  sourceType: QuestionSourceType;
  sourceLabel?: string;
  sourceUrl?: string;
};

export type QuestionAnswerResponse = {
  attemptId: number;
  questionId: number;
  selectedIndex: number;
  answerIndex: number;
  correct: boolean;
  explanation?: string | null;
};

export type QuestionImportResult = {
  importedCount: number;
  skippedCount: number;
  errorCount: number;
  errors: string[];
};

export type QuestionGenerationRequest = {
  studyTargetId: number;
  examType: '応用情報技術者試験' | '証券外務員一種';
  field: string;
  difficulty: 'basic' | 'standard' | 'advanced';
  count: number;
};

export type QuestionGenerationLog = {
  id: number;
  userId: number;
  studyTargetId: number;
  studyTargetName?: string | null;
  examType: string;
  field: string;
  difficulty: string;
  count: number;
  createdAt: string;
};

export type QuestionAttempt = {
  id: number;
  userId: number;
  questionId: number;
  questionText?: string | null;
  studyTargetName?: string | null;
  field?: string | null;
  selectedIndex: number;
  answerIndex?: number | null;
  correct: boolean;
  answeredAt: string;
};
