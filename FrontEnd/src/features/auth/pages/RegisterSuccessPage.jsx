import { useNavigate } from 'react-router-dom';
import logo from '../../../assets/brand/logo.png';
import Button from '../../../components/common/Button.jsx';

export default function RegisterSuccessPage() {
  const navigate = useNavigate();

  return (
    <main className="flex min-h-screen items-center justify-center bg-zinc-50 px-4">
      <section className="w-full max-w-md rounded-3xl border border-zinc-200 bg-white p-8 text-center shadow-sm">
        <img src={logo} alt="UniShare" className="mx-auto h-14 w-14 rounded-xl object-contain" />
        <h1 className="mt-6 text-2xl font-black text-zinc-950">Dang ky thanh cong</h1>
        <p className="mt-2 text-sm text-zinc-500">Tai khoan da san sang. Ban co the dang nhap va cap nhat ho so.</p>
        <Button className="mt-6 w-full" onClick={() => navigate('/login')}>Den trang dang nhap</Button>
      </section>
    </main>
  );
}
