import { useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { AuthCard } from '../components/organisms/AuthCard';
import { FormField } from '../components/molecules/FormField';
import { fieldError } from '../components/molecules/ErrorBanner';
import { Label } from '../components/atoms/Label';

export function LoginPage() {
  const { login, sessionError } = useAuth();
  const { state } = useLocation();
  const navigate = useNavigate();
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<unknown>(null);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    setPending(true);
    setError(null);
    try {
      await login({
        usernameOrEmail: String(form.get('usernameOrEmail')).trim(),
        password: String(form.get('password')),
        authProvider: form.get('authProvider') === 'ACTIVE_DIRECTORY' ? 'ACTIVE_DIRECTORY' : 'LOCAL',
      });
      const from: unknown = state?.from;
      navigate(typeof from === 'string' && /^\/organizations(?:[/?]|$)/.test(from) ? from : '/organizations', { replace: true });
    } catch (failure) {
      setError(failure);
    } finally {
      setPending(false);
    }
  }

  return (
    <AuthCard title="Iniciar sesión" submitLabel="Entrar" onSubmit={submit} pending={pending} error={error ?? (!pending && sessionError ? sessionError : null)}
      footer={<Link to="/register">Crear cuenta</Link>}>
      <div className="field">
        <Label htmlFor="auth-provider">Proveedor de autenticación</Label>
        <select id="auth-provider" name="authProvider" defaultValue="LOCAL">
          <option value="LOCAL">Local</option>
          <option value="ACTIVE_DIRECTORY">Active Directory</option>
        </select>
      </div>
      <FormField label="Usuario o correo electrónico" name="usernameOrEmail" autoComplete="username" maxLength={255}
        error={fieldError(error, 'usernameOrEmail')} required />
      <FormField label="Contraseña" name="password" type="password" autoComplete="current-password" maxLength={72}
        error={fieldError(error, 'password')} required />
    </AuthCard>
  );
}
