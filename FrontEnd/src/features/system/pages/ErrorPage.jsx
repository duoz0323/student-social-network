import { useNavigate } from 'react-router-dom';
import logo from '../../../assets/brand/logo.png';
import Button from '../../../components/common/Button.jsx';

export default function ErrorPage({ code, title, description }) {
  const navigate = useNavigate();

  return (
    <main className="flex min-h-screen items-center justify-center bg-zinc-50 px-4">
      <section className="w-full max-w-md rounded-3xl border border-zinc-200 bg-white p-8 text-center">
        <img src={logo} alt="UniShare" className="mx-auto h-12 w-12 rounded-xl object-contain" />
        <p className="mt-6 text-6xl font-black text-zinc-950">{code}</p>
        <h1 className="mt-4 text-2xl font-black">{title}</h1>
        <p className="mt-2 text-sm text-zinc-500">{description}</p>
        <div className="mt-6 flex justify-center gap-2">
          <Button variant="secondary" onClick={() => navigate(-1)}>Quay lai</Button>
          <Button onClick={() => navigate('/feed/for-you')}>Ve trang chu</Button>
        </div>
      </section>
    </main>
  );
}
