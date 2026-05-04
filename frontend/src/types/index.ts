export type Role = 'ADMIN' | 'ANALYST' | 'VIEWER';

export interface User {
  id: string | number;
  email: string;
  name: string;
  role: Role;
  status: 'ACTIVE' | 'INACTIVE';
}

export interface RecordItem {
  id: string | number;
  amount: number;
  type: 'INCOME' | 'EXPENSE';
  category: string;
  date: string;
  description?: string;
  userId?: number | string;
  userName?: string;
}

export interface DashboardSummary {
  totalIncome: number;
  totalExpense: number;
  netBalance: number;
}

export interface ChartData {
  date: string;
  income: number;
  expense: number;
}

export interface CategoryData {
  category: string;
  amount: number;
}

export interface RecentActivity {
  id: string | number;
  description: string;
  date: string;
  type: 'INCOME' | 'EXPENSE';
  amount: number;
}
