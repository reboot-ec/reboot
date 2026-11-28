import { DashboardActionCard } from '../components/molecules/DashboardActionCard';
import { CitizenImpactSummary } from '../components/organisms/CitizenImpactSummary';
import { CitizenWelcome } from '../components/organisms/CitizenWelcome';
import { useAuth } from '../context/AuthContext';

export function CitizenDashboardPage() {
  const { user } = useAuth();
  if (!user) return null;

  return (
    <>
      <CitizenWelcome fullName={user.fullName} />
      <section className="card-grid" aria-label="Device recovery options">
        <DashboardActionCard title="Donate a Device"
          description="Give working electronics to students, NGOs, and communities"
          to="/organizations?type=NGO&type=INSTITUTION" />
        <DashboardActionCard title="Find a Repair Shop"
          description="Extend your device lifespan with help from repair technicians"
          to="/organizations?type=REPAIR_SHOP" />
        <DashboardActionCard title="Recycle E-Waste"
          description="Find recycling centers that accept broken electronics"
          to="/organizations?type=RECYCLING_CENTER" />
      </section>
      <CitizenImpactSummary />
    </>
  );
}
