package com.internship.tool.service;

import com.internship.tool.entity.RegulatoryChange;
import com.internship.tool.repository.RegulatoryChangeRepository;
import com.internship.tool.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

@Service
@Transactional
public class RegulatoryChangeService {

    private final RegulatoryChangeRepository repository;
    private final AiServiceClient aiServiceClient;

    public RegulatoryChangeService(RegulatoryChangeRepository repository, AiServiceClient aiServiceClient) {
        this.repository = repository;
        this.aiServiceClient = aiServiceClient;
    }

    public RegulatoryChange getChangeById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public RegulatoryChange generateAiInsights(Long id) {
        RegulatoryChange change = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Regulatory Change not found with id: " + id));

        String content = change.getTitle() + " " + change.getDescription();
        
        AiServiceClient.AiResponse descRes = aiServiceClient.describe(content);
        if (descRes != null && descRes.getData() != null && descRes.getData().get("data") instanceof Map dataMap) {
            change.setAiDescription((String) dataMap.get("description"));
        }

        AiServiceClient.AiResponse recRes = aiServiceClient.recommend(content);
        if (recRes != null && recRes.getData() != null && recRes.getData().get("data") instanceof List recommendations) {
            // Convert list to a formatted string
            StringBuilder sb = new StringBuilder();
            for (Object rec : recommendations) {
                if (rec instanceof String) {
                    sb.append("- ").append(rec).append("\n");
                } else if (rec instanceof Map recMap) {
                    sb.append("- ").append(recMap.get("description")).append(" (").append(recMap.get("priority")).append(")\n");
                }
            }
            change.setAiRecommendations(sb.toString().trim());
        }

        return repository.save(change);
    }

    public Map<String, Object> queryRag(String question) {
        AiServiceClient.AiResponse response = aiServiceClient.query(question);
        if (response != null && response.getData() != null && response.getData().get("data") instanceof Map dataMap) {
            return (Map<String, Object>) dataMap;
        }
        return new HashMap<>();
    }

    public RegulatoryChange createChange(RegulatoryChange newChange) {
        newChange.setIsDeleted(false);
        return repository.save(newChange);
    }

    public RegulatoryChange updateChange(Long id, RegulatoryChange updateData) {
        RegulatoryChange existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Regulatory Change not found with id: " + id));

        // use an ObjectMapper or map specific fields explicitly
        if (updateData.getTitle() != null)
            existing.setTitle(updateData.getTitle());
        if (updateData.getDescription() != null)
            existing.setDescription(updateData.getDescription());
        if (updateData.getCategory() != null)
            existing.setCategory(updateData.getCategory());
        if (updateData.getRegulatoryBody() != null)
            existing.setRegulatoryBody(updateData.getRegulatoryBody());
        if (updateData.getStatus() != null)
            existing.setStatus(updateData.getStatus());
        if (updateData.getPriority() != null)
            existing.setPriority(updateData.getPriority());
        if (updateData.getImpactScore() != null)
            existing.setImpactScore(updateData.getImpactScore());
        if (updateData.getEffectiveDate() != null)
            existing.setEffectiveDate(updateData.getEffectiveDate());
        if (updateData.getDeadline() != null)
            existing.setDeadline(updateData.getDeadline());
        if (updateData.getAssignedTo() != null)
            existing.setAssignedTo(updateData.getAssignedTo());
        
        if (updateData.getAiDescription() != null)
            existing.setAiDescription(updateData.getAiDescription());
        if (updateData.getAiRecommendations() != null)
            existing.setAiRecommendations(updateData.getAiRecommendations());
        return repository.save(existing);
    }

    public void softDelete(Long id) {
        RegulatoryChange existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Regulatory Change not found with id: " + id));
        existing.setIsDeleted(true);
        repository.save(existing);
    }

    @Transactional(readOnly = true)
    public Page<RegulatoryChange> searchChanges(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return repository.findAllByIsDeletedFalse(pageable);
        }
        return repository.searchByKeyword(keyword, pageable);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalActive = repository.countByIsDeletedFalse();
        stats.put("totalActive", totalActive);

        // Convert List<Object[]> to Map<String, Long> for Status
        Map<String, Long> byStatus = new HashMap<>();
        List<Object[]> statusCounts = repository.countByStatus();
        for (Object[] row : statusCounts) {
            byStatus.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("byStatus", byStatus);

        // Convert List<Object[]> to Map<String, Long> for Priority
        Map<String, Long> byPriority = new HashMap<>();
        List<Object[]> priorityCounts = repository.countByPriority();
        for (Object[] row : priorityCounts) {
            byPriority.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("byPriority", byPriority);

        return stats;
    }

    @Transactional(readOnly = true)
    public byte[] exportToCsv() {
        // Use JOIN FETCH or a custom query if needed to avoid N+1 for any relationships
        // but here RegulatoryChange has only simple fields and Enums
        Page<RegulatoryChange> allActive = repository.findAllByIsDeletedFalse(Pageable.unpaged());
        List<RegulatoryChange> changes = allActive.getContent();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), CSVFormat.DEFAULT.builder()
                     .setHeader("ID", "Title", "Category", "Regulatory Body", "Status", "Priority", "Impact Score", "Effective Date", "Deadline")
                     .build())) {

            for (RegulatoryChange change : changes) {
                csvPrinter.printRecord(
                        change.getId(),
                        change.getTitle(),
                        change.getCategory(),
                        change.getRegulatoryBody(),
                        change.getStatus() != null ? change.getStatus().name() : "",
                        change.getPriority() != null ? change.getPriority().name() : "",
                        change.getImpactScore(),
                        change.getEffectiveDate(),
                        change.getDeadline()
                );
            }
            csvPrinter.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to export CSV data: " + e.getMessage());
        }
    }
}
