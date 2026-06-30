import { useState } from 'react';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';

const reasons = ['SPAM', 'HARASSMENT', 'MISINFORMATION', 'OTHER'];

export default function ReportPostFlow({ open, post, onClose }) {
  const { submitReport } = useApp();
  const [step, setStep] = useState('reason');
  const [reason, setReason] = useState('SPAM');
  const [description, setDescription] = useState('');
  const [message, setMessage] = useState('');

  function submit() {
    const result = submitReport(post.id, reason, description);
    if (!result.ok) {
      setMessage(result.message);
      return;
    }
    setStep('success');
  }

  function close() {
    setStep('reason');
    setDescription('');
    setMessage('');
    onClose();
  }

  return (
    <Modal
      open={open}
      title={step === 'success' ? 'Da gui bao cao' : 'Bao cao bai viet'}
      onClose={close}
      footer={
        step === 'success' ? (
          <Button onClick={close}>Xong</Button>
        ) : step === 'reason' ? (
          <Button onClick={() => setStep('detail')}>Tiep tuc</Button>
        ) : (
          <>
            <Button variant="secondary" onClick={() => setStep('reason')}>Quay lai</Button>
            <Button onClick={submit}>Gui bao cao</Button>
          </>
        )
      }
    >
      {step === 'success' ? (
        <p className="text-sm text-zinc-600">Quan tri vien se xem xet bao cao cua ban. Bao cao khong tu dong an bai viet.</p>
      ) : step === 'reason' ? (
        <div className="space-y-2">
          {reasons.map((item) => (
            <label key={item} className="flex items-center justify-between rounded-2xl border border-zinc-200 px-4 py-3 text-sm font-semibold">
              {item}
              <input type="radio" checked={reason === item} onChange={() => setReason(item)} />
            </label>
          ))}
        </div>
      ) : (
        <>
          <p className="rounded-2xl bg-zinc-50 p-3 text-sm text-zinc-600">{post.content}</p>
          <textarea
            value={description}
            onChange={(event) => setDescription(event.target.value)}
            placeholder="Mo ta them neu can..."
            className="mt-4 min-h-28 w-full rounded-2xl border border-zinc-200 p-3"
          />
          {message ? <p className="mt-3 rounded-xl bg-amber-50 p-3 text-sm text-amber-700">{message}</p> : null}
        </>
      )}
    </Modal>
  );
}
