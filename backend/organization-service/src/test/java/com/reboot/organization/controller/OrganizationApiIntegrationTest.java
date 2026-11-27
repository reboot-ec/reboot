package com.reboot.organization.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:organization-integration;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.flyway.enabled=false",
        "jwt.secret=test-only-signing-key-not-for-production-2026"
})
@AutoConfigureMockMvc
class OrganizationApiIntegrationTest {

    private static final String TEST_SECRET = "test-only-signing-key-not-for-production-2026";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void completesOrganizationAndMembershipFlowAndRejectsUnauthorizedRequests() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID adminMemberId = UUID.randomUUID();
        String ownerToken = token(ownerId);
        String memberToken = token(memberId);

        String organizationJson = objectMapper.writeValueAsString(Map.of(
                "name", "Integration Repair Group",
                "organizationType", "NGO"));
        MvcResult createResult = mockMvc.perform(post("/api/v1/organizations")
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(organizationJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.createdByUserId").value(ownerId.toString()))
                .andReturn();
        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String organizationId = created.path("data").path("id").asText();

        mockMvc.perform(get("/api/v1/organizations/{organizationId}", organizationId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(organizationId))
                .andExpect(jsonPath("$.meta.requestId").isNotEmpty());

        mockMvc.perform(patch("/api/v1/organizations/{organizationId}", organizationId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("description", "Updated by owner"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").value("Updated by owner"));

        String memberIdFromResponse = invite(organizationId, ownerToken, memberId);
        mockMvc.perform(patch("/api/v1/organizations/{organizationId}/members/{memberId}",
                                organizationId, memberIdFromResponse)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "ACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        String adminMemberIdFromResponse = invite(organizationId, ownerToken, adminMemberId);
        mockMvc.perform(patch("/api/v1/organizations/{organizationId}/members/{memberId}",
                                organizationId, adminMemberIdFromResponse)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "role", "ADMIN",
                                "status", "ACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mockMvc.perform(post("/api/v1/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(organizationJson))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/v1/organizations/{organizationId}", organizationId)
                        .header("Authorization", bearer(memberToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("description", "Forbidden update"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/organizations/{organizationId}/members", organizationId)
                        .header("Authorization", bearer(memberToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "userId", UUID.randomUUID().toString(),
                                "role", "MEMBER"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/members", organizationId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"role\":\"OWNER\"")))
                .andExpect(content().string(containsString("\"status\":\"ACTIVE\"")));
    }

    private String invite(String organizationId, String ownerToken, UUID userId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/organizations/{organizationId}/members", organizationId)
                        .header("Authorization", bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "userId", userId.toString(),
                                "role", "MEMBER"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("id").asText();
    }

    private String token(UUID userId) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", userId + "@example.test")
                .claim("roles", List.of())
                .signWith(key)
                .compact();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
