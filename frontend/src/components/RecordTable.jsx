import React, { useRef, useCallback, useEffect } from 'react';
import './RecordTable.css';

export default function RecordTable({
  records,
  fields,
  loading,
  loadingMore,
  hasMore,
  onFetchMore,
  onView,
  onEdit,
  onDelete,
}) {
  const observerRef = useRef(null);
  const sentinelRef = useRef(null);

  // Infinite scroll: observe a sentinel element at the bottom of the table
  const handleObserver = useCallback(
    (entries) => {
      const target = entries[0];
      if (target.isIntersecting && hasMore && !loadingMore && !loading) {
        onFetchMore();
      }
    },
    [hasMore, loadingMore, loading, onFetchMore]
  );

  useEffect(() => {
    const sentinel = sentinelRef.current;
    if (!sentinel) return;

    observerRef.current = new IntersectionObserver(handleObserver, {
      rootMargin: '200px',
    });
    observerRef.current.observe(sentinel);

    return () => {
      if (observerRef.current) {
        observerRef.current.disconnect();
      }
    };
  }, [handleObserver]);

  // Filter out Id from display columns but keep it for actions
  const displayFields = fields.filter((f) => f.name !== 'Id');

  if (!loading && records.length === 0) {
    return (
      <div className="empty-state">
        <div className="empty-icon">📋</div>
        <p className="empty-text">No records found</p>
      </div>
    );
  }

  return (
    <div className="table-wrapper">
      <table className="record-table">
        <thead>
          <tr>
            {displayFields.map((field) => (
              <th key={field.name}>{field.label}</th>
            ))}
            <th className="actions-col">Actions</th>
          </tr>
        </thead>
        <tbody>
          {records.map((record) => (
            <tr key={record.Id}>
              {displayFields.map((field) => (
                <td key={field.name} title={formatCellValue(record[field.name], field.type)}>
                  {formatCellValue(record[field.name], field.type)}
                </td>
              ))}
              <td className="actions-col">
                <div className="action-buttons">
                  <button
                    className="action-btn action-view"
                    onClick={() => onView(record.Id)}
                    title="View"
                  >
                    👁
                  </button>
                  <button
                    className="action-btn action-edit"
                    onClick={() => onEdit(record.Id)}
                    title="Edit"
                  >
                    ✏️
                  </button>
                  <button
                    className="action-btn action-delete"
                    onClick={() => onDelete(record.Id)}
                    title="Delete"
                  >
                    🗑
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* Sentinel for infinite scroll */}
      <div ref={sentinelRef} className="scroll-sentinel" />

      {loadingMore && (
        <div className="loading-more">
          <div className="loading-more-spinner"></div>
          <span>Loading more records...</span>
        </div>
      )}
    </div>
  );
}

function formatCellValue(value, type) {
  if (value === null || value === undefined) return '—';
  if (type === 'currency' && typeof value === 'number') {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value);
  }
  if (type === 'percent' && typeof value === 'number') {
    return `${value}%`;
  }
  return String(value);
}
