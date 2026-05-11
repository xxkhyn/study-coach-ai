export type StudyTarget = {
  id: number;
  userId: number;
  name: string;
  category?: string | null;
  examDate?: string | null;
  goalDate?: string | null;
  memo?: string | null;
};

export type StudyTargetRequest = {
  name: string;
  category?: string;
  examDate?: string;
  goalDate?: string;
  memo?: string;
};

export type StudyTask = {
  id: number;
  userId: number;
  targetId: number;
  targetName: string;
  title: string;
  fieldName?: string | null;
  plannedMinutes?: number | null;
  dueDate?: string | null;
  completed: boolean;
};

export type StudyTaskRequest = {
  targetId: number;
  title: string;
  fieldName?: string;
  plannedMinutes?: number;
  dueDate?: string;
  completed: boolean;
};

export type Dashboard = {
  targetCount: number;
  taskCount: number;
  completedTaskCount: number;
  openTaskCount: number;
  overdueTaskCount: number;
  plannedMinutesThisWeek: number;
  todayTasks: StudyTask[];
  upcomingTasks: StudyTask[];
};
