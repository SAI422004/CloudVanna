import React from 'react';
import './DeleteConfirmation.css';

export default function DeleteConfirmation({ objectName, recordId, onConfirm, onCancel, isDeleting }) {
  return (
    <div className="modal-overlay" onClick={onCancel}>
      <div className="modal-content delete-modal" onClick={(e) => e.stopPropagation()}>
        <h3 className="delete-title">Delete Record</h3>
        <p className="delete-message">
          Are you sure you want to delete this <strong>{objectName}</strong> record?
        </p>
        <p className="delete-id">ID: {recordId}</p>
        <p className="delete-warning">This action cannot be undone.</p>
        <div className="delete-actions">
          <button
            className="btn btn-secondary"
            onClick={onCancel}
            disabled={isDeleting}
          >
            Cancel
          </button>
          <button
            className="btn btn-danger"
            onClick={onConfirm}
            disabled={isDeleting}
          >
            {isDeleting ? 'Deleting...' : 'Delete'}
          </button>
        </div>
      </div>
    </div>
  );
}
