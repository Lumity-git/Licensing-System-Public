const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

export interface ApiResponse<T = any> {
  success: boolean;
  message: string;
  data?: T;
}

export interface LoginResponse {
  success: boolean;
  message: string;
  token: string;
}

export interface License {
  id: number;
  licenseKey: string;
  serverIp: string | null;
  maxPlayers: number;
  active: boolean;
  createdAt: string;
  expirationDate: string;
  lastValidation: string | null;
}

export interface ServerInstance {
  id: number;
  licenseId: number | null;
  serverIp: string;
  currentPlayers: number;
  maxPlayers?: number;
  serverName?: string;
  serverVersion?: string;
  pluginVersion?: string;
  cpuInfo?: string;
  memoryTotal?: number;
  memoryUsed?: number;
  diskTotal?: number;
  diskUsed?: number;
  osName?: string;
  osVersion?: string;
  javaVersion?: string;
  licenseStatus: 'VALID' | 'INVALID' | 'EXPIRED' | 'UNLICENSED' | 'UNKNOWN';
  pluginStatus: 'ACTIVE' | 'INACTIVE' | 'ERROR' | 'STARTING' | 'STOPPING';
  lastSeen: string;
  firstSeen: string;
  validationCount: number;
  licenseKeyAttempted?: string;
}

export interface CreateLicenseRequest {
  licenseKey: string;
  maxPlayers: number;
  active: boolean;
  expirationDate: string;
}

class ApiClient {
  private getAuthHeaders(): HeadersInit {
    const token = localStorage.getItem('authToken');
    return {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
    };
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${API_BASE_URL}${endpoint}`;
    const config: RequestInit = {
      headers: this.getAuthHeaders(),
      ...options,
    };

    try {
      const response = await fetch(url, config);
      
      if (!response.ok) {
        if (response.status === 401) {
          localStorage.removeItem('authToken');
          window.location.href = '/login';
          throw new Error('Authentication failed');
        }
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      return await response.json();
    } catch (error) {
      console.error('API request failed:', error);
      throw error;
    }
  }

  // Authentication
  async login(username: string, password: string): Promise<LoginResponse> {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    });

    if (!response.ok) {
      throw new Error('Login failed');
    }

    const data = await response.json();
    if (data.success && data.token) {
      localStorage.setItem('authToken', data.token);
    }
    return data;
  }

  logout(): void {
    localStorage.removeItem('authToken');
    window.location.href = '/login';
  }

  isAuthenticated(): boolean {
    return !!localStorage.getItem('authToken');
  }

  // License endpoints
  async getLicenses(): Promise<License[]> {
    return this.request<License[]>('/licenses');
  }

  async createLicense(license: CreateLicenseRequest): Promise<ApiResponse> {
    return this.request<ApiResponse>('/licenses', {
      method: 'POST',
      body: JSON.stringify(license),
    });
  }

  async getLicense(id: number): Promise<License> {
    return this.request<License>(`/licenses/${id}`);
  }

  async updateLicense(id: number, license: CreateLicenseRequest): Promise<ApiResponse> {
    return this.request<ApiResponse>(`/licenses/${id}`, {
      method: 'PUT',
      body: JSON.stringify(license),
    });
  }

  async deleteLicense(id: number): Promise<ApiResponse> {
    return this.request<ApiResponse>(`/licenses/${id}`, {
      method: 'DELETE',
    });
  }

  // Server endpoints
  async getServers(): Promise<ServerInstance[]> {
    return this.request<ServerInstance[]>('/servers');
  }

  async getLicensedServers(): Promise<ServerInstance[]> {
    return this.request<ServerInstance[]>('/servers/licensed');
  }

  async getUnlicensedServers(): Promise<ServerInstance[]> {
    return this.request<ServerInstance[]>('/servers/unlicensed');
  }

  async getInvalidLicenseServers(): Promise<ServerInstance[]> {
    return this.request<ServerInstance[]>('/servers/invalid');
  }

  async getExpiredLicenseServers(): Promise<ServerInstance[]> {
    return this.request<ServerInstance[]>('/servers/expired');
  }

  async getServersByStatus(status: string): Promise<ServerInstance[]> {
    return this.request<ServerInstance[]>(`/servers/status/${status}`);
  }

  async getActiveServers(): Promise<ServerInstance[]> {
    return this.request<ServerInstance[]>('/servers/active');
  }

  async getServerStats(): Promise<any> {
    return this.request<any>('/servers/stats');
  }

  async getServersByLicense(licenseKey: string): Promise<ServerInstance[]> {
    return this.request<ServerInstance[]>(`/servers/license/${licenseKey}`);
  }

  async getServer(id: number): Promise<ServerInstance> {
    return this.request<ServerInstance>(`/servers/${id}`);
  }

  async deleteServer(id: number): Promise<ApiResponse> {
    return this.request<ApiResponse>(`/servers/${id}`, {
      method: 'DELETE',
    });
  }
}

export const api = new ApiClient();
export default api;
