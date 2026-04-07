import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const AdminDashboard = () => {
  const navigate = useNavigate();
  const username = localStorage.getItem('username');

  const [activeTab, setActiveTab] = useState('users');
  const [users, setUsers] = useState([]);
  const [sessions, setSessions] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
  const [loginAttempts, setLoginAttempts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // ✅ Create user state
  const [createSuccess, setCreateSuccess] = useState('');
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [newUser, setNewUser] = useState({
    username: '',
    email: '',
    password: '',
    role: 'ROLE_USER'
  });

  // ✅ Raise alert state
  const [showAlertForm, setShowAlertForm] = useState(false);
  const [alertSuccess, setAlertSuccess] = useState('');
  const [newAlert, setNewAlert] = useState({
    userId: '',
    alertType: 'SUSPICIOUS_IP',
    severity: 'HIGH',
    message: '',
    ipAddress: ''
  });

  // ✅ Alert filter state
  const [alertFilter, setAlertFilter] = useState({
    status: 'ALL',
    severity: 'ALL',
    alertType: 'ALL'
  });

  // ✅ User search state
  const [userSearch, setUserSearch] = useState('');

  // ✅ Session filter state
  const [sessionFilter, setSessionFilter] = useState('ALL');

  // ✅ Filtered users
  const filteredUsers = users.filter(user => {
    const search = userSearch.toLowerCase();
    return (
      user.username.toLowerCase().includes(search) ||
      user.email.toLowerCase().includes(search) ||
      user.role.toLowerCase().includes(search)
    );
  });

  // ✅ Filtered sessions
  const filteredSessions = sessions.filter(session => {
    if (sessionFilter === 'ALL') return true;
    if (sessionFilter === 'ACTIVE') return session.endTime === null;
    if (sessionFilter === 'ENDED') return session.endTime !== null;
    return true;
  });

  // ✅ Filtered alerts
  const filteredAlerts = alerts.filter(alert => {
    const statusMatch = alertFilter.status === 'ALL' ||
      alert.status === alertFilter.status;
    const severityMatch = alertFilter.severity === 'ALL' ||
      alert.severity === alertFilter.severity;
    const typeMatch = alertFilter.alertType === 'ALL' ||
      alert.alertType === alertFilter.alertType;
    return statusMatch && severityMatch && typeMatch;
  });

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => {
    fetchData();
  }, [activeTab]);

  const fetchData = async () => {
    setLoading(true);
    setError('');
    try {
      if (activeTab === 'users') {
        const res = await api.get('/admin/users');
        setUsers(res.data);
      } else if (activeTab === 'sessions') {
        const res = await api.get('/admin/sessions');
        setSessions(res.data);
      } else if (activeTab === 'alerts') {
        const res = await api.get('/alerts/all');
        setAlerts(res.data);
      } else if (activeTab === 'audit') {
        const res = await api.get('/admin/audit');
        setAuditLogs(res.data);
      } else if (activeTab === 'attempts') {
        const res = await api.get('/admin/login-attempts');
        setLoginAttempts(res.data);
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

  // ✅ Create user
  const handleCreateUser = async (e) => {
    e.preventDefault();
    setError('');
    setCreateSuccess('');
    try {
      await api.post('/admin/users', newUser);
      setCreateSuccess('User created successfully!');
      setNewUser({
        username: '',
        email: '',
        password: '',
        role: 'ROLE_USER'
      });
      setShowCreateForm(false);
      fetchData();
    } catch (err) {
      setError('Failed to create user');
    }
  };

  // ✅ Delete user
  const handleDeleteUser = async (userId) => {
    if (!window.confirm(
      'Are you sure you want to delete this user?')) return;
    try {
      await api.delete(`/admin/users/${userId}`);
      setUsers(users.filter(u => u.userId !== userId));
    } catch (err) {
      setError('Failed to delete user');
    }
  };

  // ✅ End session
  const handleEndSession = async (sessionId) => {
    try {
      await api.put(`/admin/sessions/${sessionId}/end`);
      fetchData();
    } catch (err) {
      setError('Failed to end session');
    }
  };

  // ✅ Raise alert
  const handleRaiseAlert = async (e) => {
    e.preventDefault();
    setError('');
    setAlertSuccess('');
    try {
      await api.post(
        `/alerts/raise/${newAlert.userId}?alertType=${newAlert.alertType}&severity=${newAlert.severity}&message=${newAlert.message}&ipAddress=${newAlert.ipAddress}`
      );
      setAlertSuccess('Alert raised successfully!');
      setNewAlert({
        userId: '',
        alertType: 'SUSPICIOUS_IP',
        severity: 'HIGH',
        message: '',
        ipAddress: ''
      });
      setShowAlertForm(false);
      fetchData();
    } catch (err) {
      setError('Failed to raise alert');
    }
  };

  // ✅ Resolve alert
  const handleResolveAlert = async (alertId) => {
    try {
      await api.put(`/alerts/resolve/${alertId}`);
      fetchData();
    } catch (err) {
      setError('Failed to resolve alert');
    }
  };

  // ✅ Dismiss alert
  const handleDismissAlert = async (alertId) => {
    try {
      await api.put(`/alerts/dismiss/${alertId}`);
      fetchData();
    } catch (err) {
      setError('Failed to dismiss alert');
    }
  };

  const tabs = [
    { id: 'users',    label: 'Users' },
    { id: 'sessions', label: 'Sessions' },
    { id: 'alerts',   label: 'Security Alerts' },
    { id: 'audit',    label: 'Audit Logs' },
    { id: 'attempts', label: 'Login Attempts' },
  ];

  return (
    <div className="min-h-screen bg-gray-100">

      {/* Navbar */}
      <nav className="bg-blue-700 text-white px-6 py-4 flex
        justify-between items-center shadow">
        <h1 className="text-xl font-bold">NetBanking Admin</h1>
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

      <div className="max-w-7xl mx-auto px-4 py-6">

        {/* Tabs */}
        <div className="flex gap-2 mb-6 flex-wrap">
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
          <div className="bg-red-50 border border-red-200
            text-red-600 text-sm px-4 py-3 rounded-lg mb-4">
            {error}
          </div>
        )}

        {/* Create success */}
        {createSuccess && (
          <div className="bg-green-50 border border-green-200
            text-green-600 text-sm px-4 py-3 rounded-lg mb-4">
            {createSuccess}
          </div>
        )}

        {/* Alert success */}
        {alertSuccess && (
          <div className="bg-green-50 border border-green-200
            text-green-600 text-sm px-4 py-3 rounded-lg mb-4">
            {alertSuccess}
          </div>
        )}

        {/* Loading */}
        {loading && (
          <div className="text-center text-gray-500 py-10">
            Loading...
          </div>
        )}

        {/* ── Users Tab ── */}
        {!loading && activeTab === 'users' && (
          <div>

            {/* Top bar */}
            <div className="flex justify-between items-center
              mb-4 gap-4">
              <div className="flex-1 max-w-sm">
                <input
                  type="text"
                  value={userSearch}
                  onChange={(e) => setUserSearch(e.target.value)}
                  placeholder="Search by username, email or role..."
                  className="w-full border border-gray-300
                    rounded-lg px-4 py-2 text-sm focus:outline-none
                    focus:ring-2 focus:ring-blue-500 bg-white"
                />
              </div>
              <button
                onClick={() => {
                  setShowCreateForm(!showCreateForm);
                  setCreateSuccess('');
                  setError('');
                }}
                className="bg-blue-600 hover:bg-blue-700 text-white
                  text-sm font-medium px-4 py-2 rounded-lg
                  transition">
                {showCreateForm ? 'Cancel' : '+ Create User'}
              </button>
            </div>

            {/* Create User Form */}
            {showCreateForm && (
              <div className="bg-white rounded-2xl shadow
                p-6 mb-6">
                <h2 className="text-lg font-semibold text-gray-800
                  mb-4">
                  Create New User
                </h2>
                <form onSubmit={handleCreateUser}
                  className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium
                      text-gray-700 mb-1">Username</label>
                    <input
                      type="text"
                      value={newUser.username}
                      onChange={(e) => setNewUser({
                        ...newUser, username: e.target.value})}
                      placeholder="Enter username"
                      required
                      className="w-full border border-gray-300
                        rounded-lg px-4 py-2 focus:outline-none
                        focus:ring-2 focus:ring-blue-500 text-sm"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium
                      text-gray-700 mb-1">Email</label>
                    <input
                      type="email"
                      value={newUser.email}
                      onChange={(e) => setNewUser({
                        ...newUser, email: e.target.value})}
                      placeholder="Enter email"
                      required
                      className="w-full border border-gray-300
                        rounded-lg px-4 py-2 focus:outline-none
                        focus:ring-2 focus:ring-blue-500 text-sm"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium
                      text-gray-700 mb-1">Password</label>
                    <input
                      type="password"
                      value={newUser.password}
                      onChange={(e) => setNewUser({
                        ...newUser, password: e.target.value})}
                      placeholder="Enter password"
                      required
                      className="w-full border border-gray-300
                        rounded-lg px-4 py-2 focus:outline-none
                        focus:ring-2 focus:ring-blue-500 text-sm"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium
                      text-gray-700 mb-1">Role</label>
                    <select
                      value={newUser.role}
                      onChange={(e) => setNewUser({
                        ...newUser, role: e.target.value})}
                      className="w-full border border-gray-300
                        rounded-lg px-4 py-2 focus:outline-none
                        focus:ring-2 focus:ring-blue-500 text-sm">
                      <option value="ROLE_USER">User</option>
                      <option value="ROLE_ADMIN">Admin</option>
                    </select>
                  </div>
                  <div className="col-span-2 flex justify-end
                    gap-3">
                    <button type="button"
                      onClick={() => setShowCreateForm(false)}
                      className="px-4 py-2 text-sm text-gray-600
                        hover:text-gray-800 transition">
                      Cancel
                    </button>
                    <button type="submit"
                      className="bg-blue-600 hover:bg-blue-700
                        text-white text-sm font-medium px-6 py-2
                        rounded-lg transition">
                      Create User
                    </button>
                  </div>
                </form>
              </div>
            )}

            {/* Users Table */}
            <div className="bg-white rounded-2xl shadow
              overflow-hidden">
              <div className="px-6 py-4 border-b flex
                justify-between items-center">
                <h2 className="text-lg font-semibold text-gray-800">
                  All Users
                </h2>
                <span className="text-sm text-gray-500">
                  Showing {filteredUsers.length} of {users.length}
                </span>
              </div>
              <table className="w-full text-sm">
                <thead className="bg-gray-50 text-gray-600">
                  <tr>
                    <th className="px-6 py-3 text-left">ID</th>
                    <th className="px-6 py-3 text-left">Username</th>
                    <th className="px-6 py-3 text-left">Email</th>
                    <th className="px-6 py-3 text-left">Role</th>
                    <th className="px-6 py-3 text-left">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {filteredUsers.length === 0 ? (
                    <tr>
                      <td colSpan="5" className="px-6 py-10
                        text-center text-gray-500">
                        No users match your search
                      </td>
                    </tr>
                  ) : (
                    filteredUsers.map(user => (
                      <tr key={user.userId}
                        className="hover:bg-gray-50">
                        <td className="px-6 py-3">{user.userId}</td>
                        <td className="px-6 py-3 font-medium">
                          {user.username}
                        </td>
                        <td className="px-6 py-3">{user.email}</td>
                        <td className="px-6 py-3">
                          <span className={`px-2 py-1 rounded-full
                            text-xs font-medium
                            ${user.role === 'ROLE_ADMIN'
                              ? 'bg-purple-100 text-purple-700'
                              : 'bg-blue-100 text-blue-700'}`}>
                            {user.role}
                          </span>
                        </td>
                        <td className="px-6 py-3">
                          <button
                            onClick={() =>
                              handleDeleteUser(user.userId)}
                            className="text-red-500
                              hover:text-red-700 text-xs
                              font-medium">
                            Delete
                          </button>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* ── Sessions Tab ── */}
        {!loading && activeTab === 'sessions' && (
          <div>

            {/* Filter buttons */}
            <div className="flex gap-3 mb-4 items-center">
              <span className="text-sm text-gray-600 font-medium">
                Filter:
              </span>
              <button
                onClick={() => setSessionFilter('ALL')}
                className={`px-4 py-2 rounded-lg text-sm
                  font-medium transition
                  ${sessionFilter === 'ALL'
                    ? 'bg-blue-600 text-white'
                    : 'bg-white text-gray-600 hover:bg-blue-50'}`}>
                All ({sessions.length})
              </button>
<button
  onClick={() => setSessionFilter('ACTIVE')}
  className={`px-4 py-2 rounded-lg text-sm font-medium transition ${sessionFilter === 'ACTIVE' ? 'bg-green-600 text-white' : 'bg-white text-gray-600 hover:bg-green-50'}`}>
  Active ({sessions.filter(s => s.endTime === null).length})
</button>
              <button
  onClick={() => setSessionFilter('ENDED')}
  className={`px-4 py-2 rounded-lg text-sm font-medium transition ${sessionFilter === 'ENDED' ? 'bg-gray-600 text-white' : 'bg-white text-gray-600 hover:bg-gray-50'}`}>
  Ended ({sessions.filter(s => s.endTime !== null).length})
</button>
            </div>

            {/* Sessions Table */}
            <div className="bg-white rounded-2xl shadow
              overflow-hidden">
              <div className="px-6 py-4 border-b flex
                justify-between items-center">
                <h2 className="text-lg font-semibold text-gray-800">
                  All Sessions
                </h2>
                <span className="text-sm text-gray-500">
                  Showing {filteredSessions.length} of{' '}
                  {sessions.length}
                </span>
              </div>
              <table className="w-full text-sm">
                <thead className="bg-gray-50 text-gray-600">
                  <tr>
                    <th className="px-6 py-3 text-left">ID</th>
                    <th className="px-6 py-3 text-left">User ID</th>
                    <th className="px-6 py-3 text-left">
                      IP Address
                    </th>
                    <th className="px-6 py-3 text-left">Device</th>
                    <th className="px-6 py-3 text-left">
                      Start Time
                    </th>
                    <th className="px-6 py-3 text-left">
                      Expires At
                    </th>
                    <th className="px-6 py-3 text-left">Status</th>
                    <th className="px-6 py-3 text-left">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {filteredSessions.length === 0 ? (
                    <tr>
                      <td colSpan="8" className="px-6 py-10
                        text-center text-gray-500">
                        No sessions found
                      </td>
                    </tr>
                  ) : (
                    filteredSessions.map(session => (
                      <tr key={session.id}
                        className="hover:bg-gray-50">
                        <td className="px-6 py-3">{session.id}</td>
                        <td className="px-6 py-3">
                          {session.userId}
                        </td>
                        <td className="px-6 py-3">
                          {session.ipAddress}
                        </td>
                        <td className="px-6 py-3 max-w-xs truncate">
                          {session.deviceInfo}
                        </td>
                        <td className="px-6 py-3">
                          {new Date(session.startTime)
                            .toLocaleString()}
                        </td>
                        <td className="px-6 py-3">
                          {new Date(session.expiresAt)
                            .toLocaleString()}
                        </td>
                        <td className="px-6 py-3">
                          <span className={`px-2 py-1 rounded-full
                            text-xs font-medium
                            ${session.endTime === null
                              ? 'bg-green-100 text-green-700'
                              : 'bg-gray-100 text-gray-500'}`}>
                            {session.endTime === null
                              ? 'Active' : 'Ended'}
                          </span>
                        </td>
                        <td className="px-6 py-3">
                          {session.endTime === null && (
                            <button
                              onClick={() =>
                                handleEndSession(session.id)}
                              className="text-red-500
                                hover:text-red-700 text-xs
                                font-medium">
                              End
                            </button>
                          )}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* ── Security Alerts Tab ── */}
        {!loading && activeTab === 'alerts' && (
          <div>

            {/* Top bar — filters + raise alert button */}
            <div className="flex flex-wrap gap-3 mb-4
              items-center justify-between">
              <div className="flex gap-3 flex-wrap">
                <select
                  value={alertFilter.status}
                  onChange={(e) => setAlertFilter({
                    ...alertFilter, status: e.target.value})}
                  className="border border-gray-300 rounded-lg
                    px-3 py-2 text-sm focus:outline-none
                    focus:ring-2 focus:ring-blue-500 bg-white">
                  <option value="ALL">All Status</option>
                  <option value="OPEN">Open</option>
                  <option value="RESOLVED">Resolved</option>
                  <option value="DISMISSED">Dismissed</option>
                </select>
                <select
                  value={alertFilter.severity}
                  onChange={(e) => setAlertFilter({
                    ...alertFilter, severity: e.target.value})}
                  className="border border-gray-300 rounded-lg
                    px-3 py-2 text-sm focus:outline-none
                    focus:ring-2 focus:ring-blue-500 bg-white">
                  <option value="ALL">All Severity</option>
                  <option value="LOW">Low</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="HIGH">High</option>
                  <option value="CRITICAL">Critical</option>
                </select>
                <select
                  value={alertFilter.alertType}
                  onChange={(e) => setAlertFilter({
                    ...alertFilter, alertType: e.target.value})}
                  className="border border-gray-300 rounded-lg
                    px-3 py-2 text-sm focus:outline-none
                    focus:ring-2 focus:ring-blue-500 bg-white">
                  <option value="ALL">All Types</option>
                  <option value="SUSPICIOUS_IP">
                    Suspicious IP
                  </option>
                  <option value="OTP_ABUSE">OTP Abuse</option>
                  <option value="CONCURRENT_SESSION">
                    Concurrent Session
                  </option>
                  <option value="MULTIPLE_FAILED_LOGINS">
                    Multiple Failed Logins
                  </option>
                  <option value="ACCOUNT_BLOCKED">
                    Account Blocked
                  </option>
                </select>
                <button
                  onClick={() => setAlertFilter({
                    status: 'ALL',
                    severity: 'ALL',
                    alertType: 'ALL'
                  })}
                  className="border border-gray-300 rounded-lg
                    px-3 py-2 text-sm text-gray-600
                    hover:bg-gray-50 bg-white transition">
                  Reset
                </button>
              </div>
              <button
                onClick={() => {
                  setShowAlertForm(!showAlertForm);
                  setAlertSuccess('');
                  setError('');
                }}
                className="bg-red-600 hover:bg-red-700 text-white
                  text-sm font-medium px-4 py-2 rounded-lg
                  transition">
                {showAlertForm ? 'Cancel' : '+ Raise Alert'}
              </button>
            </div>

            {/* Raise Alert Form */}
            {showAlertForm && (
              <div className="bg-white rounded-2xl shadow
                p-6 mb-6">
                <h2 className="text-lg font-semibold text-gray-800
                  mb-4">
                  Raise Security Alert
                </h2>
                <form onSubmit={handleRaiseAlert}
                  className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium
                      text-gray-700 mb-1">User ID</label>
                    <input
                      type="number"
                      value={newAlert.userId}
                      onChange={(e) => setNewAlert({
                        ...newAlert, userId: e.target.value})}
                      placeholder="Enter user ID"
                      required
                      className="w-full border border-gray-300
                        rounded-lg px-4 py-2 focus:outline-none
                        focus:ring-2 focus:ring-red-500 text-sm"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium
                      text-gray-700 mb-1">Alert Type</label>
                    <select
                      value={newAlert.alertType}
                      onChange={(e) => setNewAlert({
                        ...newAlert, alertType: e.target.value})}
                      className="w-full border border-gray-300
                        rounded-lg px-4 py-2 focus:outline-none
                        focus:ring-2 focus:ring-red-500 text-sm">
                      <option value="SUSPICIOUS_IP">
                        Suspicious IP
                      </option>
                      <option value="OTP_ABUSE">OTP Abuse</option>
                      <option value="CONCURRENT_SESSION">
                        Concurrent Session
                      </option>
                      <option value="ACCOUNT_BLOCKED">
                        Account Blocked
                      </option>
                      <option value="MULTIPLE_FAILED_LOGINS">
                        Multiple Failed Logins
                      </option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium
                      text-gray-700 mb-1">Severity</label>
                    <select
                      value={newAlert.severity}
                      onChange={(e) => setNewAlert({
                        ...newAlert, severity: e.target.value})}
                      className="w-full border border-gray-300
                        rounded-lg px-4 py-2 focus:outline-none
                        focus:ring-2 focus:ring-red-500 text-sm">
                      <option value="LOW">Low</option>
                      <option value="MEDIUM">Medium</option>
                      <option value="HIGH">High</option>
                      <option value="CRITICAL">Critical</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium
                      text-gray-700 mb-1">IP Address</label>
                    <input
                      type="text"
                      value={newAlert.ipAddress}
                      onChange={(e) => setNewAlert({
                        ...newAlert, ipAddress: e.target.value})}
                      placeholder="e.g. 192.168.1.1"
                      required
                      className="w-full border border-gray-300
                        rounded-lg px-4 py-2 focus:outline-none
                        focus:ring-2 focus:ring-red-500 text-sm"
                    />
                  </div>
                  <div className="col-span-2">
                    <label className="block text-sm font-medium
                      text-gray-700 mb-1">Message</label>
                    <input
                      type="text"
                      value={newAlert.message}
                      onChange={(e) => setNewAlert({
                        ...newAlert, message: e.target.value})}
                      placeholder="Describe the security issue"
                      required
                      className="w-full border border-gray-300
                        rounded-lg px-4 py-2 focus:outline-none
                        focus:ring-2 focus:ring-red-500 text-sm"
                    />
                  </div>
                  <div className="col-span-2 flex justify-end
                    gap-3">
                    <button type="button"
                      onClick={() => setShowAlertForm(false)}
                      className="px-4 py-2 text-sm text-gray-600
                        hover:text-gray-800 transition">
                      Cancel
                    </button>
                    <button type="submit"
                      className="bg-red-600 hover:bg-red-700
                        text-white text-sm font-medium px-6 py-2
                        rounded-lg transition">
                      Raise Alert
                    </button>
                  </div>
                </form>
              </div>
            )}

            {/* Alerts Table */}
            <div className="bg-white rounded-2xl shadow
              overflow-hidden">
              <div className="px-6 py-4 border-b flex
                justify-between items-center">
                <h2 className="text-lg font-semibold text-gray-800">
                  Security Alerts
                </h2>
                <span className="text-sm text-gray-500">
                  Showing {filteredAlerts.length} of {alerts.length}
                </span>
              </div>
              <table className="w-full text-sm">
                <thead className="bg-gray-50 text-gray-600">
                  <tr>
                    <th className="px-6 py-3 text-left">ID</th>
                    <th className="px-6 py-3 text-left">Type</th>
                    <th className="px-6 py-3 text-left">Severity</th>
                    <th className="px-6 py-3 text-left">Message</th>
                    <th className="px-6 py-3 text-left">IP</th>
                    <th className="px-6 py-3 text-left">Status</th>
                    <th className="px-6 py-3 text-left">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {filteredAlerts.length === 0 ? (
                    <tr>
                      <td colSpan="7" className="px-6 py-10
                        text-center text-gray-500">
                        No alerts match the selected filters
                      </td>
                    </tr>
                  ) : (
                    filteredAlerts.map(alert => (
                      <tr key={alert.alertId}
                        className="hover:bg-gray-50">
                        <td className="px-6 py-3">
                          {alert.alertId}
                        </td>
                        <td className="px-6 py-3 font-medium">
                          {alert.alertType}
                        </td>
                        <td className="px-6 py-3">
                          <span className={`px-2 py-1 rounded-full
                            text-xs font-medium
                            ${alert.severity === 'CRITICAL'
                              ? 'bg-red-100 text-red-700'
                              : alert.severity === 'HIGH'
                              ? 'bg-orange-100 text-orange-700'
                              : alert.severity === 'MEDIUM'
                              ? 'bg-yellow-100 text-yellow-700'
                              : 'bg-green-100 text-green-700'}`}>
                            {alert.severity}
                          </span>
                        </td>
                        <td className="px-6 py-3">
                          {alert.message}
                        </td>
                        <td className="px-6 py-3">
                          {alert.ipAddress}
                        </td>
                        <td className="px-6 py-3">
                          <span className={`px-2 py-1 rounded-full
                            text-xs font-medium
                            ${alert.status === 'OPEN'
                              ? 'bg-red-100 text-red-700'
                              : alert.status === 'RESOLVED'
                              ? 'bg-green-100 text-green-700'
                              : 'bg-gray-100 text-gray-500'}`}>
                            {alert.status}
                          </span>
                        </td>
                        <td className="px-6 py-3">
                          <div className="flex gap-2">
                            {alert.status === 'OPEN' && (
                              <>
                                <button
                                  onClick={() =>
                                    handleResolveAlert(
                                      alert.alertId)}
                                  className="text-green-600
                                    hover:text-green-800 text-xs
                                    font-medium">
                                  Resolve
                                </button>
                                <button
                                  onClick={() =>
                                    handleDismissAlert(
                                      alert.alertId)}
                                  className="text-gray-500
                                    hover:text-gray-700 text-xs
                                    font-medium">
                                  Dismiss
                                </button>
                              </>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* ── Audit Logs Tab ── */}
        {!loading && activeTab === 'audit' && (
          <div className="bg-white rounded-2xl shadow
            overflow-hidden">
            <div className="px-6 py-4 border-b">
              <h2 className="text-lg font-semibold text-gray-800">
                Audit Logs
              </h2>
            </div>
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-gray-600">
                <tr>
                  <th className="px-6 py-3 text-left">ID</th>
                  <th className="px-6 py-3 text-left">Admin ID</th>
                  <th className="px-6 py-3 text-left">Action</th>
                  <th className="px-6 py-3 text-left">Target ID</th>
                  <th className="px-6 py-3 text-left">IP Address</th>
                  <th className="px-6 py-3 text-left">Timestamp</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {auditLogs.map(log => (
                  <tr key={log.id} className="hover:bg-gray-50">
                    <td className="px-6 py-3">{log.id}</td>
                    <td className="px-6 py-3">{log.adminId}</td>
                    <td className="px-6 py-3">
                      <span className="px-2 py-1 rounded-full
                        text-xs font-medium bg-blue-100
                        text-blue-700">
                        {log.actionType}
                      </span>
                    </td>
                    <td className="px-6 py-3">
                      {log.targetId ?? '-'}
                    </td>
                    <td className="px-6 py-3">{log.ipAddress}</td>
                    <td className="px-6 py-3">
                      {new Date(log.timestamp).toLocaleString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* ── Login Attempts Tab ── */}
        {!loading && activeTab === 'attempts' && (
          <div className="bg-white rounded-2xl shadow
            overflow-hidden">
            <div className="px-6 py-4 border-b">
              <h2 className="text-lg font-semibold text-gray-800">
                Login Attempts
              </h2>
            </div>
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-gray-600">
                <tr>
                  <th className="px-6 py-3 text-left">ID</th>
                  <th className="px-6 py-3 text-left">User ID</th>
                  <th className="px-6 py-3 text-left">Status</th>
                  <th className="px-6 py-3 text-left">IP Address</th>
                  <th className="px-6 py-3 text-left">Timestamp</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {loginAttempts.map(attempt => (
                  <tr key={attempt.attemptId}
                    className="hover:bg-gray-50">
                    <td className="px-6 py-3">
                      {attempt.attemptId}
                    </td>
                    <td className="px-6 py-3">{attempt.userId}</td>
                    <td className="px-6 py-3">
                      <span className={`px-2 py-1 rounded-full
                        text-xs font-medium
                        ${attempt.status === 'OTP_SUCCESS'
                          ? 'bg-green-100 text-green-700'
                          : attempt.status === 'OTP_SENT'
                          ? 'bg-blue-100 text-blue-700'
                          : 'bg-red-100 text-red-700'}`}>
                        {attempt.status}
                      </span>
                    </td>
                    <td className="px-6 py-3">
                      {attempt.ipAddress ?? '-'}
                    </td>
                    <td className="px-6 py-3">
                      {new Date(attempt.timestamp).toLocaleString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

      </div>
    </div>
  );
};

export default AdminDashboard;