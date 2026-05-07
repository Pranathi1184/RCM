package com.internship.tool.controller;

import com.internship.tool.entity.RegulatoryChange;
import com.internship.tool.service.RegulatoryChangeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/changes")
public class RegulatoryChangeController {

    private final RegulatoryChangeService service;

    public RegulatoryChangeController(RegulatoryChangeService service) {
        this.service = service;
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<RegulatoryChange> createChange(@Valid @RequestBody RegulatoryChange newChange) {
        return ResponseEntity.ok(service.createChange(newChange));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<RegulatoryChange> updateChange(
            @PathVariable Long id, 
            @Valid @RequestBody RegulatoryChange updateData) {
        return ResponseEntity.ok(service.updateChange(id, updateData));
    }

    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping("/{id}/ai-insights")
    public ResponseEntity<RegulatoryChange> generateAiInsights(@PathVariable Long id) {
        return ResponseEntity.ok(service.generateAiInsights(id));
    }

    @PreAuthorize("hasAnyRole('VIEWER', 'MANAGER', 'ADMIN')")
    @GetMapping("/query")
    public ResponseEntity<Map<String, Object>> queryRag(@RequestParam String q) {
        return ResponseEntity.ok(service.queryRag(q));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteChange(@PathVariable Long id) {
        service.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('VIEWER', 'MANAGER', 'ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<Page<RegulatoryChange>> searchChanges(
            @RequestParam(name = "q", required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(service.searchChanges(keyword, pageable));
    }

    @PreAuthorize("hasAnyRole('VIEWER', 'MANAGER', 'ADMIN')")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(service.getDashboardStats());
    }
    @PreAuthorize("hasAnyRole('VIEWER', 'MANAGER', 'ADMIN')")
    @GetMapping
    public ResponseEntity<Page<RegulatoryChange>> listChanges(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? 
                Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // Pass null keyword to fetch all active records with pagination
        return ResponseEntity.ok(service.searchChanges(null, pageable));
    }

    @PreAuthorize("hasAnyRole('VIEWER', 'MANAGER', 'ADMIN')")
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv() {
        byte[] csvData = service.exportToCsv();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"regulatory_changes.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
