import { Button } from '../atoms/Button';

export function Pagination({ page, totalPages, onPage }: { page: number; totalPages: number; onPage: (page: number) => void }) {
  if (page === 0 && totalPages <= 1) return null;
  return (
    <nav className="pagination" aria-label="Paginación">
      <Button disabled={page === 0} onClick={() => onPage(page - 1)}>Anterior</Button>
      <span>Página {page + 1}{totalPages > page && ` de ${totalPages}`}</span>
      <Button disabled={page + 1 >= totalPages} onClick={() => onPage(page + 1)}>Siguiente</Button>
    </nav>
  );
}
