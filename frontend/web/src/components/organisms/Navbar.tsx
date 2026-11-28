import { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Button } from '../atoms/Button';
import { ErrorBanner } from '../molecules/ErrorBanner';

export function Navbar() {
  const { user, logout, loading } = useAuth();
  const { pathname, search } = useLocation();
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
      <nav className="container nav" aria-label="Main navigation">
        <Link className="brand" to="/organizations">REBOOT</Link>
        <Link to="/organizations">Explore Centers &amp; Partners</Link>
        {user ? (
          <>
            <Link to="/organizations/mine">My Organizations</Link>
            <span className="nav-user">{user.fullName} <small>{user.email}</small></span>
            <Button disabled={pending} onClick={signOut}>Sign out</Button>
          </>
        ) : !loading && <Link to="/login" state={{ from: pathname + search }}>Sign in</Link>}
      </nav>
      <ErrorBanner error={error} />
    </header>
  );
}
