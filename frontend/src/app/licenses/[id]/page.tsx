'use client';

import { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import DashboardLayout from '@/components/DashboardLayout';
import { api, License, ServerInstance } from '@/lib/api';
import { 
  ArrowLeftIcon, 
  PencilIcon, 
  TrashIcon,
  ClipboardIcon,
  CheckIcon,
  ServerIcon
} from '@heroicons/react/24/outline';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';

export default function LicenseDetailPage() {
  const router = useRouter();
  const params = useParams();
  const [license, setLicense] = useState<License | null>(null);
  const [servers, setServers] = useState<ServerInstance[]>([]);
  const [loading, setLoading] = useState(true);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    if (params.id) {
      fetchLicense();
      fetchServers();
    }
  }, [params.id]);

  const fetchLicense = async () => {
    try {
      const data = await api.getLicense(Number(params.id));
      setLicense(data);
    } catch (error) {
      toast.error('Failed to load license');
      router.push('/licenses');
    } finally {
      setLoading(false);
    }
  };

  const fetchServers = async () => {
    try {
      const data = await api.getServers();
      setServers(data.filter(s => s.licenseId === license?.id));
    } catch (error) {
      // Silently fail, servers are optional
    }
  };

  const handleDelete = async () => {
    if (!license || !confirm('Are you sure you want to delete this license?')) return;

    try {
      await api.deleteLicense(license.id);
      toast.success('License deleted successfully');
      router.push('/licenses');
    } catch (error) {
      toast.error('Failed to delete license');
    }
  };

  const copyLicenseKey = async () => {
    if (!license) return;

    try {
      await navigator.clipboard.writeText(license.licenseKey);
      setCopied(true);
      toast.success('License key copied to clipboard');
      setTimeout(() => setCopied(false), 2000);
    } catch (error) {
      toast.error('Failed to copy license key');
    }
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

  if (!license) {
    return (
      <DashboardLayout>
        <div className="text-center py-12">
          <h2 className="text-xl font-semibold text-gray-900">License not found</h2>
          <p className="text-gray-600 mt-2">The license you're looking for doesn't exist.</p>
          <button
            onClick={() => router.push('/licenses')}
            className="mt-4 bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 transition-colors"
          >
            Back to Licenses
          </button>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="max-w-4xl mx-auto">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="space-y-6"
        >
          {/* Header */}
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <button
                onClick={() => router.back()}
                className="p-2 text-gray-400 hover:text-gray-600 transition-colors"
              >
                <ArrowLeftIcon className="h-6 w-6" />
              </button>
              <div>
                <h1 className="text-2xl font-bold text-gray-900">License Details</h1>
                <p className="text-gray-600">View and manage license information</p>
              </div>
            </div>
            <div className="flex items-center space-x-3">
              <button
                onClick={() => router.push(`/licenses/${license.id}/edit`)}
                className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 transition-colors flex items-center space-x-2"
              >
                <PencilIcon className="h-4 w-4" />
                <span>Edit</span>
              </button>
              <button
                onClick={handleDelete}
                className="bg-red-600 text-white px-4 py-2 rounded-md hover:bg-red-700 transition-colors flex items-center space-x-2"
              >
                <TrashIcon className="h-4 w-4" />
                <span>Delete</span>
              </button>
            </div>
          </div>

          {/* License Information */}
          <div className="bg-white shadow rounded-lg p-6">
            <h3 className="text-lg font-medium text-gray-900 mb-4">License Information</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-gray-500">License Key</label>
                <div className="mt-1 flex items-center space-x-2">
                  <code className="bg-gray-100 px-3 py-2 rounded-md font-mono text-sm flex-1">
                    {license.licenseKey}
                  </code>
                  <button
                    onClick={copyLicenseKey}
                    className="p-2 text-gray-400 hover:text-gray-600 transition-colors"
                    title="Copy license key"
                  >
                    {copied ? (
                      <CheckIcon className="h-4 w-4 text-green-600" />
                    ) : (
                      <ClipboardIcon className="h-4 w-4" />
                    )}
                  </button>
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-500">Status</label>
                <div className="mt-1">
                  <span className={`inline-flex px-3 py-1 text-sm font-semibold rounded-full ${
                    license.active 
                      ? 'bg-green-100 text-green-800' 
                      : 'bg-red-100 text-red-800'
                  }`}>
                    {license.active ? 'Active' : 'Inactive'}
                  </span>
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-500">Maximum Players</label>
                <div className="mt-1 text-sm text-gray-900">{license.maxPlayers}</div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-500">Expiration Date</label>
                <div className="mt-1 text-sm text-gray-900">
                  {new Date(license.expirationDate).toLocaleDateString()}
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-500">Created</label>
                <div className="mt-1 text-sm text-gray-900">
                  {new Date(license.createdAt).toLocaleDateString()}
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-500">Last Validation</label>
                <div className="mt-1 text-sm text-gray-900">
                  {license.lastValidation 
                    ? new Date(license.lastValidation).toLocaleDateString()
                    : 'Never'
                  }
                </div>
              </div>
            </div>
          </div>

          {/* Associated Servers */}
          <div className="bg-white shadow rounded-lg p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-medium text-gray-900">Associated Servers</h3>
              <div className="flex items-center space-x-2 text-sm text-gray-500">
                <ServerIcon className="h-4 w-4" />
                <span>{servers.length} server{servers.length !== 1 ? 's' : ''}</span>
              </div>
            </div>
            
            {servers.length > 0 ? (
              <div className="space-y-3">
                {servers.map((server) => (
                  <div key={server.id} className="border rounded-lg p-4">
                    <div className="flex items-center justify-between">
                      <div>
                        <h4 className="font-medium text-gray-900">
                          Server #{server.id}
                        </h4>
                        <p className="text-sm text-gray-500">
                          {server.serverIp}
                        </p>
                      </div>
                      <div className="text-right">
                        <div className="text-sm text-gray-500">Players</div>
                        <div className="font-medium">
                          {server.currentPlayers}
                        </div>
                      </div>
                    </div>
                    <div className="mt-2 text-xs text-gray-400">
                      Last seen: {new Date(server.lastSeen).toLocaleString()}
                    </div>
                    <div className="text-xs text-gray-400">
                      Validations: {server.validationCount}
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center py-8 text-gray-500">
                <ServerIcon className="h-12 w-12 mx-auto mb-4 text-gray-300" />
                <p>No servers are currently using this license</p>
                <p className="text-sm mt-1">
                  Servers will appear here when they validate with this license key
                </p>
              </div>
            )}
          </div>
        </motion.div>
      </div>
    </DashboardLayout>
  );
}
