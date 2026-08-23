import React from 'react';
import { useAuth } from './hooks/useAuth';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import LoadingSpinner from './components/LoadingSpinner';
import './App.css';

function App() {
  const { authenticated, loading, login, logout } = useAuth();

  if (loading) {
    return (
      <div className="app-loading">
        <LoadingSpinner message="Checking authentication..." />
      </div>
    );
  }

  if (!authenticated) {
    return <LoginPage onLogin={login} />;
  }

  return <DashboardPage onLogout={logout} />;
}

export default App;
