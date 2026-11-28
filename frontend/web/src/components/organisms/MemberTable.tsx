import type { OrganizationMembership } from '@reboot/shared-types';
import { Badge } from '../atoms/Badge';

export function MemberTable({ members }: { members: OrganizationMembership[] }) {
  if (!members.length) return <p>No hay miembros para mostrar.</p>;

  return (
    <div className="table-scroll">
      <table aria-label="Miembros de la organización">
        <thead><tr><th scope="col">Usuario</th><th scope="col">Rol</th><th scope="col">Estado</th></tr></thead>
        <tbody>{members.map(member => (
          <tr key={member.id}>
            <td>{member.userId}</td><td>{member.role}</td><td><Badge>{member.status}</Badge></td>
          </tr>
        ))}</tbody>
      </table>
    </div>
  );
}
