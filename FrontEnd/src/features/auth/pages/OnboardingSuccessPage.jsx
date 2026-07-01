import { useNavigate } from 'react-router-dom';
import logo from '../../../assets/brand/logo.png';
import Button from '../../../components/common/Button.jsx';

export default function OnboardingSuccessPage() {
  const navigate = useNavigate();

  return (
    <main className="auth-pattern flex min-h-screen flex-col items-center justify-center px-4 py-8">
      <section className="stitch-card-shadow w-full max-w-[420px] rounded-[10px] border border-[var(--app-border)] bg-white px-9 py-10 text-center">
        <div className="mx-auto flex h-20 w-20 items-center justify-center rounded-full bg-zinc-100">
          <img src={logo} alt="UniShare" className="h-12 w-12 object-contain" />
        </div>
        <h1 className="mt-5 text-base font-black">UniShare</h1>
        <h2 className="mt-7 text-2xl font-black">Ho so cua ban da san sang</h2>
        <p className="mx-auto mt-3 max-w-[300px] text-sm leading-6 text-[var(--app-muted)]">
          Bay gio ban co the kham pha bai viet, theo doi moi nguoi va chia se nhung trai nghiem cua minh.
        </p>
        <Button className="mt-8 px-7 font-black" onClick={() => navigate('/feed/for-you')}>
          Kham pha Feed
        </Button>
      </section>
    </main>
  );
}
