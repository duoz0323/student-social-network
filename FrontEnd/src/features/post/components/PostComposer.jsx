import { useState } from 'react';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';

export default function PostComposer({ open, onClose }) {
  const { createPost } = useApp();
  const [content, setContent] = useState('');
  const [hashtags, setHashtags] = useState('');
  const [error, setError] = useState('');

  function submit() {
    const result = createPost({ content, hashtags });
    if (!result.ok) {
      setError(result.message);
      return;
    }
    setContent('');
    setHashtags('');
    setError('');
    onClose();
  }

  return (
    <Modal
      open={open}
      title="Tao bai viet"
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" onClick={onClose}>Huy</Button>
          <Button disabled={!content.trim() || content.length > 500} onClick={submit}>Dang bai</Button>
        </>
      }
    >
      <textarea
        value={content}
        maxLength={500}
        onChange={(event) => setContent(event.target.value)}
        placeholder="Chia se dieu gi do voi ban be..."
        className="min-h-36 w-full resize-none rounded-[var(--radius-input)] border border-[var(--app-border-strong)] px-4 py-3 outline-none focus:border-[var(--app-text)] focus:ring-4 focus:ring-black/5"
      />
      <div className="mt-2 flex justify-between text-xs text-zinc-500">
        <span>Hashtag cach nhau bang dau phay hoac khoang trang.</span>
        <span>{content.length}/500</span>
      </div>
      <input
        value={hashtags}
        onChange={(event) => setHashtags(event.target.value)}
        placeholder="hoctap, sinhvien"
        className="mt-4 w-full rounded-[var(--radius-input)] border border-[var(--app-border-strong)] px-4 py-3 outline-none focus:border-[var(--app-text)] focus:ring-4 focus:ring-black/5"
      />
      {error ? <p className="mt-3 rounded-xl bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p> : null}
    </Modal>
  );
}
