package com.acuityspace.mantle.service;

import com.acuityspace.mantle.web.dto.ProjectRequest;
import com.acuityspace.mantle.web.dto.ProjectResponse;

import java.util.List;
import java.util.UUID;

public interface ProjectService {

    List<ProjectResponse> getProjects(UUID subOrgId, UUID requestingUserId);

    ProjectResponse getProject(UUID projectId, UUID requestingUserId);

    ProjectResponse createProject(UUID subOrgId, ProjectRequest request, UUID requestingUserId);

    ProjectResponse updateProject(UUID projectId, ProjectRequest request, UUID requestingUserId);

    void deleteProject(UUID projectId, UUID requestingUserId);
}
