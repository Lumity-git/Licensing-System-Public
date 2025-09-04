'use client';

import { useState, useEffect } from 'react';
import DashboardLayout from '@/components/DashboardLayout';
import { api, License, ServerInstance } from '@/lib/api';
import { 
  ChartBarIcon, 
  ArrowTrendingUpIcon,
  CalendarIcon,
  UsersIcon
} from '@heroicons/react/24/outline';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';

interface AnalyticsData {
  totalLicenses: number;
  activeLicenses: number;
  totalServers: number;
  totalValidations: number;
  todayValidations: number;
  weekValidations: number;
  monthValidations: number;
  averagePlayersPerServer: number;
  licenseUsageRate: number;
  recentActivity: Array<{
    date: string;
    validations: number;
    servers: number;
  }>;
}

export default function AnalyticsPage() {
  const [analytics, setAnalytics] = useState<AnalyticsData | null>(null);
  const [licenses, setLicenses] = useState<License[]>([]);
  const [servers, setServers] = useState<ServerInstance[]>([]);
  const [loading, setLoading] = useState(true);
  const [timeRange, setTimeRange] = useState<'7d' | '30d' | '90d'>('30d');

  useEffect(() => {
    fetchAnalytics();
  }, []);

  const fetchAnalytics = async () => {
    try {
      const [licensesData, serversData] = await Promise.all([
        api.getLicenses(),
        api.getServers(),
      ]);

      setLicenses(licensesData);
      setServers(serversData);

      // Calculate analytics
      const totalLicenses = licensesData.length;
      const activeLicenses = licensesData.filter(l => l.active).length;
      const totalServers = serversData.length;
      const totalValidations = serversData.reduce((sum, s) => sum + s.validationCount, 0);
      
      const now = new Date();
      const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
      const weekAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000);
      const monthAgo = new Date(today.getTime() - 30 * 24 * 60 * 60 * 1000);

      // Calculate real validation data from server instances
      const todayValidations = serversData.reduce((sum, s) => {
        // Assuming lastSeen represents recent activity
        const lastSeen = new Date(s.lastSeen);
        return lastSeen >= today ? sum + 1 : sum;
      }, 0);
      
      const weekValidations = serversData.reduce((sum, s) => {
        const lastSeen = new Date(s.lastSeen);
        return lastSeen >= weekAgo ? sum + s.validationCount : sum;
      }, 0);
      
      const monthValidations = totalValidations;

      const averagePlayersPerServer = totalServers > 0 
        ? Math.round(serversData.reduce((sum, s) => sum + s.currentPlayers, 0) / totalServers * 10) / 10
        : 0;

      const licenseUsageRate = totalLicenses > 0 
        ? Math.round((activeLicenses / totalLicenses) * 100 * 10) / 10
        : 0;

      // Generate real activity data based on server data
      const recentActivity = [];
      for (let i = 6; i >= 0; i--) {
        const date = new Date(today.getTime() - i * 24 * 60 * 60 * 1000);
        const dayStart = new Date(date);
        const dayEnd = new Date(date.getTime() + 24 * 60 * 60 * 1000);
        
        // Count servers that were active on this day
        const serversActiveToday = serversData.filter(s => {
          const lastSeen = new Date(s.lastSeen);
          return lastSeen >= dayStart && lastSeen < dayEnd;
        }).length;
        
        recentActivity.push({
          date: date.toISOString().split('T')[0],
          validations: serversActiveToday,
          servers: serversActiveToday,
        });
      }

      setAnalytics({
        totalLicenses,
        activeLicenses,
        totalServers,
        totalValidations,
        todayValidations,
        weekValidations,
        monthValidations,
        averagePlayersPerServer,
        licenseUsageRate,
        recentActivity,
      });
    } catch (error) {
      toast.error('Failed to load analytics data');
    } finally {
      setLoading(false);
    }
  };

  const getGrowthPercentage = (current: number, previous: number) => {
    if (previous === 0) return current > 0 ? 100 : 0;
    return Math.round(((current - previous) / previous) * 100);
  };

  if (loading) {
    return (
      <DashboardLayout>
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
        </div>
      </DashboardLayout>
    );
  }

  if (!analytics) {
    return (
      <DashboardLayout>
        <div className="text-center py-12">
          <ChartBarIcon className="h-12 w-12 mx-auto mb-4 text-gray-300" />
          <h3 className="text-lg font-medium text-gray-900">No Analytics Data</h3>
          <p className="text-gray-500">Unable to load analytics data</p>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Analytics</h1>
            <p className="text-gray-600">Performance insights and usage statistics</p>
          </div>
          <div>
            <select
              value={timeRange}
              onChange={(e) => setTimeRange(e.target.value as '7d' | '30d' | '90d')}
              className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="7d">Last 7 days</option>
              <option value="30d">Last 30 days</option>
              <option value="90d">Last 90 days</option>
            </select>
          </div>
        </div>

        {/* Key Metrics */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="bg-white rounded-lg shadow p-6"
          >
            <div className="flex items-center">
              <div className="p-3 rounded-md bg-blue-500">
                <ChartBarIcon className="h-6 w-6 text-white" />
              </div>
              <div className="ml-4 flex-1">
                <p className="text-sm font-medium text-gray-600">Total Validations</p>
                <p className="text-2xl font-semibold text-gray-900">{analytics.totalValidations}</p>
                <div className="flex items-center mt-1">
                  <ArrowTrendingUpIcon className="h-3 w-3 text-green-500" />
                  <span className="text-xs text-green-600 ml-1">
                    +{analytics.weekValidations > 0 ? getGrowthPercentage(analytics.monthValidations, analytics.weekValidations * 4) : 0}%
                  </span>
                </div>
              </div>
            </div>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
            className="bg-white rounded-lg shadow p-6"
          >
            <div className="flex items-center">
              <div className="p-3 rounded-md bg-green-500">
                <CalendarIcon className="h-6 w-6 text-white" />
              </div>
              <div className="ml-4 flex-1">
                <p className="text-sm font-medium text-gray-600">Today's Validations</p>
                <p className="text-2xl font-semibold text-gray-900">{analytics.todayValidations}</p>
                <div className="flex items-center mt-1">
                  <span className="text-xs text-gray-500">
                    vs. {Math.round(analytics.weekValidations / 7)} avg/day
                  </span>
                </div>
              </div>
            </div>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
            className="bg-white rounded-lg shadow p-6"
          >
            <div className="flex items-center">
              <div className="p-3 rounded-md bg-purple-500">
                <UsersIcon className="h-6 w-6 text-white" />
              </div>
              <div className="ml-4 flex-1">
                <p className="text-sm font-medium text-gray-600">Avg Players/Server</p>
                <p className="text-2xl font-semibold text-gray-900">{analytics.averagePlayersPerServer}</p>
                <div className="flex items-center mt-1">
                  <span className="text-xs text-gray-500">
                    across {analytics.totalServers} servers
                  </span>
                </div>
              </div>
            </div>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3 }}
            className="bg-white rounded-lg shadow p-6"
          >
            <div className="flex items-center">
              <div className="p-3 rounded-md bg-orange-500">
                <ChartBarIcon className="h-6 w-6 text-white" />
              </div>
              <div className="ml-4 flex-1">
                <p className="text-sm font-medium text-gray-600">License Usage Rate</p>
                <p className="text-2xl font-semibold text-gray-900">{analytics.licenseUsageRate}%</p>
                <div className="flex items-center mt-1">
                  <span className="text-xs text-gray-500">
                    {analytics.totalServers}/{analytics.totalLicenses} licenses used
                  </span>
                </div>
              </div>
            </div>
          </motion.div>
        </div>

        {/* Recent Activity Chart */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4 }}
          className="bg-white shadow rounded-lg p-6"
        >
          <h3 className="text-lg font-medium text-gray-900 mb-4">Recent Activity (Last 7 Days)</h3>
          <div className="space-y-4">
            {analytics.recentActivity.map((day, index) => (
              <div key={day.date} className="flex items-center justify-between">
                <div className="flex items-center space-x-4">
                  <div className="text-sm text-gray-500 w-24">
                    {new Date(day.date).toLocaleDateString('en-US', { 
                      month: 'short', 
                      day: 'numeric' 
                    })}
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center space-x-2">
                      <div className="text-sm font-medium text-gray-900">
                        {day.validations} validations
                      </div>
                      <div className="text-sm text-gray-500">
                        from {day.servers} servers
                      </div>
                    </div>
                  </div>
                </div>
                <div className="w-32">
                  <div className="bg-gray-200 rounded-full h-2">
                    <div 
                      className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                      style={{ 
                        width: `${Math.min((day.validations / Math.max(...analytics.recentActivity.map(d => d.validations))) * 100, 100)}%` 
                      }}
                    ></div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </motion.div>

        {/* License Status Overview */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.5 }}
          className="bg-white shadow rounded-lg p-6"
        >
          <h3 className="text-lg font-medium text-gray-900 mb-4">License Overview</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="text-center">
              <div className="text-3xl font-bold text-blue-600">{analytics.totalLicenses}</div>
              <div className="text-sm text-gray-500">Total Licenses</div>
            </div>
            <div className="text-center">
              <div className="text-3xl font-bold text-green-600">{analytics.activeLicenses}</div>
              <div className="text-sm text-gray-500">Active Licenses</div>
            </div>
            <div className="text-center">
              <div className="text-3xl font-bold text-orange-600">
                {analytics.totalLicenses - analytics.activeLicenses}
              </div>
              <div className="text-sm text-gray-500">Inactive Licenses</div>
            </div>
          </div>
          <div className="mt-4 bg-gray-200 rounded-full h-3">
            <div 
              className="bg-green-500 h-3 rounded-full transition-all duration-300"
              style={{ 
                width: `${analytics.totalLicenses > 0 ? (analytics.activeLicenses / analytics.totalLicenses) * 100 : 0}%` 
              }}
            ></div>
          </div>
          <div className="mt-2 text-center text-sm text-gray-600">
            {analytics.totalLicenses > 0 
              ? Math.round((analytics.activeLicenses / analytics.totalLicenses) * 100)
              : 0}% of licenses are currently active
          </div>
        </motion.div>
      </div>
    </DashboardLayout>
  );
}
