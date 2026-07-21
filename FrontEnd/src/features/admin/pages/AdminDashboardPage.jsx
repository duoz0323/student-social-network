import { Users, FileText, AlertCircle, AlertTriangle, Ban, TrendingUp, Sparkles } from 'lucide-react';
import { useApp } from '../../../contexts/AppContext.jsx';

export default function AdminDashboardPage() {
  const { data } = useApp();
  
  const stats = [
    { label: 'Người dùng', value: data.users.length, icon: Users, color: 'text-blue-600', bg: 'bg-blue-50', trend: '+12%' },
    { label: 'Bài viết', value: data.posts.length, icon: FileText, color: 'text-green-600', bg: 'bg-green-50', trend: '+28%' },
    { label: 'Báo cáo', value: data.reports.length, icon: AlertTriangle, color: 'text-orange-600', bg: 'bg-orange-50', trend: 'Cần chú ý' },
    { label: 'Tài khoản khóa', value: data.users.filter((user) => user.status === 'BLOCKED').length, icon: Ban, color: 'text-purple-600', bg: 'bg-purple-50', trend: '-2%' }
  ];

  return (
    <section>
      {/* Header */}
      <div className="mb-8 animate-slide-up">
        <h1 className="text-2xl font-bold tracking-tight text-gray-900">Tổng quan hệ thống</h1>
        <p className="mt-1.5 text-sm text-gray-500">Xin chào, Admin! Dưới đây là tình hình hoạt động hôm nay.</p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4 mb-8 animate-slide-up-delayed-1">
        {stats.map((stat, idx) => {
          const Icon = stat.icon;
          return (
            <div key={idx} className="rounded-xl border border-gray-100 bg-white p-6 shadow-sm hover:-translate-y-1 hover:shadow-md transition-all duration-300 cursor-default group">
              <div className="flex items-center justify-between mb-4">
                <div className={`${stat.bg} ${stat.color} p-2 rounded-xl`}>
                  <Icon size={20} strokeWidth={2} />
                </div>
                <div className="flex items-center gap-1 text-[11px] font-medium text-gray-600 bg-gray-50 px-2.5 py-1 rounded-md">
                  {stat.trend.includes('+') && <TrendingUp size={12} />}
                  {stat.trend}
                </div>
              </div>
              <div>
                <p className="text-sm font-medium text-gray-500 mb-1">{stat.label}</p>
                <p className="text-3xl font-bold text-gray-900">{stat.value}</p>
              </div>
            </div>
          );
        })}
      </div>

      {/* System Status section */}
      <div className="mt-8 rounded-2xl border border-gray-100 bg-gray-50/50 p-6 animate-slide-up-delayed-2">
        <h2 className="text-sm font-semibold text-gray-900 mb-2">Trạng thái hệ thống</h2>
        <p className="text-sm text-gray-500 mb-4">Các cờ cấu hình hiện tại đang được áp dụng cho môi trường này.</p>
        <div className="flex flex-wrap gap-2">
          <span className="inline-flex items-center gap-1.5 rounded-lg bg-white px-3 py-1.5 text-xs font-medium text-gray-700 border border-gray-200 shadow-sm">
            <Sparkles size={14} className="text-gray-400" /> MVP_CURRENT
          </span>
          <span className="inline-flex items-center gap-1.5 rounded-lg bg-white px-3 py-1.5 text-xs font-medium text-gray-700 border border-gray-200 shadow-sm">
            <AlertCircle size={14} className="text-gray-400" /> Mock Data
          </span>
          <span className="inline-flex items-center gap-1.5 rounded-lg bg-white px-3 py-1.5 text-xs font-medium text-gray-700 border border-gray-200 shadow-sm">
            Environment: Dev
          </span>
        </div>
      </div>
    </section>
  );
}
