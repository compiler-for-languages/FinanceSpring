import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Shield, ShieldAlert, UserCheck, Plus } from 'lucide-react';
import toast from 'react-hot-toast';
import { apiClient } from '../api/client';
import { User, Role } from '../types';
import { Table } from '../components/Table';
import { Card } from '../components/Card';
import { Button } from '../components/Button';
import { Input } from '../components/Input';
import { Modal } from '../components/Modal';

export const AdminUsers = () => {
  const queryClient = useQueryClient();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<string | number | null>(null);
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    role: 'VIEWER' as Role,
  });

  const { data: users, isLoading } = useQuery<User[]>({
    queryKey: ['admin', 'users'],
    queryFn: () => apiClient('/admin/users/all'),
  });

  const statusMutation = useMutation({
    mutationFn: ({ id, status }: { id: string | number; status: 'ACTIVE' | 'INACTIVE' }) =>
      apiClient(`/admin/users/${id}/status?status=${status}`, {
        method: 'PATCH',
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] });
      toast.success('User status updated');
    },
    onError: (err: any) => toast.error(err.message || 'Failed to update status'),
  });

  const roleMutation = useMutation({
    mutationFn: ({ id, role }: { id: string | number; role: Role }) =>
      apiClient(`/admin/users/${id}/role?role=${role}`, {
        method: 'PATCH',
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] });
      toast.success('User role updated');
    },
    onError: (err: any) => toast.error(err.message || 'Failed to update role'),
  });

  const addUserMutation = useMutation({
    mutationFn: (newUser: any) =>
      apiClient('/admin/users/add', {
        method: 'POST',
        body: JSON.stringify(newUser),
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] });
      toast.success('User added successfully');
      closeModal();
    },
    onError: (err: any) => toast.error(err.message || 'Failed to add user'),
  });

  const editUserMutation = useMutation({
    mutationFn: ({ id, user }: { id: string | number; user: any }) =>
      apiClient(`/admin/users/${id}`, {
        method: 'PUT',
        body: JSON.stringify(user),
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] });
      toast.success('User updated successfully');
      closeModal();
    },
    onError: (err: any) => toast.error(err.message || 'Failed to update user'),
  });

  const deleteUserMutation = useMutation({
    mutationFn: (id: string | number) =>
      apiClient(`/admin/users/${id}`, {
        method: 'DELETE',
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin', 'users'] });
      toast.success('User deleted successfully');
    },
    onError: (err: any) => toast.error(err.message || 'Failed to delete user'),
  });

  const handleStatusToggle = (user: User) => {
    if (user.role === 'ADMIN') {
      toast.error('Cannot change status of ADMIN');
      return;
    }
    const newStatus = user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    statusMutation.mutate({ id: user.id, status: newStatus });
  };

  const handleRoleChange = (id: string | number, newRole: Role) => {
    if (newRole === 'ADMIN') {
      toast.error('Admin creation is not allowed');
      return;
    }
    roleMutation.mutate({ id, role: newRole });
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setEditingId(null);
    setFormData({ name: '', email: '', password: '', role: 'VIEWER' });
  };

  const openEditModal = (user: User) => {
    if (user.role === 'ADMIN') {
      toast.error('Cannot edit ADMIN user');
      return;
    }
    setEditingId(user.id);
    setFormData({
      name: user.name || '',
      email: user.email,
      password: '', // Leave blank for edit, only update if provided
      role: user.role,
    });
    setIsModalOpen(true);
  };

  const handleAddSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (formData.role === 'ADMIN') {
      toast.error('Admin creation is not allowed');
      return;
    }
    if (editingId) {
      editUserMutation.mutate({ id: editingId, user: formData });
    } else {
      addUserMutation.mutate(formData);
    }
  };

  const handleDelete = (id: string | number, role: Role) => {
    if (role === 'ADMIN') {
      toast.error('Cannot delete ADMIN user');
      return;
    }
    if (window.confirm('Are you sure you want to delete this user? This will also delete all their financial records.')) {
      deleteUserMutation.mutate(id);
    }
  };

  const columns = [
    {
      header: 'Name',
      accessor: (user: User) => (
        <div className="flex items-center">
          <div className="h-10 w-10 flex-shrink-0 rounded-full bg-[#E6F6EE] flex items-center justify-center">
            <span className="font-medium text-[#02A95C]">
              {user.name?.charAt(0).toUpperCase() || user.email.charAt(0).toUpperCase()}
            </span>
          </div>
          <div className="ml-4">
            <div className="font-medium text-gray-900">{user.name || 'N/A'}</div>
            <div className="text-gray-500">{user.email}</div>
          </div>
        </div>
      ),
    },
    {
      header: 'Role',
      accessor: (user: User) => (
        user.role === 'ADMIN' ? (
          <span className="px-3 py-1.5 text-sm font-semibold text-purple-600 bg-purple-50 rounded-lg">Admin</span>
        ) : (
          <select
            className="block w-full rounded-lg border-0 py-1.5 pl-3 pr-10 text-gray-900 ring-1 ring-inset ring-gray-300 focus:ring-2 focus:ring-[#02A95C] sm:text-sm sm:leading-6 bg-white"
            value={user.role}
            onChange={(e) => handleRoleChange(user.id, e.target.value as Role)}
          >
            <option value="ANALYST">Analyst</option>
            <option value="VIEWER">Viewer</option>
          </select>
        )
      ),
    },
    {
      header: 'Status',
      accessor: (user: User) => (
        <button
          onClick={() => handleStatusToggle(user)}
          className={`relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-[#02A95C] focus:ring-offset-2 ${
            user.status === 'ACTIVE' ? 'bg-[#02A95C]' : 'bg-gray-200'
          }`}
          role="switch"
          aria-checked={user.status === 'ACTIVE'}
        >
          <span
            aria-hidden="true"
            className={`pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out ${
              user.status === 'ACTIVE' ? 'translate-x-5' : 'translate-x-0'
            }`}
          />
        </button>
      ),
    },
    {
      header: 'Type',
      accessor: (user: User) => (
        <div className="flex items-center space-x-2">
          {user.role === 'ADMIN' ? (
            <ShieldAlert className="h-5 w-5 text-purple-500" title="Admin" />
          ) : user.role === 'ANALYST' ? (
            <Shield className="h-5 w-5 text-blue-500" title="Analyst" />
          ) : (
            <UserCheck className="h-5 w-5 text-gray-400" title="Viewer" />
          )}
        </div>
      ),
    },
    {
      header: 'Actions',
      accessor: (user: User) => (
        <div className="flex items-center gap-3">
          {user.role !== 'ADMIN' && (
            <>
              <button
                onClick={() => openEditModal(user)}
                className="text-gray-400 hover:text-blue-600 transition-colors font-medium text-sm"
              >
                Edit
              </button>
              <button
                onClick={() => handleDelete(user.id, user.role)}
                className="text-gray-400 hover:text-red-600 transition-colors font-medium text-sm"
              >
                Delete
              </button>
            </>
          )}
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">User Management</h1>
        <Button onClick={() => setIsModalOpen(true)} className="flex items-center gap-2">
          <Plus className="h-4 w-4" />
          Add User
        </Button>
      </div>

      <Card className="p-0 sm:p-0 overflow-hidden border border-gray-100">
        {isLoading ? (
          <div className="flex justify-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-[#02A95C]"></div>
          </div>
        ) : (
          <Table
            className="border-0 shadow-none rounded-none"
            data={users || []}
            columns={columns}
            keyExtractor={(user) => user.id}
          />
        )}
      </Card>

      <Modal
        isOpen={isModalOpen}
        onClose={closeModal}
        title={editingId ? "Edit User" : "Add New User"}
      >
        <form onSubmit={handleAddSubmit} className="space-y-4">
          <Input
            label="Name"
            required
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            placeholder="John Doe"
          />

          <Input
            label="Email"
            type="email"
            required={!editingId}
            disabled={!!editingId}
            value={formData.email}
            onChange={(e) => setFormData({ ...formData, email: e.target.value })}
            placeholder="john@example.com"
            className={editingId ? "bg-gray-100 cursor-not-allowed text-gray-500" : ""}
          />

          <Input
            label={editingId ? "New Password (leave blank to keep current)" : "Password"}
            type="password"
            required={!editingId}
            value={formData.password}
            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
            placeholder="••••••••"
          />
          
          <div className="flex flex-col space-y-1">
            <label className="text-sm font-medium text-gray-700">Role</label>
            <select
              className="px-4 py-2 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-[#02A95C] bg-white shadow-sm"
              value={formData.role}
              onChange={(e) => setFormData({ ...formData, role: e.target.value as Role })}
            >
              <option value="ANALYST">Analyst</option>
              <option value="VIEWER">Viewer</option>
            </select>
          </div>

          <div className="pt-4 flex justify-end gap-3">
            <Button type="button" variant="ghost" onClick={closeModal}>
              Cancel
            </Button>
            <Button type="submit" isLoading={addUserMutation.isPending || editUserMutation.isPending}>
              {editingId ? 'Update User' : 'Create User'}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
};
