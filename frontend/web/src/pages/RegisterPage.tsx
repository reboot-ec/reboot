import { useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { identity } from '../context/api';
import { AuthCard } from '../components/organisms/AuthCard';
import { FormField } from '../components/molecules/FormField';
import { fieldError } from '../components/molecules/ErrorBanner';
import { ProfileIntentField } from '../components/molecules/ProfileIntentField';
import { saveProfileIntent } from '../context/profileIntent';

export function RegisterPage() {
  const navigate = useNavigate();
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<unknown>(null);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setPending(true);
    setError(null);
    try {
      const result = await identity.register({
        email: String(form.get('email')).trim(),
        username: String(form.get('username')).trim(),
        fullName: String(form.get('fullName')).trim(),
        password: String(form.get('password')),
      });
      if (!result?.data.id) throw new Error('Missing registered user');
      const intent = form.get('profileIntent') === 'representative' ? 'representative' : 'citizen';
      saveProfileIntent(result.data.id, intent);
      navigate('/login', { replace: true, state: { userId: result.data.id, intent } });
    } catch (failure) {
      setError(failure);
    } finally {
      setPending(false);
    }
  }

  return (
    <AuthCard title="Create account" submitLabel="Sign up" onSubmit={submit} pending={pending} error={error}
      footer={<Link to="/login">Sign in</Link>}>
      <ProfileIntentField />
      <FormField label="Full name" name="fullName" autoComplete="name" maxLength={255} error={fieldError(error, 'fullName')} required />
      <FormField label="Email address" name="email" type="email" autoComplete="email" maxLength={255} error={fieldError(error, 'email')} required />
      <FormField label="Username" name="username" autoComplete="username" minLength={3} maxLength={150} error={fieldError(error, 'username')} required />
      <FormField label="Password" name="password" type="password" autoComplete="new-password" minLength={10} maxLength={72}
        error={fieldError(error, 'password')} required />
    </AuthCard>
  );
}
