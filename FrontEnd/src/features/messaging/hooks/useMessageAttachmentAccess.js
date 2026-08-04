import { useCallback, useEffect, useState } from 'react';
import { isRequestCanceled } from '../../../api/apiError.js';
import { messagingApi } from '../services/messagingApi.js';

/** Mỗi URL ảnh chat chỉ được lấy sau khi Backend kiểm tra lại quyền truy cập. */
export function useMessageAttachmentAccess(attachmentId) {
  const [state, setState] = useState({ accessUrl: '', loading: true, error: '' });
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    if (!attachmentId) return undefined;
    const controller = new AbortController();
    messagingApi.getAttachmentAccess(attachmentId, controller.signal)
      .then((response) => setState({ accessUrl: response.accessUrl, loading: false, error: '' }))
      .catch((error) => {
        if (!isRequestCanceled(error)) setState({ accessUrl: '', loading: false, error: error.message });
      });
    return () => controller.abort();
  }, [attachmentId, attempt]);

  const retry = useCallback(() => {
    setState({ accessUrl: '', loading: true, error: '' });
    setAttempt((value) => value + 1);
  }, []);
  return { ...state, retry };
}
