import React, { ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";

interface ProtectedRouteProps {
  children: ReactNode;
}

export default function ProtectedRoute({ children }: ProtectedRouteProps) {
  const token = localStorage.getItem("token");
  const location = useLocation();
  if (!token) {
    // redirect to login, remembering where they were headed
    return <Navigate to="/login" replace state={{ from: location }} />;
  }
  return <>{children}</>;
}
