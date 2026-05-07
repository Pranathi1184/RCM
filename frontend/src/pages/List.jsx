import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../services/api";

export default function List() {
  const [changes, setChanges] = useState([]);

  useEffect(() => {
    api.get("/changes").then(({ data }) => {
      // Handle Spring Data Page response or direct array
      setChanges(data.content || data);
    });
  }, []);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Regulatory Records</h2>
          <p className="text-sm text-gray-500 mt-1">Manage and track all regulatory changes in one place.</p>
        </div>
        <Link 
          to="/changes/new" 
          className="inline-flex items-center px-4 py-2 bg-[#1B4F8A] text-white font-bold rounded-md hover:bg-[#153d6b] transition-all shadow-md min-h-[44px]"
        >
          <span className="mr-2">＋</span> Create New Change
        </Link>
      </div>

      <div className="bg-white shadow-sm border border-gray-200 rounded-lg overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-4 text-left text-xs font-bold text-gray-500 uppercase tracking-wider">Title & Body</th>
                <th className="px-6 py-4 text-left text-xs font-bold text-gray-500 uppercase tracking-wider">Category</th>
                <th className="px-6 py-4 text-left text-xs font-bold text-gray-500 uppercase tracking-wider">Status</th>
                <th className="px-6 py-4 text-left text-xs font-bold text-gray-500 uppercase tracking-wider">Effective Date</th>
                <th className="px-6 py-4 text-right text-xs font-bold text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {changes.map(c => (
                <tr key={c.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex flex-col">
                      <Link to={`/changes/${c.id}`} className="text-sm font-bold text-[#1B4F8A] hover:underline">
                        {c.title}
                      </Link>
                      <span className="text-xs text-gray-500 mt-0.5">{c.regulatoryBody}</span>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className="px-2.5 py-1 rounded-full text-xs font-bold bg-blue-50 text-blue-700 border border-blue-100">
                      {c.category}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span 
                      className="px-2.5 py-1 rounded-full text-xs font-bold text-white shadow-sm"
                      style={{ 
                        backgroundColor: 
                          c.status === 'APPROVED' ? '#10b981' : 
                          c.status === 'SUBMITTED' ? '#f59e0b' : 
                          c.status === 'DRAFT' ? '#6b7280' : '#ef4444' 
                      }}
                    >
                      {c.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600 font-medium">
                    {c.effectiveDate || 'N/A'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <Link to={`/changes/${c.id}`} className="text-[#1B4F8A] hover:text-blue-900 font-bold">
                      Details →
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      
      {changes.length === 0 && (
        <div className="text-center py-12 bg-white rounded-lg border-2 border-dashed border-gray-200">
          <p className="text-gray-500">No regulatory records found.</p>
        </div>
      )}
    </div>
  );
}
