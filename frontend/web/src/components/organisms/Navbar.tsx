import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Button } from '../atoms/Button';
import { ErrorBanner } from '../molecules/ErrorBanner';

export function Navbar() {
  const { user, logout } = useAuth();
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<unknown>(null);

  async function signOut() {
    setPending(true);
    setError(null);
    try {
      await logout();
    } catch (failure) {
      setError(failure);
    } finally {
      setPending(false);
    }
  }

  return (
    <header className="site-header">
      <nav className="container nav" aria-label="Navegación principal">
        <Link className="brand" to="/organizations">REBOOT</Link>
        <Link to="/organizations">Organizaciones</Link>
        {user && <span className="nav-user">{user.fullName} <small>{user.email}</small></span>}
        <Button disabled={pending} onClick={signOut}>Cerrar sesión</Button>
      </nav>
      <ErrorBanner error={error} />
    </header>
  );
}
