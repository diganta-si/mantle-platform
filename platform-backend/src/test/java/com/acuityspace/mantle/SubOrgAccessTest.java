package com.acuityspace.mantle;

import com.acuityspace.mantle.config.TestContainersBase;
import com.acuityspace.mantle.domain.enums.OrgType;
import com.acuityspace.mantle.domain.enums.UserRole;
import com.acuityspace.mantle.domain.model.Org;
import com.acuityspace.mantle.domain.model.OrgMembership;
import com.acuityspace.mantle.domain.model.SubOrg;
import com.acuityspace.mantle.domain.model.SubOrgMembership;
import com.acuityspace.mantle.domain.model.User;
import com.acuityspace.mantle.domain.repository.OrgMembershipRepository;
import com.acuityspace.mantle.domain.repository.OrgRepository;
import com.acuityspace.mantle.domain.repository.SubOrgMembershipRepository;
import com.acuityspace.mantle.domain.repository.SubOrgRepository;
import com.acuityspace.mantle.domain.repository.UserRepository;
import com.acuityspace.mantle.exception.AccessDeniedException;
import com.acuityspace.mantle.exception.DuplicateNameException;
import com.acuityspace.mantle.exception.IllegalOperationException;
import com.acuityspace.mantle.service.AuthService;
import com.acuityspace.mantle.service.SubOrgService;
import com.acuityspace.mantle.web.dto.AuthResponse;
import com.acuityspace.mantle.web.dto.RegisterRequest;
import com.acuityspace.mantle.web.dto.SubOrgRequest;
import com.acuityspace.mantle.web.dto.SubOrgResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class SubOrgAccessTest extends TestContainersBase {

    @Autowired
    private AuthService authService;

    @Autowired
    private SubOrgService subOrgService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrgRepository orgRepository;

    @Autowired
    private SubOrgRepository subOrgRepository;

    @Autowired
    private OrgMembershipRepository orgMembershipRepository;

    @Autowired
    private SubOrgMembershipRepository subOrgMembershipRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void individual_cannotCreateSubOrg_throwsIllegalOperationException() {
        AuthResponse owner = authService.register(new RegisterRequest(
                "ind-owner@access.com", "password123", "IndOwner", "IndOrg", OrgType.INDIVIDUAL));

        assertThatThrownBy(() -> subOrgService.createSubOrg(
                owner.orgId(), new SubOrgRequest("NewTeam"), owner.userId()))
                .isInstanceOf(IllegalOperationException.class);
    }

    @Test
    void enterprise_superAdmin_canCreateSubOrg() {
        AuthResponse owner = authService.register(new RegisterRequest(
                "ent-owner@access.com", "password123", "EntOwner", "EntOrg", OrgType.ENTERPRISE));

        SubOrgResponse created = subOrgService.createSubOrg(
                owner.orgId(), new SubOrgRequest("Engineering"), owner.userId());

        assertThat(created.name()).isEqualTo("Engineering");
        assertThat(created.isDefault()).isFalse();
        assertThat(created.isGlobal()).isFalse();
        assertThat(created.orgId()).isEqualTo(owner.orgId());
    }

    @Test
    void enterprise_standard_cannotCreateSubOrg() {
        AuthResponse owner = authService.register(new RegisterRequest(
                "ent-owner2@access.com", "password123", "EntOwner2", "EntOrg2", OrgType.ENTERPRISE));

        Org org = orgRepository.findById(owner.orgId()).orElseThrow();
        SubOrg defaultSubOrg = subOrgRepository.findByOrgAndIsDefaultTrue(org).orElseThrow();

        User standardUser = userRepository.save(
                new User("standard@access.com", passwordEncoder.encode("password123"), "Standard User"));
        orgMembershipRepository.save(new OrgMembership(standardUser, org));
        subOrgMembershipRepository.save(new SubOrgMembership(standardUser, defaultSubOrg, UserRole.STANDARD));

        assertThatThrownBy(() -> subOrgService.createSubOrg(
                owner.orgId(), new SubOrgRequest("Team"), standardUser.getId()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deleteSubOrg_default_throwsIllegalOperationException() {
        AuthResponse owner = authService.register(new RegisterRequest(
                "ent-owner3@access.com", "password123", "EntOwner3", "EntOrg3", OrgType.ENTERPRISE));

        Org org = orgRepository.findById(owner.orgId()).orElseThrow();
        SubOrg defaultSubOrg = subOrgRepository.findByOrgAndIsDefaultTrue(org).orElseThrow();

        assertThatThrownBy(() -> subOrgService.deleteSubOrg(defaultSubOrg.getId(), owner.userId()))
                .isInstanceOf(IllegalOperationException.class);
    }

    @Test
    void suborg_nameUnique_withinOrg() {
        AuthResponse owner = authService.register(new RegisterRequest(
                "ent-owner4@access.com", "password123", "EntOwner4", "EntOrg4", OrgType.ENTERPRISE));

        subOrgService.createSubOrg(owner.orgId(), new SubOrgRequest("Engineering"), owner.userId());

        assertThatThrownBy(() -> subOrgService.createSubOrg(
                owner.orgId(), new SubOrgRequest("Engineering"), owner.userId()))
                .isInstanceOf(DuplicateNameException.class);
    }
}
