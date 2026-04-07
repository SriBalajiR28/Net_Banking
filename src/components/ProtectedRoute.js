import React from 'react';
import { Navigate } from 'react-router-dom';

const ProtectedRoute = ({ children, requiredRole }) => {
  const token = localStorage.getItem('token');
  const role = localStorage.getItem('role');

  // ✅ Not logged in — redirect to login
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  // ✅ Wrong role — redirect to login
  if (requiredRole && role !== requiredRole) {
    return <Navigate to="/login" replace />;
  }

  return children;
};

export default ProtectedRoute;