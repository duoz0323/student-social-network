import { useState } from 'react';
import Button from '../../../components/common/Button.jsx';
import Modal from '../../../components/common/Modal.jsx';
import { AUTH_PROVIDER_META } from '../constants/authProviderConstants.js';

const PROOF_LABELS = { PASSWORD: 'Mật khẩu', GOOGLE: 'Google', FACEBOOK: 'Facebook' };

export default function ReauthenticationDialog({ targetMethod, methods, open, busy, onClose, onSubmit }) {
  const proofs = [
    methods.some((item) => item.linked && item.localLoginAvailable) ? 'PASSWORD' : null,
    methods.some((item) => item.type === 'GOOGLE' && item.linked) ? 'GOOGLE' : null,
    methods.some((item) => item.type === 'FACEBOOK' && item.linked) ? 'FACEBOOK' : null,
  ].filter(Boolean);
  const [proof, setProof] = useState(proofs[0] ?? '');
  const [password, setPassword] = useState('');
  if (!targetMethod) return null;
  return (
    <Modal open={open} title={`Xác thực lại để gỡ ${AUTH_PROVIDER_META[targetMethod].label}`} onClose={busy ? undefined : onClose} footer={(
      <><Button variant="ghost" disabled={busy} onClick={onClose}>Hủy</Button><Button variant="danger" disabled={busy || !proof || (proof === 'PASSWORD' && !password)} onClick={() => onSubmit(proof, password)}>{busy ? 'Đang xử lý...' : 'Xác thực và gỡ'}</Button></>
    )}>
      {proofs.length === 0 ? <p className="text-sm font-semibold text-red-600">Không có phương thức xác thực lại khả dụng.</p> : (
        <>
          <fieldset className="grid gap-2">
            <legend className="mb-2 text-sm font-semibold text-[var(--app-text)]">Chọn bằng chứng xác thực</legend>
            {proofs.map((item) => <label key={item} className="flex items-center gap-3 rounded-xl border border-[var(--app-border)] p-3 text-sm"><input type="radio" name="reauth-proof" value={item} checked={proof === item} disabled={busy} onChange={() => { setProof(item); setPassword(''); }} />{PROOF_LABELS[item]}</label>)}
          </fieldset>
          {proof === 'PASSWORD' ? <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} autoComplete="current-password" placeholder="Nhập mật khẩu hiện tại" disabled={busy} className="mt-4 h-11 w-full rounded-xl border border-[var(--app-border-strong)] bg-[var(--app-surface)] px-3 text-sm outline-none focus:ring-2 focus:ring-violet-500/30" /> : null}
          <p className="mt-4 text-xs leading-5 text-[var(--app-muted)]">Token xác thực lại chỉ được giữ trong bộ nhớ cho thao tác gỡ hiện tại.</p>
        </>
      )}
    </Modal>
  );
}
