import React from "react";
import { Link, useNavigate } from "react-router-dom";

export default function Layout({ children }) {
  const navigate = useNavigate();
  const token = localStorage.getItem("token");

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    navigate("/login");
  };

  return (
    <div className="min-h-screen bg-gray-50 text-gray-900 font-sans">
      <header className="bg-[#1B4F8A] shadow-lg sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center space-x-8">
              <Link to="/dashboard" className="flex items-center space-x-2 text-white group">
                <span className="text-2xl">⚖️</span>
                <span className="text-xl font-bold tracking-tight group-hover:text-blue-200 transition-colors">Regulatory Tool</span>
              </Link>
              <nav className="hidden md:flex items-center space-x-1">
                <Link 
                  to="/dashboard" 
                  className="px-4 py-2 rounded-md text-sm font-medium text-blue-100 hover:text-white hover:bg-[#153d6b] transition-all"
                >
                  Dashboard
                </Link>
                <Link 
                  to="/changes" 
                  className="px-4 py-2 rounded-md text-sm font-medium text-blue-100 hover:text-white hover:bg-[#153d6b] transition-all"
                >
                  Records
                </Link>
              </nav>
            </div>
            
            <div className="flex items-center space-x-4">
              <div className="hidden sm:flex flex-col items-end text-xs text-blue-200 mr-2">
                <span className="font-bold text-white">{localStorage.getItem("role") || "VIEWER"}</span>
                <span>Session Active</span>
              </div>
              {token ? (
                <button 
                  onClick={handleLogout} 
                  className="px-4 py-2 rounded-md bg-red-500/10 text-red-100 text-sm font-bold border border-red-500/20 hover:bg-red-500 hover:text-white transition-all shadow-sm min-h-[40px]"
                >
                  Logout
                </button>
              ) : (
                <Link 
                  to="/login" 
                  className="px-4 py-2 rounded-md bg-white text-[#1B4F8A] text-sm font-bold hover:bg-blue-50 transition-all shadow-md min-h-[40px]"
                >
                  Login
                </Link>
              )}
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 min-h-[calc(100vh-160px)]">
        {children}
      </main>

      <footer className="bg-white border-t border-gray-200 py-8">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col md:flex-row justify-between items-center space-y-4 md:space-y-0">
            <div className="text-sm text-gray-500 font-medium">
              © {new Date().getFullYear()} Regulatory Change Management. All rights reserved.
            </div>
            <div className="flex space-x-6 text-xs text-gray-400 uppercase tracking-widest font-bold">
              <span>Security Verified</span>
              <span>•</span>
              <span>AI Optimized</span>
              <span>•</span>
              <span>v1.0.0</span>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}
