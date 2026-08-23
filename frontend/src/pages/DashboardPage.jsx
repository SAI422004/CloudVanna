import React, { useState, useEffect, useCallback } from 'react';
import { objectApi } from '../services/api';
import { useRecords } from '../hooks/useRecords';
import ObjectSelector from '../components/ObjectSelector';
import RecordTable from '../components/RecordTable';
import RecordForm from '../components/RecordForm';
import RecordViewModal from '../components/RecordViewModal';
import DeleteConfirmation from '../components/DeleteConfirmation';
import LoadingSpinner from '../components/LoadingSpinner';
import ErrorMessage from '../components/ErrorMessage';
import Toast from '../components/Toast';
import { recordApi } from '../services/api';
import './DashboardPage.css';

export default function DashboardPage({ onLogout }) {
  // Object list & selection
  const [objects, setObjects] = useState([]);
  const [selectedObject, setSelectedObject] = useState('');
  const [objectLoading, setObjectLoading] = useState(true);
  const [objectError, setObjectError] = useState(null);

  // Metadata (fields)
  const [metadata, setMetadata] = useState(null);
  const [metadataLoading, setMetadataLoading] = useState(false);

  // Records (via useRecords hook)
  const {
    records,
    loading: recordsLoading,
    loadingMore,
    error: recordsError,
    hasMore,
    totalSize,
    fetchRecords,
    fetchMore,
  } = useRecords(selectedObject);

  // Modal states
  const [viewRecordId, setViewRecordId] = useState(null);
  const [editRecordId, setEditRecordId] = useState(null);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [deleteRecordId, setDeleteRecordId] = useState(null);
  const [isDeleting, setIsDeleting] = useState(false);

  // Toast
  const [toast, setToast] = useState(null);

  // Load supported objects on mount
  useEffect(() => {
    async function loadObjects() {
      try {
        setObjectLoading(true);
        const data = await objectApi.getObjects();
        setObjects(data.objects || []);
      } catch (err) {
        setObjectError(err.message || 'Failed to load objects');
      } finally {
        setObjectLoading(false);
      }
    }
    loadObjects();
  }, []);

  // Load metadata when object changes
  useEffect(() => {
    if (!selectedObject) {
      setMetadata(null);
      return;
    }

    let cancelled = false;
    async function loadMetadata() {
      try {
        setMetadataLoading(true);
        const data = await objectApi.getMetadata(selectedObject);
        if (!cancelled) setMetadata(data);
      } catch (err) {
        if (!cancelled) {
          setToast({ message: `Failed to load ${selectedObject} metadata`, type: 'error' });
        }
      } finally {
        if (!cancelled) setMetadataLoading(false);
      }
    }
    loadMetadata();
    return () => { cancelled = true; };
  }, [selectedObject]);

  function handleObjectChange(newObject) {
    setSelectedObject(newObject);
    // Close any open modals
    setViewRecordId(null);
    setEditRecordId(null);
    setShowCreateForm(false);
    setDeleteRecordId(null);
  }

  function handleCreateSuccess() {
    setShowCreateForm(false);
    setToast({ message: `${selectedObject} created successfully`, type: 'success' });
    fetchRecords();
  }

  function handleEditSuccess() {
    setEditRecordId(null);
    setToast({ message: `${selectedObject} updated successfully`, type: 'success' });
    fetchRecords();
  }

  const handleDeleteConfirm = useCallback(async () => {
    if (isDeleting || !deleteRecordId) return;
    setIsDeleting(true);
    try {
      await recordApi.deleteRecord(selectedObject, deleteRecordId);
      setDeleteRecordId(null);
      setToast({ message: `${selectedObject} deleted successfully`, type: 'success' });
      fetchRecords();
    } catch (err) {
      setToast({ message: err.message || 'Failed to delete record', type: 'error' });
    } finally {
      setIsDeleting(false);
    }
  }, [isDeleting, deleteRecordId, selectedObject, fetchRecords]);

  if (objectLoading) {
    return (
      <div className="dashboard-page">
        <LoadingSpinner message="Loading Salesforce objects..." />
      </div>
    );
  }

  if (objectError) {
    return (
      <div className="dashboard-page">
        <ErrorMessage message={objectError} />
      </div>
    );
  }

  return (
    <div className="dashboard-page">
      {/* Header */}
      <header className="dashboard-header">
        <div className="dashboard-header-left">
          <h1 className="dashboard-title">☁ Salesforce CRUD</h1>
        </div>
        <div className="dashboard-header-right">
          <button className="btn btn-secondary btn-sm" onClick={onLogout}>
            Logout
          </button>
        </div>
      </header>

      {/* Toolbar */}
      <div className="dashboard-toolbar">
        <ObjectSelector
          objects={objects}
          selectedObject={selectedObject}
          onChange={handleObjectChange}
          disabled={recordsLoading}
        />

        {selectedObject && metadata && (
          <button
            className="btn btn-primary"
            onClick={() => setShowCreateForm(true)}
            disabled={recordsLoading || metadataLoading}
          >
            + Create {selectedObject}
          </button>
        )}
      </div>

      {/* Content */}
      <div className="dashboard-content">
        {!selectedObject && (
          <div className="placeholder-state">
            <div className="placeholder-icon">📦</div>
            <p className="placeholder-text">Select a Salesforce object to get started</p>
          </div>
        )}

        {selectedObject && (metadataLoading || recordsLoading) && (
          <LoadingSpinner message={`Loading ${selectedObject} records...`} />
        )}

        {selectedObject && recordsError && !recordsLoading && (
          <ErrorMessage message={recordsError} onRetry={fetchRecords} />
        )}

        {selectedObject && !recordsLoading && !recordsError && metadata && (
          <>
            <div className="records-info">
              <span className="records-count">
                Showing {records.length} of {totalSize} record{totalSize !== 1 ? 's' : ''}
              </span>
            </div>
            <RecordTable
              records={records}
              fields={metadata.fields}
              loading={recordsLoading}
              loadingMore={loadingMore}
              hasMore={hasMore}
              onFetchMore={fetchMore}
              onView={(id) => setViewRecordId(id)}
              onEdit={(id) => setEditRecordId(id)}
              onDelete={(id) => setDeleteRecordId(id)}
            />
          </>
        )}
      </div>

      {/* Modals */}
      {viewRecordId && metadata && (
        <RecordViewModal
          objectName={selectedObject}
          recordId={viewRecordId}
          fields={metadata.fields}
          onClose={() => setViewRecordId(null)}
        />
      )}

      {showCreateForm && metadata && (
        <RecordForm
          objectName={selectedObject}
          fields={metadata.editableFields}
          onSuccess={handleCreateSuccess}
          onCancel={() => setShowCreateForm(false)}
        />
      )}

      {editRecordId && metadata && (
        <RecordForm
          objectName={selectedObject}
          fields={metadata.editableFields}
          recordId={editRecordId}
          onSuccess={handleEditSuccess}
          onCancel={() => setEditRecordId(null)}
        />
      )}

      {deleteRecordId && (
        <DeleteConfirmation
          objectName={selectedObject}
          recordId={deleteRecordId}
          onConfirm={handleDeleteConfirm}
          onCancel={() => !isDeleting && setDeleteRecordId(null)}
          isDeleting={isDeleting}
        />
      )}

      {/* Toast */}
      {toast && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(null)}
        />
      )}
    </div>
  );
}
