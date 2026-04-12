package com.acuityspace.mantle;

import com.acuityspace.mantle.config.TestContainersBase;
import com.acuityspace.mantle.domain.enums.OrgType;
import com.acuityspace.mantle.domain.enums.UserRole;
import com.acuityspace.mantle.domain.model.SubOrg;
import com.acuityspace.mantle.domain.model.SubOrgMembership;
import com.acuityspace.mantle.domain.model.User;
import com.acuityspace.mantle.domain.repository.OrgRepository;
import com.acuityspace.mantle.domain.repository.SubOrgMembershipRepository;
import com.acuityspace.mantle.domain.repository.SubOrgRepository;
import com.acuityspace.mantle.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class InviteFlowTest extends TestContainersBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrgRepository orgRepository;

    @Autowired
    private SubOrgRepository subOrgRepository;

    @Autowired
    private SubOrgMembershipRepository subOrgMembershipRepository;

    private UUID registerAndGetOrgId(String email, String password, OrgType orgType) throws Exception {
        Map<String, Object> req = Map.of(
                "email", email,
                "password", password,
                "name", "Test User",
                "orgName", "TestOrg",
                "orgType", orgType.name());

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        String orgId = objectMapper.readTree(body).get("orgId").asText();
        return UUID.fromString(orgId);
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        Map<String, String> req = Map.of("email", email, "password", password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        String setCookieHeader = result.getResponse().getHeader("Set-Cookie");
        assertThat(setCookieHeader).isNotNull();
        return setCookieHeader.split(";")[0].split("=", 2)[1];
    }

    private UUID sendInviteAndGetId(UUID orgId, String invitedEmail, String token) throws Exception {
        Map<String, String> req = Map.of("email", invitedEmail);

        MvcResult result = mockMvc.perform(post("/api/orgs/{orgId}/invites", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .cookie(new Cookie("mantle-token", token)))
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        String inviteId = objectMapper.readTree(body).get("id").asText();
        return UUID.fromString(inviteId);
    }

    @Test
    void sendInvite_thenAccept_userCanLogin() throws Exception {
        String ownerEmail = "flow-owner@invite.com";
        String ownerPassword = "password123";
        String invitedEmail = "flow-invited@invite.com";
        String invitedPassword = "newpassword123";

        UUID orgId = registerAndGetOrgId(ownerEmail, ownerPassword, OrgType.INDIVIDUAL);
        String ownerToken = loginAndGetToken(ownerEmail, ownerPassword);
        UUID inviteId = sendInviteAndGetId(orgId, invitedEmail, ownerToken);

        // Accept invite (public endpoint)
        Map<String, Object> acceptReq = Map.of(
                "email", invitedEmail,
                "password", invitedPassword,
                "name", "Invited User",
                "orgName", "ignored",
                "orgType", "INDIVIDUAL");

        mockMvc.perform(post("/api/orgs/{orgId}/invites/{inviteId}/accept", orgId, inviteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acceptReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(invitedEmail));

        // Invited user can login
        String invitedToken = loginAndGetToken(invitedEmail, invitedPassword);
        assertThat(invitedToken).isNotBlank();

        // Verify STANDARD role on default SubOrg
        User invitedUser = userRepository.findByEmail(invitedEmail).orElseThrow();
        SubOrg defaultSubOrg = subOrgRepository.findByOrgAndIsDefaultTrue(
                orgRepository.findById(orgId).orElseThrow()).orElseThrow();

        SubOrgMembership membership = subOrgMembershipRepository
                .findByUserAndSubOrg(invitedUser, defaultSubOrg).orElseThrow();
        assertThat(membership.getRole()).isEqualTo(UserRole.STANDARD);
    }

    @Test
    void revokeInvite_thenAccept_throwsInvalidInviteException() throws Exception {
        String ownerEmail = "revoke-owner@invite.com";
        String ownerPassword = "password123";
        String invitedEmail = "revoke-invited@invite.com";

        UUID orgId = registerAndGetOrgId(ownerEmail, ownerPassword, OrgType.INDIVIDUAL);
        String ownerToken = loginAndGetToken(ownerEmail, ownerPassword);
        UUID inviteId = sendInviteAndGetId(orgId, invitedEmail, ownerToken);

        // Revoke invite
        mockMvc.perform(delete("/api/orgs/{orgId}/invites/{inviteId}/revoke", orgId, inviteId)
                        .cookie(new Cookie("mantle-token", ownerToken)))
                .andExpect(status().isNoContent());

        // Accept revoked invite → 400
        Map<String, Object> acceptReq = Map.of(
                "email", invitedEmail,
                "password", "password123",
                "name", "Revoked User",
                "orgName", "ignored",
                "orgType", "INDIVIDUAL");

        mockMvc.perform(post("/api/orgs/{orgId}/invites/{inviteId}/accept", orgId, inviteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(acceptReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_INVITE"));
    }

    @Test
    void duplicateInvite_samePendingEmail_throwsException() throws Exception {
        String ownerEmail = "dup-owner@invite.com";
        String ownerPassword = "password123";
        String invitedEmail = "dup-invited@invite.com";

        UUID orgId = registerAndGetOrgId(ownerEmail, ownerPassword, OrgType.ENTERPRISE);
        String ownerToken = loginAndGetToken(ownerEmail, ownerPassword);

        // First invite succeeds
        sendInviteAndGetId(orgId, invitedEmail, ownerToken);

        // Second invite to same email fails
        Map<String, String> req = Map.of("email", invitedEmail);
        mockMvc.perform(post("/api/orgs/{orgId}/invites", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .cookie(new Cookie("mantle-token", ownerToken)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("ILLEGAL_OPERATION"));
    }
}
