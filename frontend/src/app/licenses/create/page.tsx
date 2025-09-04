'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import DashboardLayout from '@/components/DashboardLayout';
import { api } from '@/lib/api';
import { ArrowLeftIcon } from '@heroicons/react/24/outline';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';

export default function CreateLicensePage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    licenseKey: '',
    maxPlayers: 100,
    expirationDate: '',
    active: true,
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      await api.createLicense({
        licenseKey: formData.licenseKey,
        maxPlayers: formData.maxPlayers,
        expirationDate: formData.expirationDate,
        active: formData.active,
      });

      toast.success('License created successfully');
      router.push('/licenses');
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to create license');
    } finally {
      setLoading(false);
    }
  };

  const generateLicenseKey = () => {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
    let result = '';
    for (let i = 0; i < 16; i++) {
      if (i > 0 && i % 4 === 0) result += '-';
      result += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    setFormData(prev => ({ ...prev, licenseKey: result }));
  };

  const setDefaultExpiration = () => {
    const date = new Date();
    date.setFullYear(date.getFullYear() + 1);
    setFormData(prev => ({ 
      ...prev, 
      expirationDate: date.toISOString().split('T')[0] 
    }));
  };

  return (
    <DashboardLayout>
      <div className="max-w-2xl mx-auto">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="space-y-6"
        >
          {/* Header */}
          <div className="flex items-center space-x-4">
            <button
              onClick={() => router.back()}
              className="p-2 text-gray-400 hover:text-gray-600 transition-colors"
            >
              <ArrowLeftIcon className="h-6 w-6" />
            </button>
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Create License</h1>
              <p className="text-gray-600">Generate a new license for the DeadZone plugin</p>
            </div>
          </div>

          {/* Form */}
          <div className="bg-white shadow rounded-lg p-6">
            <form onSubmit={handleSubmit} className="space-y-6">
              {/* License Key */}
              <div>
                <label htmlFor="licenseKey" className="block text-sm font-medium text-gray-700 mb-2">
                  License Key
                </label>
                <div className="flex space-x-2">
                  <input
                    type="text"
                    id="licenseKey"
                    value={formData.licenseKey}
                    onChange={(e) => setFormData(prev => ({ ...prev, licenseKey: e.target.value }))}
                    className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="Enter license key or generate one"
                    required
                  />
                  <button
                    type="button"
                    onClick={generateLicenseKey}
                    className="bg-gray-600 text-white px-4 py-2 rounded-md hover:bg-gray-700 transition-colors"
                  >
                    Generate
                  </button>
                </div>
                <p className="text-sm text-gray-500 mt-1">
                  A unique identifier for this license (e.g., XXXX-XXXX-XXXX-XXXX)
                </p>
              </div>

              {/* Max Players */}
              <div>
                <label htmlFor="maxPlayers" className="block text-sm font-medium text-gray-700 mb-2">
                  Maximum Players
                </label>
                <input
                  type="number"
                  id="maxPlayers"
                  value={formData.maxPlayers}
                  onChange={(e) => setFormData(prev => ({ ...prev, maxPlayers: parseInt(e.target.value) }))}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  min="1"
                  max="1000"
                  required
                />
                <p className="text-sm text-gray-500 mt-1">
                  Maximum number of players allowed on the server
                </p>
              </div>

              {/* Expiration Date */}
              <div>
                <label htmlFor="expirationDate" className="block text-sm font-medium text-gray-700 mb-2">
                  Expiration Date
                </label>
                <div className="flex space-x-2">
                  <input
                    type="date"
                    id="expirationDate"
                    value={formData.expirationDate}
                    onChange={(e) => setFormData(prev => ({ ...prev, expirationDate: e.target.value }))}
                    className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    required
                  />
                  <button
                    type="button"
                    onClick={setDefaultExpiration}
                    className="bg-gray-600 text-white px-4 py-2 rounded-md hover:bg-gray-700 transition-colors"
                  >
                    +1 Year
                  </button>
                </div>
                <p className="text-sm text-gray-500 mt-1">
                  When this license will expire
                </p>
              </div>

              {/* Active Status */}
              <div className="flex items-center">
                <input
                  type="checkbox"
                  id="active"
                  checked={formData.active}
                  onChange={(e) => setFormData(prev => ({ ...prev, active: e.target.checked }))}
                  className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label htmlFor="active" className="ml-2 block text-sm text-gray-900">
                  License is active
                </label>
              </div>

              {/* Submit Buttons */}
              <div className="flex items-center justify-end space-x-4 pt-6 border-t border-gray-200">
                <button
                  type="button"
                  onClick={() => router.back()}
                  className="px-4 py-2 text-gray-700 bg-gray-100 rounded-md hover:bg-gray-200 transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {loading ? 'Creating...' : 'Create License'}
                </button>
              </div>
            </form>
          </div>
        </motion.div>
      </div>
    </DashboardLayout>
  );
}
