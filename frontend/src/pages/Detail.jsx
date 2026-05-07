import React, { useEffect, useState } from "react";
import { useParams, Link, useNavigate } from "react-router-dom";
import api from "../services/api";

export default function Detail() {
  const { id } = useParams();
  const [change, setChange] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    api.get(`/changes/${id}`).then(({ data }) => setChange(data));
  }, [id]);

  const handleDelete = async () => {
    await api.delete(`/changes/${id}`);
    navigate("/changes");
  };

  const generateAiInsights = async () => {
    try {
      const { data } = await api.post(`/changes/${id}/ai-insights`);
      setChange(data);
    } catch (err) {
      alert("Error generating insights: " + (err.response?.data?.error || err.message));
    }
  };

  if (!change) return <p>Loading...</p>;

  return (
    <div className="max-w-4xl bg-white rounded shadow p-6">
      <div className="flex items-start justify-between">
        <div>
          <h2 className="text-2xl font-semibold">{change.title}</h2>
          <div className="text-sm text-gray-600">{change.regulatoryBody} • {change.category} • {change.effectiveDate}</div>
        </div>
        <div className="space-x-2">
          <button onClick={generateAiInsights} className="px-3 py-1 bg-green-600 text-white rounded hover:bg-green-700">AI Insights</button>
          <Link to={`/changes/${id}/edit`} className="px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700">Edit</Link>
          <button onClick={handleDelete} className="px-3 py-1 bg-red-500 text-white rounded hover:bg-red-600">Delete</button>
        </div>
      </div>

      <div className="mt-6 grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <h3 className="text-lg font-medium border-b pb-2 mb-3">Description</h3>
          <div className="text-gray-800 whitespace-pre-wrap">{change.description}</div>
        </div>

        {change.aiDescription && (
          <div className="bg-blue-50 p-4 rounded border border-blue-200">
            <h3 className="text-lg font-medium text-blue-800 mb-2 flex items-center">
              <span className="mr-2">✨</span> AI Summary
            </h3>
            <div className="text-blue-900 text-sm italic">{change.aiDescription}</div>
          </div>
        )}
      </div>

      {change.aiRecommendations && (
        <div className="mt-6 bg-purple-50 p-4 rounded border border-purple-200">
          <h3 className="text-lg font-medium text-purple-800 mb-2 flex items-center">
            <span className="mr-2">💡</span> AI Recommendations
          </h3>
          <div className="text-purple-900 text-sm whitespace-pre-wrap">{change.aiRecommendations}</div>
        </div>
      )}

      <div className="mt-6 flex gap-6 text-sm text-gray-600 border-t pt-4">
        <div>Status: <span className="font-semibold text-gray-900">{change.status}</span></div>
        <div>Priority: <span className="font-semibold text-gray-900">{change.priority}</span></div>
        <div>Deadline: <span className="font-semibold text-gray-900">{change.deadline}</span></div>
      </div>
    </div>
  );
}
