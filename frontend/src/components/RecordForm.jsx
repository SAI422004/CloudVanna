import React, { useState, useEffect } from 'react';
import { recordApi } from '../services/api';
import LoadingSpinner from './LoadingSpinner';
import './RecordForm.css';

export default function RecordForm({
  objectName,
  fields,
  recordId = null,
  onSuccess,
  onCancel,
}) {
  const isEdit = !!recordId;
  const [formData, setFormData] = useState({});
  const [loading, setLoading] = useState(isEdit);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({});

  // If editing, load existing record
  useEffect(() => {
    if (!isEdit) {
      // Initialize empty form for create
      const initial = {};
      fields.forEach((f) => { initial[f.name] = ''; });
      setFormData(initial);
      return;
    }

    let cancelled = false;
    async function loadRecord() {
      try {
        setLoading(true);
        const data = await recordApi.getRecord(objectName, recordId);
        if (!cancelled) {
          const filtered = {};
          fields.forEach((f) => {
            filtered[f.name] = data[f.name] ?? '';
          });
          setFormData(filtered);
        }
      } catch (err) {
        if (!cancelled) setError(err.message || 'Failed to load record');
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    loadRecord();
    return () => { cancelled = true; };
  }, [objectName, recordId, isEdit, fields]);

  function handleChange(fieldName, value) {
    setFormData((prev) => ({ ...prev, [fieldName]: value }));
    // Clear field error on change
    if (fieldErrors[fieldName]) {
      setFieldErrors((prev) => {
        const next = { ...prev };
        delete next[fieldName];
        return next;
      });
    }
  }

  function validate() {
    const errors = {};
    fields.forEach((field) => {
      if (field.required) {
        const val = formData[field.name];
        if (val === undefined || val === null || String(val).trim() === '') {
          errors[field.name] = `${field.label} is required`;
        }
      }
      // Email validation
      if (field.type === 'email' && formData[field.name]) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(formData[field.name])) {
          errors[field.name] = 'Invalid email address';
        }
      }
    });
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    if (saving) return; // Prevent duplicate submits
    if (!validate()) return;

    setSaving(true);
    setError(null);

    try {
      // Build payload with only non-empty fields
      const payload = {};
      fields.forEach((field) => {
        const val = formData[field.name];
        if (val !== undefined && val !== null && String(val).trim() !== '') {
          // Convert numeric fields
          if (field.type === 'currency' || field.type === 'percent') {
            payload[field.name] = parseFloat(val);
          } else {
            payload[field.name] = val;
          }
        } else if (isEdit) {
          // For edit, explicitly set empty values to null
          payload[field.name] = null;
        }
      });

      if (isEdit) {
        await recordApi.updateRecord(objectName, recordId, payload);
      } else {
        await recordApi.createRecord(objectName, payload);
      }
      onSuccess();
    } catch (err) {
      setError(err.message || `Failed to ${isEdit ? 'update' : 'create'} record`);
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <div className="modal-overlay" onClick={onCancel}>
        <div className="modal-content form-modal" onClick={(e) => e.stopPropagation()}>
          <LoadingSpinner message="Loading record..." />
        </div>
      </div>
    );
  }

  return (
    <div className="modal-overlay" onClick={onCancel}>
      <div className="modal-content form-modal" onClick={(e) => e.stopPropagation()}>
        <div className="form-header">
          <h3 className="form-title">
            {isEdit ? 'Edit' : 'Create'} {objectName}
          </h3>
          <button className="form-close" onClick={onCancel} disabled={saving}>✕</button>
        </div>

        {error && (
          <div className="form-error">{error}</div>
        )}

        <form onSubmit={handleSubmit} className="form-body">
          {fields.map((field) => (
            <div key={field.name} className="form-group">
              <label htmlFor={`field-${field.name}`} className="form-label">
                {field.label}
                {field.required && <span className="form-required">*</span>}
              </label>
              {renderInput(field, formData[field.name] ?? '', handleChange, saving)}
              {fieldErrors[field.name] && (
                <span className="form-field-error">{fieldErrors[field.name]}</span>
              )}
            </div>
          ))}

          <div className="form-actions">
            <button
              type="button"
              className="btn btn-secondary"
              onClick={onCancel}
              disabled={saving}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={saving}
            >
              {saving ? 'Saving...' : isEdit ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function renderInput(field, value, onChange, disabled) {
  const commonProps = {
    id: `field-${field.name}`,
    className: 'form-input',
    value: value ?? '',
    onChange: (e) => onChange(field.name, e.target.value),
    disabled,
    placeholder: `Enter ${field.label.toLowerCase()}`,
  };

  switch (field.type) {
    case 'textarea':
      return <textarea {...commonProps} rows={3} />;
    case 'date':
      return <input {...commonProps} type="date" />;
    case 'email':
      return <input {...commonProps} type="email" />;
    case 'phone':
      return <input {...commonProps} type="tel" />;
    case 'url':
      return <input {...commonProps} type="url" />;
    case 'currency':
    case 'percent':
      return <input {...commonProps} type="number" step="0.01" />;
    default:
      return <input {...commonProps} type="text" />;
  }
}
