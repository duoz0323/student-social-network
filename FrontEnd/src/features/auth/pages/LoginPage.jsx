import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { useApp } from '../../../contexts/AppContext.jsx';
import AuthLayout from '../components/AuthLayout.jsx';
import AuthForm from '../components/AuthForm.jsx';

export default function LoginPage() {
  const { login, handleFutureSocialAuth } = useApp();
  const navigate = useNavigate();
  const [form, setForm] = useState({ identifier: '', password: '' });
  const [message, setMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function submit() {
    if (!form.identifier.trim() || !form.password) {
      setMessage('Vui lòng nhập email/số điện thoại và mật khẩu.');
      return;
    }

    setSubmitting(true);
    setMessage('');
    const result = await login(form.identifier, form.password);
    setSubmitting(false);

    if (!result.ok) {
      setMessage(result.message);
      return;
    }

    if (!result.profileCompleted) {
      navigate('/onboarding/profile');
      return;
    }
    navigate(result.role === 'ADMIN' ? '/admin' : '/feed/for-you');
  }

  function showFutureMessage(providerName) {
    const result = handleFutureSocialAuth(providerName);
    setMessage(result.message);
  }

  return (
    <AuthLayout>
      <AuthForm 
        type="login"
        form={form}
        setForm={setForm}
        onSubmit={submit}
        submitting={submitting}
        message={message}
        showFutureMessage={showFutureMessage}
      />
    </AuthLayout>
  );
}
