import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../services/api";

export default function Dashboard() {
  const [stats, setStats] = useState({ total: 0, pending: 0, byStatus: {}, byPriority: {} });
  const [query, setQuery] = useState("");
  const [queryResult, setQueryResult] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    api.get("/changes/stats").then(({ data }) => {
      setStats({
        total: data.totalActive || 0,
        pending: (data.byStatus?.DRAFT || 0) + (data.byStatus?.SUBMITTED || 0),
        byStatus: data.byStatus || {},
        byPriority: data.byPriority || {}
      });
    }).catch(() => {
      // fallback if stats endpoint fails
      api.get("/changes").then(({ data }) => {
        const items = data.content || data;
        setStats(prev => ({ ...prev, total: items.length }));
      });
    });
  }, []);

  const handleQuery = async (e) => {
    e.preventDefault();
    if (!query.trim()) return;
    setLoading(true);
    try {
      const { data } = await api.get(`/changes/query?q=${encodeURIComponent(query)}`);
      setQueryResult(data);
    } catch (err) {
      alert("Search failed: " + (err.response?.data?.error || err.message));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <h2 className="text-2xl font-semibold text-gray-800">Regulatory Dashboard</h2>

      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
        <div className="bg-white rounded shadow p-4 border-l-4 border-blue-500">
          <div className="text-sm text-gray-500 font-medium">Total Active</div>
          <div className="text-3xl font-bold text-gray-900">{stats.total}</div>
        </div>

        <div className="bg-white rounded shadow p-4 border-l-4 border-yellow-500">
          <div className="text-sm text-gray-500 font-medium">Pending Review</div>
          <div className="text-3xl font-bold text-yellow-600">{stats.pending}</div>
        </div>

        <div className="bg-white rounded shadow p-4 border-l-4 border-red-500">
          <div className="text-sm text-gray-500 font-medium">High Priority</div>
          <div className="text-3xl font-bold text-red-600">{stats.byPriority?.P1 || 0}</div>
        </div>

        <div className="bg-white rounded shadow p-4 border-l-4 border-green-500">
          <div className="text-sm text-gray-500 font-medium">Approved</div>
          <div className="text-3xl font-bold text-green-600">{stats.byStatus?.APPROVED || 0}</div>
        </div>
      </div>

      <div className="bg-white rounded shadow p-6 border-t-2 border-purple-500">
        <h3 className="text-lg font-semibold mb-4 flex items-center">
          <span className="mr-2">🔍</span> AI Regulatory Assistant (RAG)
        </h3>
        <form onSubmit={handleQuery} className="flex gap-2">
          <input 
            className="flex-1 px-4 py-2 border rounded shadow-sm focus:ring-2 focus:ring-purple-300 outline-none"
            placeholder="Ask a question about regulations (e.g., 'What are the SEC updates for 2026?')"
            value={query}
            onChange={e => setQuery(e.target.value)}
          />
          <button 
            type="submit" 
            disabled={loading}
            className="px-6 py-2 bg-purple-600 text-white rounded hover:bg-purple-700 disabled:opacity-50"
          >
            {loading ? "Searching..." : "Ask AI"}
          </button>
        </form>

        {queryResult && (
          <div className="mt-4 p-4 bg-purple-50 rounded border border-purple-100">
            <div className="font-medium text-purple-900 mb-2 text-sm italic">AI Answer:</div>
            <div className="text-gray-800 text-sm leading-relaxed">{queryResult.answer}</div>
            {queryResult.sources && queryResult.sources.length > 0 && (
              <div className="mt-3">
                <div className="text-xs font-bold text-purple-700 uppercase tracking-wider mb-1">Sources:</div>
                <div className="flex flex-wrap gap-2">
                  {queryResult.sources.map((src, i) => (
                    <span key={i} className="text-xs bg-white px-2 py-1 rounded border border-purple-200 text-purple-600">{src}</span>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
      </div>

      <div className="flex justify-between items-center pt-4">
        <Link to="/changes" className="px-4 py-2 bg-gray-100 text-gray-700 rounded hover:bg-gray-200 text-sm font-medium">View All Records</Link>
        <Link to="/changes/new" className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 text-sm font-medium">+ Add New Change</Link>
      </div>
    </div>
  );
}
