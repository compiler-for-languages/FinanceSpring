import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Search, Filter, Edit2, Trash2, ChevronLeft, ChevronRight } from 'lucide-react';
import toast from 'react-hot-toast';
import { apiClient } from '../api/client';
import { RecordItem } from '../types';
import { useAuthStore } from '../store/useAuthStore';
import { Table } from '../components/Table';
import { Button } from '../components/Button';
import { Card } from '../components/Card';
import { Input } from '../components/Input';
import { Modal } from '../components/Modal';

export const Records = () => {
  const { user } = useAuthStore();
  const queryClient = useQueryClient();
  const isAdmin = user?.role === 'ADMIN';
  const isAnalyst = user?.role === 'ANALYST';

  const [page, setPage] = useState(1);
  const [filters, setFilters] = useState({ search: '', type: '' });
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<string | number | null>(null);
  
  const { data: usersData } = useQuery({
    queryKey: ['admin', 'users'],
    queryFn: () => apiClient('/admin/users/all'),
    enabled: isAdmin,
  });
  
  const [formData, setFormData] = useState({
    amount: '',
    type: 'EXPENSE' as 'INCOME' | 'EXPENSE',
    category: '',
    date: new Date().toISOString().split('T')[0],
    description: '',
    userId: user?.id || '',
  });

  const { data: records, isLoading } = useQuery<RecordItem[]>({
    queryKey: ['records', filters],
    queryFn: () => {
      const params = new URLSearchParams();
      if (filters.search) params.append('category', filters.search);
      if (filters.type) params.append('type', filters.type);
      
      let endpoint = '/records/all';
      if (filters.search || filters.type) {
        endpoint = '/records/filter';
      }
      return apiClient(`${endpoint}?${params.toString()}`);
    },
  });

  const ITEMS_PER_PAGE = 10;
  const paginatedRecords = Array.isArray(records) 
    ? records.slice((page - 1) * ITEMS_PER_PAGE, page * ITEMS_PER_PAGE) 
    : [];
  const totalPages = Array.isArray(records) ? Math.ceil(records.length / ITEMS_PER_PAGE) : 1;

  const addMutation = useMutation({
    mutationFn: (payload: any) => {
      const targetUserId = isAdmin ? payload.userId : user?.id;
      return apiClient(`/records/add?userId=${targetUserId}`, {
        method: 'POST',
        body: JSON.stringify(payload),
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['records'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      toast.success('Record added successfully');
      closeModal();
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to add record');
    }
  });

  const editMutation = useMutation({
    mutationFn: ({ id, record }: { id: string | number; record: any }) =>
      apiClient(`/records/update/${id}`, {
        method: 'PUT',
        body: JSON.stringify(record),
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['records'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      toast.success('Record updated successfully');
      closeModal();
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to update record');
    }
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string | number) =>
      apiClient(`/records/${id}`, {
        method: 'DELETE',
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['records'] });
      queryClient.invalidateQueries({ queryKey: ['dashboard'] });
      toast.success('Record deleted successfully');
    },
    onError: (error: any) => {
      toast.error(error.message || 'Failed to delete record');
    }
  });

  const closeModal = () => {
    setIsModalOpen(false);
    setEditingId(null);
    setFormData({
      amount: '',
      type: 'EXPENSE' as 'INCOME' | 'EXPENSE',
      category: '',
      date: new Date().toISOString().split('T')[0],
      description: '',
      userId: user?.id || '',
    });
  };

  const openEditModal = (record: any) => {
    setEditingId(record.id);
    setFormData({
      amount: record.amount.toString(),
      type: record.type,
      category: record.category,
      date: new Date(record.recordDate || record.date).toISOString().split('T')[0],
      description: record.description || record.notes || '',
      userId: record.userId || user?.id || '',
    });
    setIsModalOpen(true);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const payload = {
      amount: Number(formData.amount),
      type: formData.type,
      category: formData.category,
      recordDate: formData.date,
      notes: formData.description,
      userId: isAdmin ? formData.userId : undefined,
    };
    if (editingId) {
      editMutation.mutate({ id: editingId, record: payload });
    } else {
      addMutation.mutate(payload);
    }
  };

  const handleDelete = (id: string | number) => {
    if (window.confirm('Are you sure you want to delete this record?')) {
      deleteMutation.mutate(id);
    }
  };

  const columns: any[] = [
    {
      header: 'Date',
      accessor: (item: any) => {
        const dateStr = item.recordDate || item.date;
        const date = dateStr ? new Date(dateStr) : null;
        return date && !isNaN(date.getTime()) ? date.toLocaleDateString("en-IN") : "-";
      },
    },
    {
      header: 'Description',
      accessor: (item: any) => item.description || item.notes || 'N/A',
    },
    {
      header: 'Category',
      accessor: (item: RecordItem) => (
        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
          {item.category}
        </span>
      ),
    },
    {
      header: 'Type',
      accessor: (item: RecordItem) => (
        <span
          className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
            item.type === 'INCOME' ? 'bg-[#E6F6EE] text-[#02A95C]' : 'bg-red-50 text-red-700'
          }`}
        >
          {item.type}
        </span>
      ),
    },
    {
      header: 'Amount',
      accessor: (item: RecordItem) => (
        <span className={`font-medium ${item.type === 'INCOME' ? 'text-[#02A95C]' : 'text-red-600'}`}>
          ₹{item.amount.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
        </span>
      ),
    },
  ];

  if (isAdmin || isAnalyst) {
    columns.splice(2, 0, {
      header: 'User',
      accessor: (item: any) => item.userName || 'N/A',
    });
  }

  if (isAdmin) {
    columns.push({
      header: 'Actions',
      accessor: (item: RecordItem) => (
        <div className="flex items-center gap-2">
          <button
            onClick={() => openEditModal(item)}
            className="p-1 text-gray-400 hover:text-blue-600 transition-colors"
            title="Edit"
          >
            <Edit2 className="h-4 w-4" />
          </button>
          <button
            onClick={() => handleDelete(item.id)}
            className="p-1 text-gray-400 hover:text-red-600 transition-colors"
            title="Delete"
          >
            <Trash2 className="h-4 w-4" />
          </button>
        </div>
      ),
    });
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <h1 className="text-2xl font-bold text-gray-900">Records</h1>
        {isAdmin && (
          <Button onClick={() => setIsModalOpen(true)} className="flex items-center gap-2">
            <Plus className="h-4 w-4" />
            Add Record
          </Button>
        )}
      </div>

      <Card className="p-4 sm:p-6">
        <div className="flex flex-col md:flex-row gap-4 mb-6">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-gray-400" />
            <Input
              placeholder="Search by category..."
              className="pl-10 w-full"
              value={filters.search}
              onChange={(e) => {
                setFilters((prev) => ({ ...prev, search: e.target.value }));
                setPage(1);
              }}
            />
          </div>
          <div className="flex items-center gap-2">
            <Filter className="h-5 w-5 text-gray-400" />
            <select
              className="px-4 py-2 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-[#02A95C] bg-white h-10 text-sm"
              value={filters.type}
              onChange={(e) => {
                setFilters((prev) => ({ ...prev, type: e.target.value }));
                setPage(1);
              }}
            >
              <option value="">All Types</option>
              <option value="INCOME">Income</option>
              <option value="EXPENSE">Expense</option>
            </select>
          </div>
        </div>

        {isLoading ? (
          <div className="flex justify-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-[#02A95C]"></div>
          </div>
        ) : (
          <Table
            data={paginatedRecords}
            columns={columns}
            keyExtractor={(item) => item.id}
          />
        )}
        
        <div className="flex items-center justify-between border-t border-gray-200 bg-white px-4 py-3 sm:px-6 mt-4">
          <div className="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
            <div>
              <p className="text-sm text-gray-700">
                Page <span className="font-medium">{page}</span> of <span className="font-medium">{totalPages}</span>
              </p>
            </div>
            <div>
              <nav className="isolate inline-flex -space-x-px rounded-md shadow-sm" aria-label="Pagination">
                <button 
                  onClick={() => setPage((p) => Math.max(1, p - 1))}
                  disabled={page === 1}
                  className="relative inline-flex items-center rounded-l-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0 disabled:opacity-50"
                >
                  <ChevronLeft className="h-5 w-5" />
                </button>
                <button 
                  onClick={() => setPage((p) => p + 1)}
                  disabled={page >= totalPages}
                  className="relative inline-flex items-center rounded-r-md px-2 py-2 text-gray-400 ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-20 focus:outline-offset-0 disabled:opacity-50"
                >
                  <ChevronRight className="h-5 w-5" />
                </button>
              </nav>
            </div>
          </div>
        </div>
      </Card>

      <Modal
        isOpen={isModalOpen}
        onClose={closeModal}
        title={editingId ? "Edit Record" : "Add New Record"}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          {isAdmin && (
            <div className="flex flex-col space-y-1">
              <label className="text-sm font-medium text-gray-700">Linked User</label>
              <select
                className="px-4 py-2 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-[#02A95C] bg-white"
                value={formData.userId}
                onChange={(e) => setFormData({ ...formData, userId: e.target.value })}
                required
              >
                <option value="">Select a user</option>
                {(usersData as any[])?.filter(u => u.role !== 'ADMIN').map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.name || u.email}
                  </option>
                ))}
              </select>
            </div>
          )}
          <Input
            label="Amount (₹)"
            type="number"
            step="0.01"
            required
            value={formData.amount}
            onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
            placeholder="0.00"
          />
          
          <div className="flex flex-col space-y-1">
            <label className="text-sm font-medium text-gray-700">Type</label>
            <select
              className="px-4 py-2 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-[#02A95C] bg-white"
              value={formData.type}
              onChange={(e) => setFormData({ ...formData, type: e.target.value as 'INCOME' | 'EXPENSE' })}
            >
              <option value="INCOME">Income</option>
              <option value="EXPENSE">Expense</option>
            </select>
          </div>

          <Input
            label="Category"
            required
            value={formData.category}
            onChange={(e) => setFormData({ ...formData, category: e.target.value })}
            placeholder="e.g. Salary, Groceries"
          />

          <Input
            label="Date"
            type="date"
            required
            value={formData.date}
            onChange={(e) => setFormData({ ...formData, date: e.target.value })}
          />

          <Input
            label="Description"
            value={formData.description}
            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            placeholder="Brief description"
          />

          <div className="pt-4 flex justify-end gap-3">
            <Button type="button" variant="ghost" onClick={closeModal}>
              Cancel
            </Button>
            <Button type="submit" isLoading={addMutation.isPending || editMutation.isPending}>
              {editingId ? 'Update Record' : 'Save Record'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
