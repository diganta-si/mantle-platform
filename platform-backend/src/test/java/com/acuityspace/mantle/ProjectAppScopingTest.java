package com.acuityspace.mantle;

import com.acuityspace.mantle.config.TestContainersBase;
import com.acuityspace.mantle.domain.enums.OrgType;
import com.acuityspace.mantle.exception.AccessDeniedException;
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
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class ProjectAppScopingTest extends TestContainersBase {

    @Autowired private AuthService authService;
    @Autowired private ProjectService projectService;
    @Autowired private AppService appService;

    private AuthResponse register(String email, OrgType orgType) {
        return authService.register(new RegisterRequest(
                email, "password123", "User", "Org-" + email, orgType));
    }

    @Test
    void userCannotAccessProjectInSubOrgTheyAreNotMemberOf() {
        // User A owns SubOrg A and creates a project there
        AuthResponse userA = register("scope-a@scope.com", OrgType.INDIVIDUAL);
        ProjectResponse projectInA = projectService.createProject(
                userA.subOrgId(), new ProjectRequest("ProjectInA", null), userA.userId());

        // User B belongs to their own org — not a member of SubOrg A
        AuthResponse userB = register("scope-b@scope.com", OrgType.INDIVIDUAL);

        assertThatThrownBy(() -> projectService.getProject(projectInA.id(), userB.userId()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void userCannotAccessAppInSubOrgTheyAreNotMemberOf() {
        // User A creates a project and app in their SubOrg
        AuthResponse userA = register("scope-app-a@scope.com", OrgType.INDIVIDUAL);
        ProjectResponse projectInA = projectService.createProject(
                userA.subOrgId(), new ProjectRequest("ProjectA", null), userA.userId());
        AppResponse appInA = appService.createApp(
                projectInA.id(), new AppRequest("AppInA", null), userA.userId());

        // User B is not a member of SubOrg A
        AuthResponse userB = register("scope-app-b@scope.com", OrgType.INDIVIDUAL);

        assertThatThrownBy(() -> appService.getApp(appInA.id(), userB.userId()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void userInSubOrgACannotSeeProjectsInSubOrgB() {
        // User A has SubOrg A
        AuthResponse userA = register("scope-list-a@scope.com", OrgType.INDIVIDUAL);

        // User B has SubOrg B with a project
        AuthResponse userB = register("scope-list-b@scope.com", OrgType.INDIVIDUAL);
        projectService.createProject(userB.subOrgId(), new ProjectRequest("InB", null), userB.userId());

        // User A tries to list projects in SubOrg B — not a member
        assertThatThrownBy(() -> projectService.getProjects(userB.subOrgId(), userA.userId()))
                .isInstanceOf(AccessDeniedException.class);
    }
}
