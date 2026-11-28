import { Outlet } from 'react-router-dom';
import { Navbar } from '../organisms/Navbar';

export function DashboardLayout() {
  return <><Navbar /><main className="container dashboard"><Outlet /></main></>;
}
