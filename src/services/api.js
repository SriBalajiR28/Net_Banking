import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8085',
  headers: {
    'Content-Type': 'application/json',
  }
});

// ✅ Public endpoints that don't need a token
const publicEndpoints = [
  '/auth/login',
  '/auth/register',
  '/auth/register-admin',
  '/auth/validate-otp'
];

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');

  // ✅ Only attach token for protected endpoints
  const isPublic = publicEndpoints.some(endpoint =>
    config.url.includes(endpoint));

  if (token && !isPublic) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;