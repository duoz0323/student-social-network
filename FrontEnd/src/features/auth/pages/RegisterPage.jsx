import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { useApp } from '../../../contexts/AppContext.jsx';
import AuthForm from '../components/AuthForm.jsx';
import AuthLayout from '../components/AuthLayout.jsx';

export default function RegisterPage() {
  const { register, handleFutureSocialAuth } = useApp();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    identifier: '',
    password: '',
    confirmPassword: '',
    acceptTerms: false
  });
  const [message, setMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function submit() {
    if (!form.identifier.trim() || !form.password) {
      setMessage('Vui lòng nhập đầy đủ thông tin.');
      return;
    }
    if (form.password !== form.confirmPassword) {
      setMessage('Mật khẩu xác nhận không khớp.');
      return;
    }
    if (!form.acceptTerms) {
      setMessage('Vui lòng đồng ý với điều khoản sử dụng.');
      return;
    }

    setSubmitting(true);
    setMessage('');
    const result = await register(form);
    setSubmitting(false);

    if (!result.ok) {
      setMessage(result.message);
      return;
    }
    navigate('/onboarding/profile');
  }

  function showFutureMessage(providerName) {
    const result = handleFutureSocialAuth(providerName);
    setMessage(result.message);
  }

  return (
    <AuthLayout>
      <AuthForm 
        type="register"
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
