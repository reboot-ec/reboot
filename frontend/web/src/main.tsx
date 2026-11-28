import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Link, Navigate, Route, Routes } from 'react-router-dom';
import { AuthLayout } from './components/templates/AuthLayout';
import { DashboardLayout } from './components/templates/DashboardLayout';
import { ProtectedRoute } from './components/templates/ProtectedRoute';
import { AuthProvider } from './context/AuthContext';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { OrganizationsPage } from './pages/OrganizationsPage';
import { OrganizationDetailPage } from './pages/OrganizationDetailPage';
import { CreateOrganizationPage } from './pages/CreateOrganizationPage';
import { MyOrganizationsPage } from './pages/MyOrganizationsPage';
import { CitizenDashboardPage } from './pages/CitizenDashboardPage';
import './styles/global.css';

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/" element={<Navigate replace to="/organizations" />} />
          <Route element={<AuthLayout />}>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
          </Route>
          <Route element={<DashboardLayout />}>
            <Route element={<ProtectedRoute />}>
              <Route path="/dashboard" element={<CitizenDashboardPage />} />
              <Route path="/organizations" element={<OrganizationsPage />} />
              <Route path="/organizations/new" element={<CreateOrganizationPage />} />
              <Route path="/organizations/mine" element={<MyOrganizationsPage />} />
            </Route>
            <Route path="/organizations/:id" element={<OrganizationDetailPage />} />
          </Route>
          <Route path="*" element={<main className="container dashboard"><h1>Page not found</h1><Link to="/">Back to home</Link></main>} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>,
);
