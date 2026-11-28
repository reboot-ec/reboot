import { Button } from '../atoms/Button';

export function Pagination({ page, totalPages, onPage }: { page: number; totalPages: number; onPage: (page: number) => void }) {
  if (page === 0 && totalPages <= 1) return null;
  return (
    <nav className="pagination" aria-label="Pagination">
      <Button disabled={page === 0} onClick={() => onPage(page - 1)}>Previous</Button>
      <span>Page {page + 1}{totalPages > page && ` of ${totalPages}`}</span>
      <Button disabled={page + 1 >= totalPages} onClick={() => onPage(page + 1)}>Next</Button>
    </nav>
  );
}
