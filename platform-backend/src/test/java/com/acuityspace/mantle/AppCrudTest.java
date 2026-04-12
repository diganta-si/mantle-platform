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
import com.acuityspace.mantle.service.AppService;
import com.acuityspace.mantle.service.AuthService;
import com.acuityspace.mantle.service.ProjectService;
import com.acuityspace.mantle.web.dto.AppRequest;
import com.acuityspace.mantle.web.dto.AppResponse;
import com.acuityspace.mantle.web.dto.AuthResponse;
import com.acuityspace.mantle.web.dto.ProjectRequest;
import com.acuityspace.mantle.web.dto.ProjectResponse;
import com.acuityspace.mantle.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class AppCrudTest extends TestContainersBase {

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
        return authService.register(new RegisterRequest(
                email, "password123", "Owner", "TestOrg", OrgType.INDIVIDUAL));
    }

    private ProjectResponse setupProject(AuthResponse owner) {
        return projectService.createProject(
                owner.subOrgId(), new ProjectRequest("TestProject", null), owner.userId());
    }

    private User createStandardMember(AuthResponse owner) {
        User std = userRepository.save(
                new User("std-app-" + owner.email(), passwordEncoder.encode("pass"), "Standard"));
        Org org = orgRepository.findById(owner.orgId()).orElseThrow();
        SubOrg subOrg = subOrgRepository.findById(owner.subOrgId()).orElseThrow();
        orgMembershipRepository.save(new OrgMembership(std, org));
        subOrgMembershipRepository.save(new SubOrgMembership(std, subOrg, UserRole.STANDARD));
        return std;
    }

    @Test
    void createApp_adminCanCreate() {
        AuthResponse owner = registerOwner("app-admin@crud.com");
        ProjectResponse project = setupProject(owner);

        AppResponse resp = appService.createApp(
                project.id(), new AppRequest("MyApp", "desc"), owner.userId());

        assertThat(resp.name()).isEqualTo("MyApp");
        assertThat(resp.projectId()).isEqualTo(project.id());
        assertThat(resp.id()).isNotNull();
    }

    @Test
    void createApp_standardMember_throwsAccessDeniedException() {
        AuthResponse owner = registerOwner("app-std@crud.com");
        ProjectResponse project = setupProject(owner);
        User std = createStandardMember(owner);

        assertThatThrownBy(() -> appService.createApp(
                project.id(), new AppRequest("BlockedApp", null), std.getId()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void createApp_duplicateName_throwsDuplicateNameException() {
        AuthResponse owner = registerOwner("app-dup@crud.com");
        ProjectResponse project = setupProject(owner);
        appService.createApp(project.id(), new AppRequest("SameName", null), owner.userId());

        assertThatThrownBy(() -> appService.createApp(
                project.id(), new AppRequest("SameName", null), owner.userId()))
                .isInstanceOf(DuplicateNameException.class);
    }

    @Test
    void getApps_memberCanView() {
        AuthResponse owner = registerOwner("app-view@crud.com");
        ProjectResponse project = setupProject(owner);
        appService.createApp(project.id(), new AppRequest("App1", null), owner.userId());
        appService.createApp(project.id(), new AppRequest("App2", null), owner.userId());

        List<AppResponse> apps = appService.getApps(project.id(), owner.userId());

        assertThat(apps).hasSize(2);
        assertThat(apps).extracting(AppResponse::name).containsExactlyInAnyOrder("App1", "App2");
    }

    @Test
    void updateApp_adminCanUpdate() {
        AuthResponse owner = registerOwner("app-update@crud.com");
        ProjectResponse project = setupProject(owner);
        AppResponse created = appService.createApp(
                project.id(), new AppRequest("OldApp", "old"), owner.userId());

        AppResponse updated = appService.updateApp(
                created.id(), new AppRequest("NewApp", "new"), owner.userId());

        assertThat(updated.name()).isEqualTo("NewApp");
        assertThat(updated.description()).isEqualTo("new");
    }

    @Test
    void deleteApp_adminCanDelete() {
        AuthResponse owner = registerOwner("app-del@crud.com");
        ProjectResponse project = setupProject(owner);
        AppResponse app = appService.createApp(
                project.id(), new AppRequest("ToDelete", null), owner.userId());

        appService.deleteApp(app.id(), owner.userId());

        assertThat(appService.getApps(project.id(), owner.userId())).isEmpty();
    }
}
