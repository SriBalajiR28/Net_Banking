import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const UserDashboard = () => {
  const navigate = useNavigate();
  const username = localStorage.getItem('username');
  const userId = localStorage.getItem('userId');

  const [activeTab, setActiveTab] = useState('profile');
  const [profile, setProfile] = useState(null);
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchData();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTab]);

  const fetchData = async () => {
    setLoading(true);
    setError('');
    try {
      if (activeTab === 'profile') {
        const res = await api.get('/user/profile/me');
        setProfile(res.data);
      } else if (activeTab === 'sessions') {
        // ✅ Get userId from profile — more reliable than localStorage
        const profileRes = await api.get('/user/profile/me');
        const id = profileRes.data.userId;
        const res = await api.get(`/session/user/${id}/active`);
        setSessions(res.data);
      }
    } catch (err) {
      setError('Failed to fetch data');
    } finally {
      setLoading(false);
    }
  };

  // ✅ Logout
  const handleLogout = async () => {
    try {
      await api.post('/auth/logout');
    } catch (err) {}
    localStorage.clear();
    navigate('/login');
  };

  // ✅ End session by ID
  const handleEndSession = async (sessionId) => {
    try {
      await api.put(`/session/end/${sessionId}`);
      fetchData();
    } catch (err) {
      setError('Failed to end session');
    }
  };

  const tabs = [
    { id: 'profile',  label: 'My Profile' },
    { id: 'sessions', label: 'My Sessions' },
  ];

  return (
    <div className="min-h-screen bg-gray-100">

      {/* Navbar */}
      <nav className="bg-blue-700 text-white px-6 py-4 flex
        justify-between items-center shadow">
        <h1 className="text-xl font-bold">NetBanking</h1>
        <div className="flex items-center gap-4">
          <span className="text-sm">Welcome, {username}</span>
          <button
            onClick={handleLogout}
            className="bg-white text-blue-700 text-sm font-medium
              px-4 py-1.5 rounded-lg hover:bg-blue-50 transition">
            Logout
          </button>
        </div>
      </nav>

      <div className="max-w-4xl mx-auto px-4 py-6">

        {/* Tabs */}
        <div className="flex gap-2 mb-6">
          {tabs.map(tab => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`px-4 py-2 rounded-lg text-sm font-medium
                transition duration-200
                ${activeTab === tab.id
                  ? 'bg-blue-600 text-white'
                  : 'bg-white text-gray-600 hover:bg-blue-50'}`}>
              {tab.label}
            </button>
          ))}
        </div>

        {/* Error */}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-600
            text-sm px-4 py-3 rounded-lg mb-4">
            {error}
          </div>
        )}

        {/* Loading */}
        {loading && (
          <div className="text-center text-gray-500 py-10">
            Loading...
          </div>
        )}

        {/* ── Profile Tab ── */}
        {!loading && activeTab === 'profile' && profile && (
          <div className="bg-white rounded-2xl shadow p-6">
            <h2 className="text-lg font-semibold text-gray-800 mb-6">
              My Profile
            </h2>
            <div className="grid grid-cols-2 gap-4">
              <div className="bg-gray-50 rounded-lg p-4">
                <p className="text-xs text-gray-500 mb-1">User ID</p>
                <p className="font-medium text-gray-800">
                  {profile.userId}
                </p>
              </div>
              <div className="bg-gray-50 rounded-lg p-4">
                <p className="text-xs text-gray-500 mb-1">Username</p>
                <p className="font-medium text-gray-800">
                  {profile.username}
                </p>
              </div>
              <div className="bg-gray-50 rounded-lg p-4">
                <p className="text-xs text-gray-500 mb-1">Email</p>
                <p className="font-medium text-gray-800">
                  {profile.email}
                </p>
              </div>
              <div className="bg-gray-50 rounded-lg p-4">
                <p className="text-xs text-gray-500 mb-1">Role</p>
                <span className={`px-2 py-1 rounded-full text-xs
                  font-medium
                  ${profile.role === 'ROLE_ADMIN'
                    ? 'bg-purple-100 text-purple-700'
                    : 'bg-blue-100 text-blue-700'}`}>
                  {profile.role}
                </span>
              </div>
            </div>
          </div>
        )}

        {/* ── Sessions Tab ── */}
        {!loading && activeTab === 'sessions' && (
          <div className="bg-white rounded-2xl shadow overflow-hidden">
            <div className="px-6 py-4 border-b">
              <h2 className="text-lg font-semibold text-gray-800">
                My Active Sessions
              </h2>
            </div>
            {sessions.length === 0 ? (
              <div className="text-center text-gray-500 py-10">
                No active sessions found
              </div>
            ) : (
              <table className="w-full text-sm">
                <thead className="bg-gray-50 text-gray-600">
                  <tr>
                    <th className="px-6 py-3 text-left">ID</th>
                    <th className="px-6 py-3 text-left">IP Address</th>
                    <th className="px-6 py-3 text-left">Device</th>
                    <th className="px-6 py-3 text-left">Start Time</th>
                    <th className="px-6 py-3 text-left">Expires At</th>
                    <th className="px-6 py-3 text-left">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {sessions.map(session => (
                    <tr key={session.id} className="hover:bg-gray-50">
                      <td className="px-6 py-3">{session.id}</td>
                      <td className="px-6 py-3">
                        {session.ipAddress}
                      </td>
                      <td className="px-6 py-3 max-w-xs truncate">
                        {session.deviceInfo}
                      </td>
                      <td className="px-6 py-3">
                        {new Date(session.startTime).toLocaleString()}
                      </td>
                      <td className="px-6 py-3">
                        {new Date(session.expiresAt).toLocaleString()}
                      </td>
                      <td className="px-6 py-3">
                        <button
                          onClick={() => handleEndSession(session.id)}
                          className="text-red-500 hover:text-red-700
                            text-xs font-medium">
                          End Session
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default UserDashboard;