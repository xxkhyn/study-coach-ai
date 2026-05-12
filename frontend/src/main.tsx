import React from 'react';
import ReactDOM from 'react-dom/client';
import { Navigate, createBrowserRouter, RouterProvider } from 'react-router-dom';
import App from './App';
import AiAdvicePage from './pages/AiAdvicePage';
import AnalyticsPage from './pages/AnalyticsPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import StudyLogFormPage from './pages/StudyLogFormPage';
import StudyLogListPage from './pages/StudyLogListPage';
import StudyTargetFormPage from './pages/StudyTargetFormPage';
import StudyTargetListPage from './pages/StudyTargetListPage';
import StudyTaskFormPage from './pages/StudyTaskFormPage';
import StudyTaskListPage from './pages/StudyTaskListPage';
import ProtectedRoute from './components/ProtectedRoute';
import './styles.css';

const router = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  { path: '/register', element: <RegisterPage /> },
  {
    path: '/',
    element: (
      <ProtectedRoute>
        <App />
      </ProtectedRoute>
    ),
    children: [
      { index: true, element: <Navigate to="/dashboard" replace /> },
      { path: 'dashboard', element: <DashboardPage /> },
      { path: 'analytics', element: <AnalyticsPage /> },
      { path: 'ai-advice', element: <AiAdvicePage /> },
      { path: 'targets', element: <StudyTargetListPage /> },
      { path: 'targets/new', element: <StudyTargetFormPage /> },
      { path: 'targets/:id/edit', element: <StudyTargetFormPage /> },
      { path: 'tasks', element: <StudyTaskListPage /> },
      { path: 'tasks/new', element: <StudyTaskFormPage /> },
      { path: 'tasks/:id/edit', element: <StudyTaskFormPage /> },
      { path: 'logs', element: <StudyLogListPage /> },
      { path: 'logs/new', element: <StudyLogFormPage /> },
      { path: 'logs/:id/edit', element: <StudyLogFormPage /> },
    ],
  },
], {
  future: {
    v7_fetcherPersist: true,
    v7_normalizeFormMethod: true,
    v7_partialHydration: true,
    v7_relativeSplatPath: true,
    v7_skipActionErrorRevalidation: true,
  },
});

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <RouterProvider router={router} future={{ v7_startTransition: true }} />
  </React.StrictMode>,
);
