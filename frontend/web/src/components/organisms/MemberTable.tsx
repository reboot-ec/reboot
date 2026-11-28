import { useState } from 'react';
import type { FormEvent } from 'react';
import type { OrganizationMembership } from '@reboot/shared-types';
import { useOrganizations } from '../../context/useOrganizations';
import { Badge } from '../atoms/Badge';
import { Button } from '../atoms/Button';
import { ErrorBanner, fieldError } from '../molecules/ErrorBanner';
import { FormField } from '../molecules/FormField';
import { SelectField } from '../molecules/SelectField';
import { memberRoles, memberStatuses } from '../molecules/organizationOptions';

interface Props {
  organizationId: string;
  members: OrganizationMembership[];
  managerRole?: 'OWNER' | 'ADMIN';
  onSaved: (member: OrganizationMembership) => void;
}

export function MemberTable({ organizationId, members, managerRole, onSaved }: Props) {
  const service = useOrganizations();
  const [pending, setPending] = useState(false);
  const [feedback, setFeedback] = useState<{ target: string; error?: unknown }>();
  const allowedRoles = memberRoles.filter(role => role !== 'OWNER' || managerRole === 'OWNER');

  async function save(event: FormEvent<HTMLFormElement>, member?: OrganizationMembership) {
    event.preventDefault();
    if (pending) return;
    const form = event.currentTarget;
    const fields = new FormData(form);
    const role = allowedRoles.find(value => value === fields.get('role'));
    if (!role) return;
    const target = member?.id ?? 'new';
    setPending(true);
    setFeedback(undefined);
    try {
      const status = memberStatuses.find(value => value === fields.get('status'));
      const saved = member
        ? await service.updateMember(organizationId, member.id, { role, status })
        : await service.addMember(organizationId, { userId: String(fields.get('userId')).trim(), role });
      onSaved(saved);
      if (!member) form.reset();
      setFeedback({ target });
    } catch (error) {
      setFeedback({ target, error });
    } finally {
      setPending(false);
    }
  }

  function result(target: string) {
    if (feedback?.target !== target) return null;
    return feedback.error
      ? <ErrorBanner error={feedback.error} />
      : <p role="status">{target === 'new' ? 'Invitación registrada.' : 'Cambios guardados.'}</p>;
  }

  return (
    <>
      {managerRole && (
        <form className="management-form" onSubmit={event => void save(event)}>
          <fieldset disabled={pending}>
            <legend>Agregar miembro</legend>
            <FormField label="ID de usuario" name="userId" required pattern="[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
              error={feedback?.target === 'new' ? fieldError(feedback.error, 'userId') : undefined} />
            <SelectField label="Rol" name="role" defaultValue="MEMBER">
              {allowedRoles.map(role => <option key={role}>{role}</option>)}
            </SelectField>
            <Button type="submit">Agregar miembro</Button>
          </fieldset>
          {result('new')}
        </form>
      )}
      {!members.length ? <p>No hay miembros para mostrar.</p> : (
        <div className="table-scroll">
          <table aria-label="Miembros de la organización">
            <thead><tr><th scope="col">Usuario</th><th scope="col">Rol</th><th scope="col">Estado</th>{managerRole && <th scope="col">Gestión</th>}</tr></thead>
            <tbody>{members.map(member => (
              <tr key={member.id}>
                <td>{member.userId}</td><td>{member.role}</td>
                <td><Badge tone={member.status === 'ACTIVE' ? 'positive' : 'muted'}>{member.status}</Badge></td>
                {managerRole && (
                  <td>
                    {(member.role !== 'OWNER' || managerRole === 'OWNER') && (
                      <form onSubmit={event => void save(event, member)}>
                        <fieldset disabled={pending} key={`${member.role}/${member.status}`}>
                          <SelectField label="Rol" name="role" defaultValue={member.role}>
                            {allowedRoles.map(role => <option key={role}>{role}</option>)}
                          </SelectField>
                          <SelectField label="Estado" name="status" defaultValue={member.status}>
                            {memberStatuses.map(status => <option key={status}>{status}</option>)}
                          </SelectField>
                          <Button type="submit">Guardar</Button>
                        </fieldset>
                        {result(member.id)}
                      </form>
                    )}
                  </td>
                )}
              </tr>
            ))}</tbody>
          </table>
        </div>
      )}
    </>
  );
}
