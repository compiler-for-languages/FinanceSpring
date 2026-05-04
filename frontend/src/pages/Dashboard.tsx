import { useQuery } from '@tanstack/react-query';
import { TrendingUp, TrendingDown, DollarSign } from 'lucide-react';
import { 
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer,
  PieChart, Pie, Cell 
} from 'recharts';
import { Card } from '../components/Card';
import { apiClient } from '../api/client';
import { DashboardSummary, RecentActivity, ChartData, CategoryData } from '../types';

import { useAuthStore } from '../store/useAuthStore';

const COLORS = ['#02A95C', '#3B82F6', '#EF4444', '#F59E0B', '#8B5CF6', '#EC4899'];

const formatCurrency = (value: number) => {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 2,
  }).format(value);
};

const formatDate = (dateStr: string) => {
  const date = new Date(dateStr);
  return isNaN(date.getTime()) ? '-' : date.toLocaleDateString('en-IN');
};

export const Dashboard = () => {
  const { user } = useAuthStore();
  const isAdminOrAnalyst = user?.role === 'ADMIN' || user?.role === 'ANALYST';

  const { data: summary, isLoading: isSummaryLoading } = useQuery<DashboardSummary>({
    queryKey: ['dashboard', 'summary', user?.role],
    queryFn: () => apiClient(isAdminOrAnalyst ? '/dashboard/company-summary' : '/dashboard/summary'),
  });

  const { data: recentActivity, isLoading: isActivityLoading } = useQuery<RecentActivity[]>({
    queryKey: ['dashboard', 'recent-activity'],
    queryFn: () => apiClient('/dashboard/recent-activity'),
  });

  const { data: categoryData, isLoading: isCategoryLoading } = useQuery<CategoryData[]>({
    queryKey: ['dashboard', 'category-analysis'],
    queryFn: () => apiClient('/dashboard/category-analysis'),
  });

  const { data: trendsData, isLoading: isTrendsLoading } = useQuery<ChartData[]>({
    queryKey: ['dashboard', 'trends'],
    queryFn: () => apiClient('/dashboard/trends'),
  });

  if (isSummaryLoading || isActivityLoading || isCategoryLoading || isTrendsLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#02A95C]"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Dashboard Overview</h1>
      </div>

      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
        <Card className="flex items-center p-6 border-l-4 border-[#02A95C]">
          <div className="flex-1">
            <p className="text-sm font-medium text-gray-500">Total Income</p>
            <p className="mt-1 text-2xl font-bold text-gray-900">
              {formatCurrency(summary?.totalIncome || 0)}
            </p>
          </div>
          <div className="rounded-full bg-[#E6F6EE] p-3">
            <TrendingUp className="h-6 w-6 text-[#02A95C]" />
          </div>
        </Card>

        <Card className="flex items-center p-6 border-l-4 border-red-500">
          <div className="flex-1">
            <p className="text-sm font-medium text-gray-500">Total Expense</p>
            <p className="mt-1 text-2xl font-bold text-gray-900">
              {formatCurrency(summary?.totalExpense || 0)}
            </p>
          </div>
          <div className="rounded-full bg-red-50 p-3">
            <TrendingDown className="h-6 w-6 text-red-500" />
          </div>
        </Card>

        <Card className="flex items-center p-6 border-l-4 border-blue-500">
          <div className="flex-1">
            <p className="text-sm font-medium text-gray-500">Net Balance</p>
            <p className="mt-1 text-2xl font-bold text-gray-900">
              {formatCurrency(summary?.netBalance || 0)}
            </p>
          </div>
          <div className="rounded-full bg-blue-50 p-3">
            <DollarSign className="h-6 w-6 text-blue-500" />
          </div>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card className="h-[400px] flex flex-col">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Income vs Expense Trends</h2>
          {trendsData && trendsData.length > 0 ? (
            <div className="flex-1 min-h-0">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart
                  data={trendsData}
                  margin={{ top: 10, right: 10, left: 0, bottom: 0 }}
                >
                  <CartesianGrid strokeDasharray="3 3" vertical={false} />
                  <XAxis 
                    dataKey="date" 
                    tickFormatter={(val) => new Date(val).toLocaleDateString('en-IN', { month: 'short', day: 'numeric' })}
                    fontSize={12}
                    tick={{ fill: '#6B7280' }}
                  />
                  <YAxis 
                    fontSize={12}
                    tick={{ fill: '#6B7280' }}
                    tickFormatter={(val) => `₹${val}`}
                  />
                  <Tooltip 
                    formatter={(value: number) => [formatCurrency(value), '']}
                    labelFormatter={(label) => formatDate(label)}
                    contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}
                  />
                  <Legend verticalAlign="top" height={36}/>
                  <Bar dataKey="income" fill="#02A95C" name="Income" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="expense" fill="#EF4444" name="Expense" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div className="flex-1 flex items-center justify-center text-gray-500">
              No data available
            </div>
          )}
        </Card>

        <Card className="h-[400px] flex flex-col">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Category Breakdown</h2>
          {categoryData && categoryData.length > 0 ? (
            <div className="flex-1 min-h-0">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={categoryData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={100}
                    paddingAngle={5}
                    dataKey="amount"
                    nameKey="category"
                    label={({ category, percent }) => `${category} (${(percent * 100).toFixed(0)}%)`}
                  >
                    {categoryData.map((_, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip 
                    formatter={(value: number) => [formatCurrency(value), 'Amount']}
                    contentStyle={{ borderRadius: '12px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}
                  />
                  <Legend verticalAlign="bottom" height={36}/>
                </PieChart>
              </ResponsiveContainer>
            </div>
          ) : (
            <div className="flex-1 flex items-center justify-center text-gray-500">
              No data available
            </div>
          )}
        </Card>

        <Card className="lg:col-span-2">
          <h2 className="text-lg font-semibold text-gray-900 mb-4">Recent Activity</h2>
          {recentActivity && recentActivity.length > 0 ? (
            <ul className="divide-y divide-gray-100">
              {recentActivity.map((activity) => (
                <li key={activity.id} className="py-4 flex items-center justify-between">
                  <div className="flex items-center gap-4">
                    <div className={`p-2 rounded-full ${activity.type === 'INCOME' ? 'bg-[#E6F6EE] text-[#02A95C]' : 'bg-red-50 text-red-500'}`}>
                      {activity.type === 'INCOME' ? <TrendingUp className="h-5 w-5" /> : <TrendingDown className="h-5 w-5" />}
                    </div>
                    <div>
                      <p className="text-sm font-medium text-gray-900">{activity.description}</p>
                      <p className="text-xs text-gray-500">{formatDate(activity.date)}</p>
                    </div>
                  </div>
                  <span className={`text-sm font-semibold ${activity.type === 'INCOME' ? 'text-[#02A95C]' : 'text-red-500'}`}>
                    {activity.type === 'INCOME' ? '+' : '-'}₹{activity.amount.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                  </span>
                </li>
              ))}
            </ul>
          ) : (
            <div className="py-8 text-center text-gray-500">
              No data available
            </div>
          )}
        </Card>
      </div>
    </div>
  );
};
