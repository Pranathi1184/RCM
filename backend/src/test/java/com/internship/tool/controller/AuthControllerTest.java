package com.internship.tool.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.tool.config.CustomUserDetailsService;
import com.internship.tool.config.JwtUtil;
import com.internship.tool.dto.AuthRequest;
import com.internship.tool.dto.RegisterRequest;
import com.internship.tool.entity.Role;
import com.internship.tool.entity.User;
import com.internship.tool.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    void testLogin_Success() throws Exception {
        AuthRequest request = new AuthRequest("admin@example.com", "admin123");
        User mockUser = User.builder().email("admin@example.com").role(Role.ADMIN).build();
        
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateToken(any())).thenReturn("mock-jwt-token");
        
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void testRegister_Success() throws Exception {
        RegisterRequest request = new RegisterRequest("new@example.com", "pass123", "VIEWER");
        User mockUser = User.builder().email("new@example.com").role(Role.VIEWER).build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed-pass");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtUtil.generateToken(any())).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.role").value("VIEWER"));
    }

    @Test
    void testRefresh_Success() throws Exception {
        User mockUser = User.builder().email("admin@example.com").role(Role.ADMIN).build();
        
        when(jwtUtil.extractUsername(anyString())).thenReturn("admin@example.com");
        when(jwtUtil.isTokenValid(anyString(), any())).thenReturn(true);
        when(jwtUtil.generateToken(any())).thenReturn("new-mock-jwt-token");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(mockUser));
        when(userDetailsService.loadUserByUsername("admin@example.com")).thenReturn(
            new org.springframework.security.core.userdetails.User(
                "admin@example.com", 
                "pass", 
                Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))
            )
        );

        mockMvc.perform(post("/auth/refresh")
                .header("Authorization", "Bearer old-mock-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-mock-jwt-token"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}
