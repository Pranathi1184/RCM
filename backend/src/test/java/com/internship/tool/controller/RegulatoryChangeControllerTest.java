package com.internship.tool.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.tool.entity.RegulatoryChange;
import com.internship.tool.service.RegulatoryChangeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.flyway.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class RegulatoryChangeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegulatoryChangeService service;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateChange_Success() throws Exception {
        RegulatoryChange mockChange = new RegulatoryChange();
        mockChange.setId(1L);
        mockChange.setTitle("New Rule");
        mockChange.setCategory("Privacy");
        mockChange.setRegulatoryBody("FCC");

        when(service.createChange(any(RegulatoryChange.class))).thenReturn(mockChange);

        mockMvc.perform(post("/api/changes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockChange)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Rule"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testUpdateChange_Success() throws Exception {
        RegulatoryChange mockChange = new RegulatoryChange();
        mockChange.setId(1L);
        mockChange.setTitle("Updated Rule");
        mockChange.setCategory("Privacy");
        mockChange.setRegulatoryBody("FCC");

        when(service.updateChange(eq(1L), any(RegulatoryChange.class))).thenReturn(mockChange);

        mockMvc.perform(put("/api/changes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockChange)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Rule"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSoftDelete_Success() throws Exception {
        doNothing().when(service).softDelete(1L);

        mockMvc.perform(delete("/api/changes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void testListChanges_Success() throws Exception {
        Page<RegulatoryChange> page = new PageImpl<>(Collections.emptyList());
        when(service.searchChanges(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/changes")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "id")
                .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void testSearchChanges_Success() throws Exception {
        Page<RegulatoryChange> page = new PageImpl<>(Collections.emptyList());
        when(service.searchChanges(eq("test"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/changes/search")
                .param("q", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testGetDashboardStats_Success() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalActive", 100);

        when(service.getDashboardStats()).thenReturn(stats);

        mockMvc.perform(get("/api/changes/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalActive").value(100));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testExportCsv_Success() throws Exception {
        byte[] csvBytes = "ID,Title\n1,Test".getBytes();
        when(service.exportToCsv()).thenReturn(csvBytes);

        mockMvc.perform(get("/api/changes/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"regulatory_changes.csv\""))
                .andExpect(content().contentType(MediaType.parseMediaType("text/csv")));
    }
}
