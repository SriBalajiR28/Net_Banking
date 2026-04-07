import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const Login = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState(1); // 1 = login, 2 = otp
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [otp, setOtp] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // ✅ Step 1 — Login with username + password
  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await api.post('/auth/login', { username, password });
      setStep(2); // move to OTP step
    } catch (err) {
      setError('Invalid username or password');
    } finally {
      setLoading(false);
    }
  };

  // ✅ Step 2 — Validate OTP
  const handleOtp = async (e) => {
  e.preventDefault();
  setLoading(true);
  setError('');
  try {
    const res = await api.post(
      `/auth/validate-otp?username=${username}&otp=${otp}`
    );

    // ✅ Save all to localStorage
    localStorage.setItem('token', res.data.token);
    localStorage.setItem('role', res.data.role);
    localStorage.setItem('username', res.data.username);
    localStorage.setItem('sessionId', res.data.sessionId);
    localStorage.setItem('userId', res.data.userId); // ✅ from response

    // Redirect based on role
    if (res.data.role === 'ROLE_ADMIN') {
      navigate('/admin/dashboard');
    } else {
      navigate('/user/dashboard');
    }
  } catch (err) {
    setError('Invalid or expired OTP');
  } finally {
    setLoading(false);
  }
};

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center">
      <div className="bg-white p-8 rounded-2xl shadow-lg w-full max-w-md">

        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-blue-700">NetBanking</h1>
          <p className="text-gray-500 mt-1">Secure Online Banking</p>
        </div>

        {/* Step indicator */}
        <div className="flex items-center justify-center mb-6 gap-4">
          <div className={`flex items-center gap-2 text-sm font-medium
            ${step === 1 ? 'text-blue-600' : 'text-green-500'}`}>
            <span className={`w-7 h-7 rounded-full flex items-center
              justify-center text-white text-xs
              ${step === 1 ? 'bg-blue-600' : 'bg-green-500'}`}>
              {step === 1 ? '1' : '✓'}
            </span>
            Login
          </div>
          <div className="h-px w-10 bg-gray-300"/>
          <div className={`flex items-center gap-2 text-sm font-medium
            ${step === 2 ? 'text-blue-600' : 'text-gray-400'}`}>
            <span className={`w-7 h-7 rounded-full flex items-center
              justify-center text-white text-xs
              ${step === 2 ? 'bg-blue-600' : 'bg-gray-300'}`}>
              2
            </span>
            Verify OTP
          </div>
        </div>

        {/* Error message */}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-600
            text-sm px-4 py-3 rounded-lg mb-4">
            {error}
          </div>
        )}

        {/* Step 1 — Login form */}
        {step === 1 && (
          <form onSubmit={handleLogin} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Username
              </label>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Enter username"
                required
                className="w-full border border-gray-300 rounded-lg px-4 py-2
                  focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Password
              </label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter password"
                required
                className="w-full border border-gray-300 rounded-lg px-4 py-2
                  focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
              />
            </div>
            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 hover:bg-blue-700 text-white
                font-medium py-2 rounded-lg transition duration-200
                disabled:opacity-50">
              {loading ? 'Logging in...' : 'Login'}
            </button>
          </form>
        )}

        {/* Step 2 — OTP form */}
        {step === 2 && (
          <form onSubmit={handleOtp} className="space-y-4">
            <div className="bg-blue-50 border border-blue-200 text-blue-700
              text-sm px-4 py-3 rounded-lg">
              OTP sent to your registered email. Valid for 5 minutes.
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Enter OTP
              </label>
              <input
                type="text"
                value={otp}
                onChange={(e) => setOtp(e.target.value)}
                placeholder="Enter 6 digit OTP"
                maxLength={6}
                required
                className="w-full border border-gray-300 rounded-lg px-4 py-2
                  focus:outline-none focus:ring-2 focus:ring-blue-500
                  text-sm tracking-widest text-center text-lg"
              />
            </div>
            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 hover:bg-blue-700 text-white
                font-medium py-2 rounded-lg transition duration-200
                disabled:opacity-50">
              {loading ? 'Verifying...' : 'Verify OTP'}
            </button>
            <button
              type="button"
              onClick={() => setStep(1)}
              className="w-full text-sm text-gray-500 hover:text-blue-600
                transition duration-200">
              Back to login
            </button>
          </form>
        )}
      </div>
    </div>
  );
};

export default Login;