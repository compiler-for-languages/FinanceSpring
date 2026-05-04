import { useState } from 'react';
import { Outlet, useNavigate, Link, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/useAuthStore';
import { LogOut, Menu, X, LayoutDashboard, FileText, Users } from 'lucide-react';
import { cn } from '../utils/classNames';

export const DashboardLayout = () => {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const handleLogout = async () => {
    try {
      await fetch('http://localhost:8080/auth/logout', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
      });
    } catch (e) {
      console.error('Logout API failed', e);
    }
    logout();
    navigate('/login');
  };

  const getNavigation = () => {
    const role = user?.role;
    const base = `/${role?.toLowerCase()}`;
    const nav = [
      { name: 'Dashboard', href: `${base}/dashboard`, icon: LayoutDashboard },
      { name: 'Records', href: `${base}/records`, icon: FileText },
    ];

    if (role === 'ADMIN') {
      nav.push({ name: 'Users', href: `${base}/users`, icon: Users });
    }

    return nav;
  };

  const navigation = getNavigation();

  return (
    <div className="min-h-screen bg-[#F9FAFB]">
      {/* Mobile sidebar */}
      <div className={cn("relative z-50 lg:hidden", sidebarOpen ? "block" : "hidden")}>
        <div className="fixed inset-0 bg-gray-900/80" onClick={() => setSidebarOpen(false)} />
        <div className="fixed inset-0 flex">
          <div className="relative mr-16 flex w-full max-w-xs flex-1 flex-col bg-white pt-5 pb-4">
            <div className="absolute top-0 right-0 -mr-12 pt-2">
              <button
                type="button"
                className="ml-1 flex h-10 w-10 items-center justify-center rounded-full focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white"
                onClick={() => setSidebarOpen(false)}
              >
                <X className="h-6 w-6 text-white" />
              </button>
            </div>
            <div className="flex shrink-0 items-center px-4">
              <span className="text-xl font-bold text-[#02A95C]">FinanceFlow</span>
            </div>
            <div className="mt-5 h-0 flex-1 overflow-y-auto">
              <nav className="space-y-1 px-2">
                {navigation.map((item) => {
                  const isActive = location.pathname.startsWith(item.href);
                  return (
                    <Link
                      key={item.name}
                      to={item.href}
                      className={cn(
                        isActive
                          ? 'bg-[#E6F6EE] text-[#02A95C]'
                          : 'text-[#6B7280] hover:bg-gray-50 hover:text-gray-900',
                        'group flex items-center rounded-xl px-2 py-2 text-base font-medium transition-colors'
                      )}
                    >
                      <item.icon
                        className={cn(
                          isActive ? 'text-[#02A95C]' : 'text-gray-400 group-hover:text-gray-500',
                          'mr-4 h-6 w-6 shrink-0'
                        )}
                      />
                      {item.name}
                    </Link>
                  );
                })}
              </nav>
            </div>
            <div className="border-t border-gray-200 p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-base font-medium text-gray-700">{user?.name || user?.email}</p>
                  <p className="text-sm font-medium text-gray-500">{user?.role}</p>
                </div>
                <button
                  type="button"
                  onClick={handleLogout}
                  className="rounded-full bg-white p-2 text-gray-400 hover:text-gray-500"
                >
                  <LogOut className="h-6 w-6" />
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Desktop sidebar */}
      <div className="hidden lg:fixed lg:inset-y-0 lg:flex lg:w-64 lg:flex-col">
        <div className="flex flex-grow flex-col overflow-y-auto border-r border-gray-200 bg-white pt-5 pb-4">
          <div className="flex shrink-0 items-center px-6">
            <span className="text-2xl font-bold text-[#02A95C]">FinanceFlow</span>
          </div>
          <div className="mt-8 flex flex-1 flex-col">
            <nav className="flex-1 space-y-2 px-4">
              {navigation.map((item) => {
                const isActive = location.pathname.startsWith(item.href);
                return (
                  <Link
                    key={item.name}
                    to={item.href}
                    className={cn(
                      isActive
                        ? 'bg-[#E6F6EE] text-[#02A95C]'
                        : 'text-[#6B7280] hover:bg-gray-50 hover:text-gray-900',
                      'group flex items-center rounded-xl px-3 py-2 text-sm font-medium transition-colors'
                    )}
                  >
                    <item.icon
                      className={cn(
                        isActive ? 'text-[#02A95C]' : 'text-gray-400 group-hover:text-gray-500',
                        'mr-3 h-5 w-5 shrink-0'
                      )}
                    />
                    {item.name}
                  </Link>
                );
              })}
            </nav>
          </div>
          <div className="border-t border-gray-200 p-4">
            <div className="flex items-center px-2">
              <div className="ml-3">
                <p className="text-sm font-medium text-gray-700">{user?.name || user?.email}</p>
                <p className="text-xs font-medium text-gray-500">{user?.role}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Main content */}
      <div className="flex flex-1 flex-col lg:pl-64">
        <div className="sticky top-0 z-10 flex h-16 flex-shrink-0 bg-white shadow-sm border-b border-gray-200 lg:border-none lg:shadow-none lg:hidden">
          <button
            type="button"
            className="border-r border-gray-200 px-4 text-gray-500 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-[#02A95C] lg:hidden"
            onClick={() => setSidebarOpen(true)}
          >
            <span className="sr-only">Open sidebar</span>
            <Menu className="h-6 w-6" />
          </button>
          <div className="flex flex-1 justify-between px-4 sm:px-6 lg:px-8">
            <div className="flex flex-1 items-center">
              <span className="text-xl font-bold text-[#02A95C] lg:hidden">FinanceFlow</span>
            </div>
          </div>
        </div>

        <div className="hidden lg:flex sticky top-0 z-10 h-16 flex-shrink-0 bg-white shadow-sm border-b border-gray-200">
          <div className="flex flex-1 justify-end px-4 sm:px-6 lg:px-8">
            <div className="ml-4 flex items-center md:ml-6">
              <button
                type="button"
                onClick={handleLogout}
                className="flex items-center gap-2 rounded-xl bg-white p-2 text-gray-400 hover:text-gray-500 hover:bg-gray-50 transition-colors"
              >
                <LogOut className="h-5 w-5" />
                <span className="text-sm font-medium">Logout</span>
              </button>
            </div>
          </div>
        </div>

        <main className="flex-1">
          <div className="py-6 sm:py-8 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
};
