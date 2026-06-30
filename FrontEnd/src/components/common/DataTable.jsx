import { EmptyState } from './StateBlock.jsx';

export default function DataTable({ columns, rows, emptyText = 'Khong co du lieu' }) {
  if (!rows.length) return <EmptyState title={emptyText} description="Thu thay doi bo loc hoac tu khoa tim kiem." />;

  return (
    <div className="overflow-hidden rounded-2xl border border-zinc-200 bg-white">
      <table className="w-full min-w-[720px] border-collapse text-left text-sm">
        <thead className="bg-zinc-50 text-xs uppercase text-zinc-500">
          <tr>
            {columns.map((column) => (
              <th key={column.key} className="px-4 py-3 font-bold">
                {column.label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-zinc-100">
          {rows.map((row) => (
            <tr key={row.id} className="hover:bg-zinc-50">
              {columns.map((column) => (
                <td key={column.key} className="px-4 py-3 text-zinc-700">
                  {column.render ? column.render(row) : row[column.key]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
