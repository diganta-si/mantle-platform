package com.acuityspace.mantle;

import com.acuityspace.mantle.config.TestContainersBase;
import com.acuityspace.mantle.domain.enums.OrgType;
import com.acuityspace.mantle.domain.enums.UserRole;
import com.acuityspace.mantle.domain.model.SubOrg;
import com.acuityspace.mantle.domain.repository.OrgMembershipRepository;
import com.acuityspace.mantle.domain.repository.OrgRepository;
import com.acuityspace.mantle.domain.repository.SubOrgMembershipRepository;
import com.acuityspace.mantle.domain.repository.SubOrgRepository;
import com.acuityspace.mantle.domain.repository.UserRepository;
import com.acuityspace.mantle.exception.EmailAlreadyExistsException;
import com.acuityspace.mantle.service.AuthService;
import com.acuityspace.mantle.web.dto.AuthResponse;
import com.acuityspace.mantle.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@Transactional
class AuthServiceRegistrationTest extends TestContainersBase {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrgRepository orgRepository;

    @Autowired
    private OrgMembershipRepository orgMembershipRepository;

    // @SpyBean replaces the SubOrgRepository bean in the context with a Mockito spy.
    // The spy delegates to the real implementation by default, so all normal read/write
    // operations work correctly. We only stub it for the rollback test.
    @SpyBean
    private SubOrgRepository subOrgRepository;

    @Autowired
    private SubOrgMembershipRepository subOrgMembershipRepository;

    @Test
    void registerIndividual_createsUserOrgSubOrgAndMembership() {
        RegisterRequest req = new RegisterRequest(
                "alice@example.com", "password123", "Alice", "Alice Corp", OrgType.INDIVIDUAL);

        AuthResponse response = authService.register(req);

        // User persisted with encoded password
        var user = userRepository.findByEmail("alice@example.com").orElseThrow();
        assertThat(user.getPasswordHash()).isNotEqualTo("password123");
        assertThat(user.getPasswordHash()).startsWith("$2a$");

        // Org persisted with correct type
        var org = orgRepository.findById(response.orgId()).orElseThrow();
        assertThat(org.getType()).isEqualTo(OrgType.INDIVIDUAL);
        assertThat(org.getName()).isEqualTo("Alice Corp");

        // OrgMembership persisted
        var membership = orgMembershipRepository.findByUser(user);
        assertThat(membership).isPresent();
        assertThat(membership.get().getId().getOrgId()).isEqualTo(org.getId());

        // One SubOrg with isDefault=true
        List<SubOrg> subOrgs = subOrgRepository.findByOrg(org);
        assertThat(subOrgs).hasSize(1);
        assertThat(subOrgs.get(0).isDefault()).isTrue();
        assertThat(subOrgs.get(0).isGlobal()).isFalse();

        // SubOrgMembership with SUPER_ADMIN
        var subOrgMembership = subOrgMembershipRepository.findByUserAndSubOrg(user, subOrgs.get(0));
        assertThat(subOrgMembership).isPresent();
        assertThat(subOrgMembership.get().getRole()).isEqualTo(UserRole.SUPER_ADMIN);

        // AuthResponse fully populated
        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.email()).isEqualTo("alice@example.com");
        assertThat(response.name()).isEqualTo("Alice");
        assertThat(response.orgId()).isEqualTo(org.getId());
        assertThat(response.orgName()).isEqualTo("Alice Corp");
        assertThat(response.orgType()).isEqualTo(OrgType.INDIVIDUAL);
        assertThat(response.subOrgId()).isEqualTo(subOrgs.get(0).getId());
        assertThat(response.role()).isEqualTo(UserRole.SUPER_ADMIN);
    }

    @Test
    void registerEnterprise_createsTwoSubOrgs() {
        RegisterRequest req = new RegisterRequest(
                "bob@example.com", "password123", "Bob", "Bob Corp", OrgType.ENTERPRISE);

        AuthResponse response = authService.register(req);

        var user = userRepository.findByEmail("bob@example.com").orElseThrow();
        var org = orgRepository.findById(response.orgId()).orElseThrow();

        List<SubOrg> subOrgs = subOrgRepository.findByOrg(org);
        assertThat(subOrgs).hasSize(2);

        SubOrg defaultSubOrg = subOrgs.stream().filter(SubOrg::isDefault).findFirst().orElseThrow();
        SubOrg globalSubOrg  = subOrgs.stream().filter(SubOrg::isGlobal).findFirst().orElseThrow();

        assertThat(defaultSubOrg.isDefault()).isTrue();
        assertThat(globalSubOrg.isGlobal()).isTrue();

        // Both SubOrgMemberships with SUPER_ADMIN
        var defaultMembership = subOrgMembershipRepository.findByUserAndSubOrg(user, defaultSubOrg);
        var globalMembership  = subOrgMembershipRepository.findByUserAndSubOrg(user, globalSubOrg);
        assertThat(defaultMembership).isPresent();
        assertThat(globalMembership).isPresent();
        assertThat(defaultMembership.get().getRole()).isEqualTo(UserRole.SUPER_ADMIN);
        assertThat(globalMembership.get().getRole()).isEqualTo(UserRole.SUPER_ADMIN);

        // AuthResponse subOrgId is the default SubOrg
        assertThat(response.subOrgId()).isEqualTo(defaultSubOrg.getId());
    }

    @Test
    void register_duplicateEmail_throwsEmailAlreadyExistsException() {
        RegisterRequest req1 = new RegisterRequest(
                "dup@example.com", "password123", "User1", "Org1", OrgType.INDIVIDUAL);
        RegisterRequest req2 = new RegisterRequest(
                "dup@example.com", "password456", "User2", "Org2", OrgType.INDIVIDUAL);

        authService.register(req1);

        assertThatThrownBy(() -> authService.register(req2))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void register_isTransactional_rollsBackOnFailure() {
        // Force a failure mid-way through register() by throwing when SubOrg.save() is called.
        // The service's @Transactional ensures the user (already saved) is rolled back too.
        doThrow(new RuntimeException("forced failure")).when(subOrgRepository).save(any());

        RegisterRequest req = new RegisterRequest(
                "rollback@example.com", "password123", "Rollback User", "RollbackOrg", OrgType.INDIVIDUAL);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(RuntimeException.class);

        // Restore normal behaviour before asserting (avoids spy side-effects on later tests)
        Mockito.reset(subOrgRepository);

        assertThat(userRepository.existsByEmail("rollback@example.com")).isFalse();
    }
}
