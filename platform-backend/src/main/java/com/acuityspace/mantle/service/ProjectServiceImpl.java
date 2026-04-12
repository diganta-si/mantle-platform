package com.acuityspace.mantle.service;

import com.acuityspace.mantle.domain.model.Project;
import com.acuityspace.mantle.domain.model.SubOrg;
import com.acuityspace.mantle.domain.repository.AppRepository;
import com.acuityspace.mantle.domain.repository.ProjectRepository;
import com.acuityspace.mantle.domain.repository.SubOrgRepository;
import com.acuityspace.mantle.exception.DuplicateNameException;
import com.acuityspace.mantle.exception.IllegalOperationException;
import com.acuityspace.mantle.exception.ResourceNotFoundException;
import com.acuityspace.mantle.web.dto.ProjectRequest;
import com.acuityspace.mantle.web.dto.ProjectResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final SubOrgRepository subOrgRepository;
    private final AppRepository appRepository;
    private final RoleResolutionService roleResolutionService;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              SubOrgRepository subOrgRepository,
                              AppRepository appRepository,
                              RoleResolutionService roleResolutionService) {
        this.projectRepository = projectRepository;
        this.subOrgRepository = subOrgRepository;
        this.appRepository = appRepository;
        this.roleResolutionService = roleResolutionService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjects(UUID subOrgId, UUID requestingUserId) {
        SubOrg subOrg = subOrgRepository.findById(subOrgId)
                .orElseThrow(() -> new ResourceNotFoundException("SubOrg not found: " + subOrgId));

        roleResolutionService.requireMembership(requestingUserId, subOrgId);

        return projectRepository.findBySubOrg(subOrg).stream()
                .map(p -> toResponse(p, appRepository.findByProject(p).size()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProject(UUID projectId, UUID requestingUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        roleResolutionService.requireMembership(requestingUserId, project.getSubOrg().getId());

        return toResponse(project, appRepository.findByProject(project).size());
    }

    @Override
    @Transactional
    public ProjectResponse createProject(UUID subOrgId, ProjectRequest request, UUID requestingUserId) {
        SubOrg subOrg = subOrgRepository.findById(subOrgId)
                .orElseThrow(() -> new ResourceNotFoundException("SubOrg not found: " + subOrgId));

        roleResolutionService.requireAdminOrAbove(requestingUserId, subOrgId);

        boolean nameExists = projectRepository.findBySubOrg(subOrg).stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(request.name()));
        if (nameExists) {
            throw new DuplicateNameException("A project named '" + request.name() + "' already exists in this SubOrg");
        }

        Project saved = projectRepository.save(new Project(subOrg, request.name(), request.description()));
        return toResponse(saved, 0);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(UUID projectId, ProjectRequest request, UUID requestingUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        SubOrg subOrg = project.getSubOrg();
        roleResolutionService.requireAdminOrAbove(requestingUserId, subOrg.getId());

        if (!project.getName().equalsIgnoreCase(request.name())) {
            boolean nameExists = projectRepository.findBySubOrg(subOrg).stream()
                    .anyMatch(p -> !p.getId().equals(project.getId()) && p.getName().equalsIgnoreCase(request.name()));
            if (nameExists) {
                throw new DuplicateNameException("A project named '" + request.name() + "' already exists in this SubOrg");
            }
        }

        project.setName(request.name());
        project.setDescription(request.description());
        Project saved = projectRepository.save(project);
        return toResponse(saved, appRepository.findByProject(saved).size());
    }

    @Override
    @Transactional
    public void deleteProject(UUID projectId, UUID requestingUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        roleResolutionService.requireAdminOrAbove(requestingUserId, project.getSubOrg().getId());

        if (!appRepository.findByProject(project).isEmpty()) {
            throw new IllegalOperationException("Cannot delete a project that has active apps");
        }

        projectRepository.delete(project);
    }

    private ProjectResponse toResponse(Project project, long appCount) {
        return new ProjectResponse(
                project.getId(),
                project.getSubOrg().getId(),
                project.getName(),
                project.getDescription(),
                appCount,
                project.getCreatedAt());
    }
}
