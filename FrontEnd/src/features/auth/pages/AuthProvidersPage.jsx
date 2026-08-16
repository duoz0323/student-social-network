import { useState } from 'react';
import { RefreshCw } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { EmptyState, LoadingState } from '../../../components/common/StateBlock.jsx';
import AuthProviderCard from '../components/AuthProviderCard.jsx';
import LinkAuthOtpDialog from '../components/LinkAuthOtpDialog.jsx';
import LinkEmailDialog from '../components/LinkEmailDialog.jsx';
import PasswordMethodDialog from '../components/PasswordMethodDialog.jsx';
import ReauthenticationDialog from '../components/ReauthenticationDialog.jsx';
import UnlinkProviderDialog from '../components/UnlinkProviderDialog.jsx';
import { AUTH_PROVIDER_META } from '../constants/authProviderConstants.js';
import { useAuthProviders } from '../hooks/useAuthProviders.js';
import { useLinkAuthProvider } from '../hooks/useLinkAuthProvider.js';
import { useAuth } from '../hooks/useAuth.js';

export default function AuthProvidersPage() {
  const providers = useAuthProviders();
  const auth = useAuth();
  const navigate = useNavigate();
  const actions = useLinkAuthProvider({ onUpdated: providers.refetch });
  const [showEmailDialog, setShowEmailDialog] = useState(false);
  const [unlinkTarget, setUnlinkTarget] = useState(null);
  const [confirmUnlink, setConfirmUnlink] = useState(false);
  const [reauthenticate, setReauthenticate] = useState(false);
  const [passwordMode, setPasswordMode] = useState(null);
  const [setPasswordProof, setSetPasswordProof] = useState(null);
  const linkedCount = providers.methods.filter((method) => method.linked).length;
  const pageError = providers.error || (!passwordMode ? actions.error : '');

  async function startEmailLink(email) { try { const flow = await actions.startEmailLink(email); if (flow) setShowEmailDialog(false); } catch { /* Hook hiển thị lỗi. */ } }
  async function linkSocial(type) { try { await actions.linkSocial(type); } catch { /* Hook hiển thị lỗi. */ } }
  async function verifyEmailOtp(code) { try { return await actions.verifyEmailOtp(code); } catch { return null; } }
  async function completeEmailLink(payload) { try { await actions.completeEmailLink(payload); } catch { /* Giữ flow nếu lỗi còn sửa được. */ } }
  async function resendOtp() { try { await actions.resendOtp(); } catch { /* Chỉ rotate khi response hợp lệ. */ } }
  async function finishUnlink(proof, password) {
    try { const success = await actions.unlinkWithProof(unlinkTarget.type, proof, password); if (success) { setReauthenticate(false); setUnlinkTarget(null); } } catch { /* Không optimistic remove. */ }
  }
  function beginSetPassword() { actions.clearMessages(); setReauthenticate(true); setUnlinkTarget({ type: 'EMAIL', purpose: 'SET_PASSWORD' }); }
  function acceptSetPasswordProof(proof) { setSetPasswordProof(proof); setReauthenticate(false); setPasswordMode('SET'); }
  async function submitPassword(payload) {
    try {
      if (passwordMode === 'SET') await actions.setPassword(setPasswordProof, payload);
      else await actions.changePassword(payload);
      setPasswordMode(null);
      setSetPasswordProof(null);
      auth.clearSession('UNAUTHENTICATED');
      navigate('/login', { replace: true, state: { message: passwordMode === 'CHANGE' ? 'Mật khẩu đã được thay đổi. Vui lòng đăng nhập lại.' : 'Mật khẩu đã được thiết lập. Vui lòng đăng nhập lại.' } });
    } catch { /* Hook ánh xạ lỗi an toàn. */ }
  }
  function beginLink(method) {
    actions.clearMessages();
    if (AUTH_PROVIDER_META[method.type].kind === 'LOCAL') setShowEmailDialog(true);
    else void linkSocial(method.type);
  }
  function beginUnlink(method) { actions.clearMessages(); setUnlinkTarget(method); setConfirmUnlink(true); }

  return (
    <section className="w-full min-w-0 px-5 pb-10 pt-6 sm:px-7 sm:pt-8">
      <div className="flex items-start justify-between gap-4 border-b border-[var(--app-border)] pb-5">
        <div>
          <h1 className="text-xl font-extrabold tracking-[-0.015em] text-[var(--app-text)]">Phương thức đăng nhập</h1>
          <p className="mt-1.5 max-w-xl text-sm leading-6 text-[var(--app-muted)]">
            Chọn cách bạn muốn dùng để đăng nhập vào tài khoản.
          </p>
        </div>
        <button
          type="button"
          disabled={providers.isLoading || actions.isSubmitting}
          onClick={() => providers.refetch().catch(() => {})}
          className="mt-0.5 flex h-9 w-9 shrink-0 items-center justify-center rounded-full text-[var(--app-muted)] transition hover:bg-[var(--app-surface-soft)] hover:text-[var(--app-text)] disabled:cursor-not-allowed disabled:opacity-50"
          aria-label="Làm mới phương thức đăng nhập"
          title="Làm mới"
        >
          <RefreshCw size={18} className={providers.isLoading ? 'animate-spin' : ''} aria-hidden="true" />
        </button>
      </div>
      {pageError ? <div className="mt-5 rounded-xl border border-red-200 bg-red-50 p-3 text-sm font-semibold text-red-700">{pageError}</div> : null}
      {actions.success ? <div className="mt-5 rounded-xl border border-emerald-200 bg-emerald-50 p-3 text-sm font-semibold text-emerald-700">{actions.success}</div> : null}
      <div>
        {providers.isLoading && providers.methods.length === 0 ? <LoadingState message="Đang tải phương thức đăng nhập..." /> : null}
        {!providers.isLoading && providers.methods.length > 0 && linkedCount === 0 ? <EmptyState title="Chưa có phương thức được Backend trả về" description="Đây là trạng thái bất thường với một phiên đã đăng nhập. Bạn vẫn có thể liên kết phương thức mới." /> : null}
        {providers.methods.length > 0 ? <div className="min-w-0">{providers.methods.map((method) => <AuthProviderCard key={method.type} method={method} disabled={actions.isSubmitting || actions.ambiguousTarget === method.type} onLink={beginLink} onUnlink={beginUnlink} onSetPassword={beginSetPassword} onChangePassword={() => { actions.clearMessages(); setPasswordMode('CHANGE'); }} />)}</div> : null}
      </div>
      {showEmailDialog ? <LinkEmailDialog open busy={actions.isSubmitting} onClose={() => setShowEmailDialog(false)} onSubmit={startEmailLink} /> : null}
      {actions.linkFlow ? <LinkAuthOtpDialog flow={actions.linkFlow} busy={actions.isSubmitting} onClose={actions.clearLinkFlow} onVerifyOtp={verifyEmailOtp} onComplete={completeEmailLink} onResend={resendOtp} /> : null}
      <UnlinkProviderDialog method={unlinkTarget} open={confirmUnlink} onClose={() => { setConfirmUnlink(false); setUnlinkTarget(null); }} onConfirm={() => { setConfirmUnlink(false); setReauthenticate(true); }} />
      {reauthenticate ? <ReauthenticationDialog targetMethod={unlinkTarget?.type} methods={providers.methods} purpose={unlinkTarget?.purpose ?? 'UNLINK_AUTH_METHOD'} open busy={actions.isSubmitting} onClose={() => { setReauthenticate(false); setUnlinkTarget(null); }} onSubmit={unlinkTarget?.purpose === 'SET_PASSWORD' ? acceptSetPasswordProof : finishUnlink} /> : null}
      {passwordMode ? <PasswordMethodDialog mode={passwordMode} open busy={actions.isSubmitting} error={actions.error} onClearError={actions.clearMessages} onClose={() => { actions.clearMessages(); setPasswordMode(null); setSetPasswordProof(null); }} onSubmit={submitPassword} /> : null}
    </section>
  );
}
