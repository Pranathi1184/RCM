import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../services/api";

export default function Form() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState({ title: "", description: "", source: "", status: "PENDING", effectiveDate: "" });
  const [errors, setErrors] = useState({ effectiveDate: "", title: "" });

  useEffect(() => {
    if (id) api.get(`/changes/${id}`).then(({ data }) => setForm(data));
  }, [id]);

  const validateDate = (value) => {
    if (!value) return "Effective date is required";
    const iso = /^\d{4}-\d{2}-\d{2}$/;
    if (!iso.test(value)) return "Date must be in YYYY-MM-DD format";
    const t = new Date(value).getTime();
    if (isNaN(t)) return "Invalid date";
    return "";
  };

  const validateTitle = (value) => {
    if (!value || value.trim().length === 0) return "Title is required";
    if (value.length > 255) return "Title must be 255 characters or less";
    return "";
  };

  const handleDateChange = (value) => {
    const err = validateDate(value);
    setErrors({ ...errors, effectiveDate: err });
    setForm({ ...form, effectiveDate: value });
  };

  const handleTitleChange = (value) => {
    const err = validateTitle(value);
    setErrors({ ...errors, title: err });
    setForm({ ...form, title: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const dateError = validateDate(form.effectiveDate);
    if (dateError) {
      setErrors({ ...errors, effectiveDate: dateError });
      return;
    }

    try {
      const payload = { ...form, effectiveDate: form.effectiveDate ? form.effectiveDate : null };
      if (id) await api.put(`/changes/${id}`, payload);
      else await api.post("/changes", payload);
      navigate("/changes");
    } catch (err) {
      alert("Error saving: " + (err.response?.data?.error || err.message));
    }
  };

  return (
    <div className="max-w-2xl mx-auto bg-white shadow rounded p-6">
      <h2 className="text-2xl font-semibold mb-4">{id ? "Edit" : "New"} Regulatory Change</h2>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Title <span className="text-red-600">*</span></label>
          <input className="w-full px-3 py-2 border rounded shadow-sm" placeholder="Title" value={form.title} onChange={e => handleTitleChange(e.target.value)} />
          {errors.title && <p className="text-sm text-red-600 mt-1">{errors.title}</p>}
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
          <textarea className="w-full px-3 py-2 border rounded shadow-sm" rows={5} placeholder="Description" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} />
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Source</label>
            <input className="w-full px-3 py-2 border rounded" placeholder="Source" value={form.source} onChange={e => setForm({ ...form, source: e.target.value })} />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Status</label>
            <select className="w-full px-3 py-2 border rounded" value={form.status} onChange={e => setForm({ ...form, status: e.target.value })}>
              <option>PENDING</option>
              <option>IN_REVIEW</option>
              <option>APPROVED</option>
              <option>REJECTED</option>
            </select>
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Effective Date <span className="text-red-600">*</span></label>
          <input className="px-3 py-2 border rounded" type="date" value={form.effectiveDate || ""} onChange={e => handleDateChange(e.target.value)} />
          {errors.effectiveDate && <p className="text-sm text-red-600 mt-1">{errors.effectiveDate}</p>}
        </div>

        <div className="flex items-center space-x-3 pt-2">
          <button type="submit" disabled={!!errors.effectiveDate || !form.effectiveDate || !!errors.title || !form.title} className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50" >Save</button>
          <button type="button" onClick={() => navigate(-1)} className="px-4 py-2 border rounded">Cancel</button>
        </div>
      </form>
    </div>
  );
}
