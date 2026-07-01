import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import logo from '../../../assets/brand/logo.png';
import Button from '../../../components/common/Button.jsx';
import Avatar from '../../../components/common/Avatar.jsx';
import { useApp } from '../../../contexts/AppContext.jsx';

function todayIsoDate() {
  return new Date().toISOString().slice(0, 10);
}

export default function OnboardingProfilePage() {
  const { currentUser, completeOnboarding } = useApp();
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [form, setForm] = useState({
    displayName: currentUser?.profile?.displayName ?? '',
    avatarUrl: currentUser?.profile?.avatarUrl ?? '',
    dateOfBirth: currentUser?.profile?.dateOfBirth ?? '',
    bio: currentUser?.profile?.bio ?? '',
  });
  const [message, setMessage] = useState('');

  function goStepTwo() {
    if (!form.displayName.trim()) {
      setMessage('Ten hien thi la bat buoc de hoan tat ho so.');
      return;
    }
    setMessage('');
    setStep(2);
  }

  function chooseAvatar(event) {
    const file = event.target.files?.[0];
    if (!file) return;

    // FileReader chi tao preview cho mock Frontend, khong upload API that trong MVP hien tai.
    const reader = new FileReader();
    reader.onload = () => setForm((prev) => ({ ...prev, avatarUrl: String(reader.result) }));
    reader.readAsDataURL(file);
  }

  function finishOnboarding() {
    if (form.dateOfBirth && form.dateOfBirth > todayIsoDate()) {
      setMessage('Ngay sinh khong duoc lon hon ngay hien tai.');
      return;
    }

    const result = completeOnboarding(form);
    if (!result.ok) {
      setMessage(result.message);
      setStep(1);
      return;
    }
    navigate('/onboarding/success');
  }

  return (
    <main className="auth-pattern flex min-h-screen flex-col items-center justify-center px-4 py-8">
      <section className="stitch-card-shadow w-full max-w-[430px] rounded-[10px] border border-[var(--app-border)] bg-white px-8 py-8">
        <div className="mb-7 flex flex-col items-center text-center">
          <span className="rounded-full bg-zinc-100 px-3 py-1 text-xs font-black text-zinc-600">Buoc {step}/3</span>
          <img src={logo} alt="UniShare" className="mt-5 h-16 w-16 object-contain" />
          <h1 className="mt-2 text-base font-black">UniShare</h1>
        </div>

        {step === 1 ? (
          <div>
            <h2 className="text-center text-xl font-black">Ban muon moi nguoi goi minh la gi?</h2>
            <p className="mt-2 text-center text-sm text-[var(--app-muted)]">
              Ten nay se duoc hien thi tren ho so va cac bai viet cua ban.
            </p>
            <label className="mt-6 block text-sm font-black">
              Ten hien thi
              <input
                value={form.displayName}
                onChange={(event) => setForm({ ...form, displayName: event.target.value })}
                placeholder="Nhap ten hien thi"
                className="mt-2 h-[48px] w-full rounded-none border border-[var(--app-border-strong)] px-4 text-sm outline-none focus:border-[var(--app-text)]"
              />
            </label>
            {message ? <p className="mt-3 rounded-xl bg-red-50 px-3 py-2 text-sm text-red-700">{message}</p> : null}
            <Button className="mt-5 min-h-[48px] w-full rounded-none font-black" onClick={goStepTwo}>
              Tiep tuc
            </Button>
          </div>
        ) : null}

        {step === 2 ? (
          <div className="text-center">
            <h2 className="text-xl font-black">Them anh dai dien</h2>
            <p className="mt-2 text-sm text-[var(--app-muted)]">Giup ban be de dang nhan ra ban hon.</p>
            <div className="mt-7 flex justify-center">
              <Avatar src={form.avatarUrl} name={form.displayName || 'UniShare'} size="lg" />
            </div>
            <label className="mx-auto mt-4 inline-flex cursor-pointer rounded-full border border-[var(--app-border)] px-4 py-2 text-sm font-black hover:bg-zinc-50">
              Chon anh
              <input type="file" accept="image/*" className="sr-only" onChange={chooseAvatar} />
            </label>
            <Button className="mt-8 min-h-[48px] w-full font-black" onClick={() => setStep(3)}>
              Tiep tuc
            </Button>
            <button type="button" className="mt-5 text-sm font-black text-zinc-600" onClick={() => setStep(3)}>
              Bo qua
            </button>
          </div>
        ) : null}

        {step === 3 ? (
          <div>
            <h2 className="text-center text-xl font-black">Cho moi nguoi biet them ve ban</h2>
            <label className="mt-6 block text-sm font-black">
              Ngay sinh (Tuy chon)
              <input
                type="date"
                value={form.dateOfBirth}
                max={todayIsoDate()}
                onChange={(event) => setForm({ ...form, dateOfBirth: event.target.value })}
                className="mt-2 h-[48px] w-full rounded-[var(--radius-input)] border border-[var(--app-border-strong)] px-4 text-sm outline-none focus:border-[var(--app-text)]"
              />
            </label>
            <label className="mt-5 block text-sm font-black">
              Gioi thieu (Tuy chon)
              <textarea
                value={form.bio}
                onChange={(event) => setForm({ ...form, bio: event.target.value })}
                placeholder="Chia se mot chut ve ban than, so thich hoac cuoc song sinh vien cua ban..."
                className="mt-2 min-h-[110px] w-full resize-none rounded-[var(--radius-input)] border border-[var(--app-border-strong)] px-4 py-3 text-sm outline-none focus:border-[var(--app-text)]"
              />
            </label>
            {message ? <p className="mt-3 rounded-xl bg-red-50 px-3 py-2 text-sm text-red-700">{message}</p> : null}
            <Button className="mt-7 min-h-[48px] w-full font-black" onClick={finishOnboarding}>
              Hoan tat ho so
            </Button>
            <button type="button" className="mt-5 block w-full text-sm font-black text-zinc-600" onClick={finishOnboarding}>
              Bo qua
            </button>
          </div>
        ) : null}
      </section>
    </main>
  );
}
