-- Add missing performance indexes identified during Day 13 review

-- Index the title column to speed up search queries and ordering
CREATE INDEX idx_regulatory_change_title ON regulatory_change (title);

-- Create an index for the assigned_to column as it's often filtered in dashboards
CREATE INDEX idx_regulatory_change_assigned_to ON regulatory_change (assigned_to);


