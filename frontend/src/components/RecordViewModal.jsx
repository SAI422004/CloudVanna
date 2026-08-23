import React, { useEffect, useState } from 'react';
import { recordApi } from '../services/api';
import LoadingSpinner from './LoadingSpinner';
import ErrorMessage from './ErrorMessage';
import './RecordViewModal.css';

export default function RecordViewModal({ objectName, recordId, fields, onClose }) {
  const [record, setRecord] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;

    async function fetchRecord() {
      try {
        setLoading(true);
        setError(null);
        const data = await recordApi.getRecord(objectName, recordId);
        if (!cancelled) setRecord(data);
      } catch (err) {
        if (!cancelled) setError(err.message || 'Failed to load record');
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    fetchRecord();
    return () => { cancelled = true; };
  }, [objectName, recordId]);

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content view-modal" onClick={(e) => e.stopPropagation()}>
        <div className="view-modal-header">
          <h3 className="view-modal-title">View {objectName}</h3>
          <button className="view-modal-close" onClick={onClose}>✕</button>
        </div>

        {loading && <LoadingSpinner message="Loading record..." />}
        {error && <ErrorMessage message={error} />}

        {!loading && !error && record && (
          <div className="view-modal-body">
            {fields.map((field) => (
              <div key={field.name} className="view-field">
                <span className="view-field-label">{field.label}</span>
                <span className="view-field-value">
                  {formatValue(record[field.name], field.type)}
                </span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

function formatValue(value, type) {
  if (value === null || value === undefined) return '—';
  if (type === 'currency' && typeof value === 'number') {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value);
  }
  if (type === 'percent' && typeof value === 'number') {
    return `${value}%`;
  }
  if (type === 'url' && value) {
    return <a href={value} target="_blank" rel="noopener noreferrer">{value}</a>;
  }
  return String(value);
}
