import { useState } from 'react';
import Button from '../../../components/common/Button.jsx';
import { EmptyState, LoadingState } from '../../../components/common/StateBlock.jsx';
import AuthProviderCard from '../components/AuthProviderCard.jsx';
import LinkAuthOtpDialog from '../components/LinkAuthOtpDialog.jsx';
import LinkEmailDialog from '../components/LinkEmailDialog.jsx';
import ReauthenticationDialog from '../components/ReauthenticationDialog.jsx';
import UnlinkProviderDialog from '../components/UnlinkProviderDialog.jsx';
import { AUTH_PROVIDER_META } from '../constants/authProviderConstants.js';
import { useAuthProviders } from '../hooks/useAuthProviders.js';
import { useLinkAuthProvider } from '../hooks/useLinkAuthProvider.js';

export default function AuthProvidersPage() {
  const providers = useAuthProviders();
  const actions = useLinkAuthProvider({ onUpdated: providers.refetch });
  const [showEmailDialog, setShowEmailDialog] = useState(false);
  const [unlinkTarget, setUnlinkTarget] = useState(null);
  const [confirmUnlink, setConfirmUnlink] = useState(false);
  const [reauthenticate, setReauthenticate] = useState(false);
  const linkedCount = providers.methods.filter((method) => method.linked).length;

  async function startEmailLink(email) { try { const flow = await actions.startEmailLink(email); if (flow) setShowEmailDialog(false); } catch { /* Hook hiển thị lỗi. */ } }
  async function linkSocial(type) { try { await actions.linkSocial(type); } catch { /* Hook hiển thị lỗi. */ } }
  async function verifyOtp(code) { try { await actions.verifyOtp(code); } catch { /* Giữ flow nếu lỗi còn sửa được. */ } }
  async function resendOtp() { try { await actions.resendOtp(); } catch { /* Chỉ rotate khi response hợp lệ. */ } }
  async function finishUnlink(proof, password) {
    try { const success = await actions.unlinkWithProof(unlinkTarget.type, proof, password); if (success) { setReauthenticate(false); setUnlinkTarget(null); } } catch { /* Không optimistic remove. */ }
  }
  function beginLink(method) {
    actions.clearMessages();
    if (AUTH_PROVIDER_META[method.type].kind === 'LOCAL') setShowEmailDialog(true);
    else void linkSocial(method.type);
  }
  function beginUnlink(method) { actions.clearMessages(); setUnlinkTarget(method); setConfirmUnlink(true); }

  return (
    <section className="w-full min-w-0 max-w-4xl px-4 pb-28 pt-6 sm:px-6 lg:pb-10 lg:pt-10">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div><p className="text-xs font-bold uppercase tracking-[0.16em] text-violet-600">Cài đặt tài khoản</p><h1 className="mt-1 text-2xl font-extrabold text-[var(--app-text)] sm:text-3xl">Phương thức đăng nhập</h1><p className="mt-2 max-w-2xl text-sm leading-6 text-[var(--app-muted)]">Quản lý các phương thức đã được Backend xác nhận cho tài khoản hiện tại.</p></div>
        <Button variant="secondary" disabled={providers.isLoading || actions.isSubmitting} onClick={() => providers.refetch().catch(() => {})}>Làm mới</Button>
      </div>
      {actions.error || providers.error ? <div className="mt-5 rounded-xl border border-red-200 bg-red-50 p-3 text-sm font-semibold text-red-700">{actions.error || providers.error}</div> : null}
      {actions.success ? <div className="mt-5 rounded-xl border border-emerald-200 bg-emerald-50 p-3 text-sm font-semibold text-emerald-700">{actions.success}</div> : null}
      <div className="mt-6">
        {providers.isLoading && providers.methods.length === 0 ? <LoadingState message="Đang tải phương thức đăng nhập..." /> : null}
        {!providers.isLoading && providers.methods.length > 0 && linkedCount === 0 ? <EmptyState title="Chưa có phương thức được Backend trả về" description="Đây là trạng thái bất thường với một phiên đã đăng nhập. Bạn vẫn có thể liên kết phương thức mới." /> : null}
        {providers.methods.length > 0 ? <div className="grid min-w-0 gap-4">{providers.methods.map((method) => <AuthProviderCard key={method.type} method={method} disabled={actions.isSubmitting || actions.ambiguousTarget === method.type} onLink={beginLink} onUnlink={beginUnlink} />)}</div> : null}
      </div>
      {showEmailDialog ? <LinkEmailDialog open busy={actions.isSubmitting} onClose={() => setShowEmailDialog(false)} onSubmit={startEmailLink} /> : null}
      {actions.linkFlow ? <LinkAuthOtpDialog flow={actions.linkFlow} busy={actions.isSubmitting} onClose={actions.clearLinkFlow} onVerify={verifyOtp} onResend={resendOtp} /> : null}
      <UnlinkProviderDialog method={unlinkTarget} open={confirmUnlink} onClose={() => { setConfirmUnlink(false); setUnlinkTarget(null); }} onConfirm={() => { setConfirmUnlink(false); setReauthenticate(true); }} />
      {reauthenticate ? <ReauthenticationDialog targetMethod={unlinkTarget?.type} methods={providers.methods} open busy={actions.isSubmitting} onClose={() => { setReauthenticate(false); setUnlinkTarget(null); }} onSubmit={finishUnlink} /> : null}
    </section>
  );
}
