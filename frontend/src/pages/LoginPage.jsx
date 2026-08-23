import React from 'react';
import './LoginPage.css';

export default function LoginPage({ onLogin }) {
  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-logo">☁</div>
        <h1 className="login-title">Salesforce CRUD</h1>
        <p className="login-subtitle">
          Manage your Salesforce records with a clean, modern interface.
        </p>
        <button className="login-button" onClick={onLogin}>
          <svg className="login-sf-icon" viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M10.1 4.9c.9-1 2.2-1.5 3.5-1.5 1.7 0 3.2.9 4 2.2.7-.3 1.5-.5 2.3-.5 3 0 5.5 2.5 5.5 5.5s-2.5 5.5-5.5 5.5c-.4 0-.8 0-1.2-.1-.7 1.3-2.1 2.2-3.7 2.2-0.8 0-1.5-.2-2.1-.6-.7 1.6-2.3 2.7-4.2 2.7-2 0-3.7-1.3-4.3-3.1-.3 0-.6.1-.9.1C1.7 17.3 0 15.6 0 13.4c0-1.6 1-3 2.4-3.6-.1-.4-.2-.8-.2-1.2C2.2 6.2 4.4 4 7.2 4c1.1 0 2.2.4 3 1z"/>
          </svg>
          Login with Salesforce
        </button>
      </div>
    </div>
  );
}
