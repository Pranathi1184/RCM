package com.internship.tool.controller;

import com.internship.tool.dto.AuthRequest;
import com.internship.tool.entity.ChangeStatus;
import com.internship.tool.entity.Priority;
import com.internship.tool.entity.RegulatoryChange;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.DockerClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RegulatoryChangeIntegrationTest {

    @BeforeAll
    static void checkDocker() {
        Assumptions.assumeTrue(
            DockerClientFactory.instance().isDockerAvailable(),
            "Docker is not available — skipping Testcontainers integration tests"
        );
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379).toString());
    }

    @Autowired
    private TestRestTemplate restTemplate;

    private static String jwtToken;
    private static Long createdId;

    @Test
    @Order(1)
    void loginWithSeededAdmin() {
        AuthRequest loginRequest = new AuthRequest("admin@example.com", "admin123");
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity("/auth/login", loginRequest, Map.class);
        
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).containsKey("token");
        
        jwtToken = (String) loginResponse.getBody().get("token");
    }

    @Test
    @Order(2)
    void createRegulatoryChange() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        RegulatoryChange change = new RegulatoryChange();
        change.setTitle("Testcontainers Rule");
        change.setRegulatoryBody("Test Authority");
        change.setCategory("Test Category");
        change.setStatus(ChangeStatus.DRAFT);
        change.setPriority(Priority.P1);
        change.setImpactScore(new java.math.BigDecimal("5.5"));

        HttpEntity<RegulatoryChange> request = new HttpEntity<>(change, headers);

        ResponseEntity<RegulatoryChange> response = restTemplate.postForEntity("/api/changes", request, RegulatoryChange.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Testcontainers Rule");

        createdId = response.getBody().getId();
    }

    @Test
    @Order(3)
    void listRegulatoryChanges() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/api/changes?page=0&size=10",
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("content");
    }

    @Test
    @Order(4)
    void updateRegulatoryChange() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        RegulatoryChange update = new RegulatoryChange();
        update.setTitle("Updated Test Rule");
        update.setRegulatoryBody("Test Authority");
        update.setCategory("Test Category");

        HttpEntity<RegulatoryChange> request = new HttpEntity<>(update, headers);

        ResponseEntity<RegulatoryChange> response = restTemplate.exchange(
                "/api/changes/" + createdId,
                HttpMethod.PUT,
                request,
                RegulatoryChange.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getTitle()).isEqualTo("Updated Test Rule");
    }

    @Test
    @Order(5)
    void softDeleteRegulatoryChange() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/changes/" + createdId,
                HttpMethod.DELETE,
                request,
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
