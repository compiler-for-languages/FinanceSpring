import { ReactNode } from 'react';
import { cn } from '../utils/classNames';

interface Column<T> {
  header: string;
  accessor: keyof T | ((item: T) => ReactNode);
  className?: string;
}

interface TableProps<T> {
  data: T[];
  columns: Column<T>[];
  keyExtractor: (item: T) => string | number;
  className?: string;
}

export function Table<T>({ data, columns, keyExtractor, className }: TableProps<T>) {
  return (
    <div className={cn("overflow-x-auto rounded-xl border border-gray-200 bg-white shadow-sm", className)}>
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            {columns.map((col, i) => (
              <th
                key={i}
                scope="col"
                className={cn("px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider", col.className)}
              >
                {col.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {data.length > 0 ? (
            data.map((item) => (
              <tr key={keyExtractor(item)} className="hover:bg-gray-50 transition-colors">
                {columns.map((col, i) => (
                  <td key={i} className={cn("px-6 py-4 whitespace-nowrap text-sm text-gray-900", col.className)}>
                    {typeof col.accessor === 'function'
                      ? col.accessor(item)
                      : String(item[col.accessor] as any)}
                  </td>
                ))}
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan={columns.length} className="px-6 py-8 text-center text-sm text-gray-500">
                No data available
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
