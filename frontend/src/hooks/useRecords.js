import { useState, useEffect, useCallback, useRef } from 'react';
import { recordApi } from '../services/api';

/**
 * Hook for fetching records with infinite scroll and cursor-based pagination.
 * Handles deduplication, abort on object change, and rapid scroll protection.
 */
export function useRecords(objectName) {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState(null);
  const [hasMore, setHasMore] = useState(false);
  const [totalSize, setTotalSize] = useState(0);

  const nextCursorRef = useRef(null);
  const abortControllerRef = useRef(null);
  const isFetchingRef = useRef(false);

  // Reset when object changes
  useEffect(() => {
    setRecords([]);
    setError(null);
    setHasMore(false);
    setTotalSize(0);
    nextCursorRef.current = null;
  }, [objectName]);

  /**
   * Fetch the first page of records.
   */
  const fetchRecords = useCallback(async () => {
    if (!objectName) return;

    // Cancel any in-flight request
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }

    const controller = new AbortController();
    abortControllerRef.current = controller;
    isFetchingRef.current = true;

    setLoading(true);
    setError(null);
    setRecords([]);
    nextCursorRef.current = null;

    try {
      const data = await recordApi.getRecords(objectName, null, controller.signal);
      if (controller.signal.aborted) return;

      setRecords(data.records || []);
      setTotalSize(data.totalSize || 0);
      setHasMore(!data.done);
      nextCursorRef.current = data.nextPageUrl || null;
    } catch (err) {
      if (err.name === 'AbortError') return;
      setError(err.message || 'Failed to load records');
    } finally {
      setLoading(false);
      isFetchingRef.current = false;
    }
  }, [objectName]);

  /**
   * Fetch the next page (used by infinite scroll).
   */
  const fetchMore = useCallback(async () => {
    if (!objectName || !nextCursorRef.current || isFetchingRef.current) return;

    // Cancel any in-flight request
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }

    const controller = new AbortController();
    abortControllerRef.current = controller;
    isFetchingRef.current = true;

    setLoadingMore(true);

    try {
      const data = await recordApi.getRecords(objectName, nextCursorRef.current, controller.signal);
      if (controller.signal.aborted) return;

      setRecords(prev => {
        // Deduplicate by Id
        const existingIds = new Set(prev.map(r => r.Id));
        const newRecords = (data.records || []).filter(r => !existingIds.has(r.Id));
        return [...prev, ...newRecords];
      });
      setHasMore(!data.done);
      nextCursorRef.current = data.nextPageUrl || null;
    } catch (err) {
      if (err.name === 'AbortError') return;
      setError(err.message || 'Failed to load more records');
    } finally {
      setLoadingMore(false);
      isFetchingRef.current = false;
    }
  }, [objectName]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, []);

  // Auto-fetch when object changes
  useEffect(() => {
    if (objectName) {
      fetchRecords();
    }
  }, [objectName, fetchRecords]);

  return {
    records,
    loading,
    loadingMore,
    error,
    hasMore,
    totalSize,
    fetchRecords,
    fetchMore,
  };
}
