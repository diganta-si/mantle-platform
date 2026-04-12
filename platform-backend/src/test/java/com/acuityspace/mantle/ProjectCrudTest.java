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
import com.acuityspace.mantle.service.AppService;
import com.acuityspace.mantle.service.AuthService;
import com.acuityspace.mantle.service.ProjectService;
import com.acuityspace.mantle.web.dto.AppRequest;
import com.acuityspace.mantle.web.dto.AuthResponse;
import com.acuityspace.mantle.web.dto.ProjectRequest;
import com.acuityspace.mantle.web.dto.ProjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class ProjectCrudTest extends TestContainersBase {

    @Autowired private AuthService authService;
    @Autowired private ProjectService projectService;
    @Autowired private AppService appService;
    @Autowired private UserRepository userRepository;
    @Autowired private OrgRepository orgRepository;
    @Autowired private SubOrgRepository subOrgRepository;
    @Autowired private OrgMembershipRepository orgMembershipRepository;
    @Autowired private SubOrgMembershipRepository subOrgMembershipRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private AuthResponse registerOwner(String email) {
        return authService.register(new com.acuityspace.mantle.web.dto.RegisterRequest(
                email, "password123", "Owner", "TestOrg", OrgType.INDIVIDUAL));
    }

    private User createStandardMember(AuthResponse owner) {
        User std = userRepository.save(
                new User("std-" + owner.email(), passwordEncoder.encode("pass"), "Standard"));
        Org org = orgRepository.findById(owner.orgId()).orElseThrow();
        SubOrg subOrg = subOrgRepository.findById(owner.subOrgId()).orElseThrow();
        orgMembershipRepository.save(new OrgMembership(std, org));
        subOrgMembershipRepository.save(new SubOrgMembership(std, subOrg, UserRole.STANDARD));
        return std;
    }

    @Test
    void createProject_adminCanCreate() {
        AuthResponse owner = registerOwner("proj-admin@crud.com");

        ProjectResponse resp = projectService.createProject(
                owner.subOrgId(), new ProjectRequest("Alpha", "desc"), owner.userId());

        assertThat(resp.name()).isEqualTo("Alpha");
        assertThat(resp.subOrgId()).isEqualTo(owner.subOrgId());
        assertThat(resp.appCount()).isZero();
        assertThat(resp.id()).isNotNull();
    }

    @Test
    void createProject_standardMember_throwsAccessDeniedException() {
        AuthResponse owner = registerOwner("proj-std@crud.com");
        User std = createStandardMember(owner);

        assertThatThrownBy(() -> projectService.createProject(
                owner.subOrgId(), new ProjectRequest("Beta", null), std.getId()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void createProject_duplicateName_throwsDuplicateNameException() {
        AuthResponse owner = registerOwner("proj-dup@crud.com");
        projectService.createProject(owner.subOrgId(), new ProjectRequest("Gamma", null), owner.userId());

        assertThatThrownBy(() -> projectService.createProject(
                owner.subOrgId(), new ProjectRequest("Gamma", null), owner.userId()))
                .isInstanceOf(DuplicateNameException.class);
    }

    @Test
    void getProjects_memberCanView() {
        AuthResponse owner = registerOwner("proj-view@crud.com");
        projectService.createProject(owner.subOrgId(), new ProjectRequest("P1", null), owner.userId());
        projectService.createProject(owner.subOrgId(), new ProjectRequest("P2", null), owner.userId());

        List<ProjectResponse> projects = projectService.getProjects(owner.subOrgId(), owner.userId());

        assertThat(projects).hasSize(2);
        assertThat(projects).extracting(ProjectResponse::name).containsExactlyInAnyOrder("P1", "P2");
    }

    @Test
    void getProjects_nonMember_throwsAccessDeniedException() {
        AuthResponse owner = registerOwner("proj-nonmember@crud.com");
        // Second user registered in their own org — not a member of owner's SubOrg
        AuthResponse other = authService.register(new com.acuityspace.mantle.web.dto.RegisterRequest(
                "proj-other@crud.com", "password123", "Other", "OtherOrg", OrgType.INDIVIDUAL));

        assertThatThrownBy(() -> projectService.getProjects(owner.subOrgId(), other.userId()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void updateProject_adminCanUpdate() {
        AuthResponse owner = registerOwner("proj-update@crud.com");
        ProjectResponse created = projectService.createProject(
                owner.subOrgId(), new ProjectRequest("Old Name", "old desc"), owner.userId());

        ProjectResponse updated = projectService.updateProject(
                created.id(), new ProjectRequest("New Name", "new desc"), owner.userId());

        assertThat(updated.name()).isEqualTo("New Name");
        assertThat(updated.description()).isEqualTo("new desc");
    }

    @Test
    void deleteProject_withNoApps_succeeds() {
        AuthResponse owner = registerOwner("proj-del@crud.com");
        ProjectResponse created = projectService.createProject(
                owner.subOrgId(), new ProjectRequest("ToDelete", null), owner.userId());

        projectService.deleteProject(created.id(), owner.userId());

        assertThat(projectService.getProjects(owner.subOrgId(), owner.userId())).isEmpty();
    }

    @Test
    void deleteProject_withApps_throwsIllegalOperationException() {
        AuthResponse owner = registerOwner("proj-del-apps@crud.com");
        ProjectResponse project = projectService.createProject(
                owner.subOrgId(), new ProjectRequest("HasApps", null), owner.userId());
        appService.createApp(project.id(), new AppRequest("MyApp", null), owner.userId());

        assertThatThrownBy(() -> projectService.deleteProject(project.id(), owner.userId()))
                .isInstanceOf(IllegalOperationException.class);
    }
}
