import { useCallback } from 'react';
import { useOrganizations } from '../../context/useOrganizations';
import { useApiQuery } from '../../hooks/useApiQuery';

export function OrganizationOperationalMetrics({ organizationId }: { organizationId: string }) {
  const service = useOrganizations();
  const loadMembers = useCallback((signal: AbortSignal) => service.listMembers(organizationId, signal), [service, organizationId]);
  const { data, pending } = useApiQuery(loadMembers);
  const activeCount = data?.filter(member => member.status === 'ACTIVE').length;

  return (
    <section className="citizen-impact" aria-labelledby="operational-metrics">
      <h2 id="operational-metrics">Operational Metrics</h2>
      <dl className="card-grid">
        <div className="card"><dt>Devices Received</dt><dd>0</dd></div>
        <div className="card"><dt>Devices Refurbished</dt><dd>0</dd></div>
        <div className="card">
          <dt>Active Team Members</dt>
          <dd aria-live="polite">{pending ? 'Loading…' : activeCount ?? 'Unavailable'}</dd>
        </div>
      </dl>
    </section>
  );
}
