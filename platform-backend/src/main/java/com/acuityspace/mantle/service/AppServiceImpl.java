package com.acuityspace.mantle.service;

import com.acuityspace.mantle.domain.model.App;
import com.acuityspace.mantle.domain.model.Project;
import com.acuityspace.mantle.domain.repository.AppRepository;
import com.acuityspace.mantle.domain.repository.ProjectRepository;
import com.acuityspace.mantle.exception.DuplicateNameException;
import com.acuityspace.mantle.exception.ResourceNotFoundException;
import com.acuityspace.mantle.web.dto.AppRequest;
import com.acuityspace.mantle.web.dto.AppResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AppServiceImpl implements AppService {

    private final AppRepository appRepository;
    private final ProjectRepository projectRepository;
    private final RoleResolutionService roleResolutionService;

    public AppServiceImpl(AppRepository appRepository,
                          ProjectRepository projectRepository,
                          RoleResolutionService roleResolutionService) {
        this.appRepository = appRepository;
        this.projectRepository = projectRepository;
        this.roleResolutionService = roleResolutionService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppResponse> getApps(UUID projectId, UUID requestingUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        roleResolutionService.requireMembership(requestingUserId, project.getSubOrg().getId());

        return appRepository.findByProject(project).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AppResponse getApp(UUID appId, UUID requestingUserId) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException("App not found: " + appId));

        roleResolutionService.requireMembership(requestingUserId, app.getProject().getSubOrg().getId());

        return toResponse(app);
    }

    @Override
    @Transactional
    public AppResponse createApp(UUID projectId, AppRequest request, UUID requestingUserId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));

        roleResolutionService.requireAdminOrAbove(requestingUserId, project.getSubOrg().getId());

        boolean nameExists = appRepository.findByProject(project).stream()
                .anyMatch(a -> a.getName().equalsIgnoreCase(request.name()));
        if (nameExists) {
            throw new DuplicateNameException("An app named '" + request.name() + "' already exists in this project");
        }

        App saved = appRepository.save(new App(project, request.name(), request.description()));
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AppResponse updateApp(UUID appId, AppRequest request, UUID requestingUserId) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException("App not found: " + appId));

        Project project = app.getProject();
        roleResolutionService.requireAdminOrAbove(requestingUserId, project.getSubOrg().getId());

        if (!app.getName().equalsIgnoreCase(request.name())) {
            boolean nameExists = appRepository.findByProject(project).stream()
                    .anyMatch(a -> !a.getId().equals(app.getId()) && a.getName().equalsIgnoreCase(request.name()));
            if (nameExists) {
                throw new DuplicateNameException("An app named '" + request.name() + "' already exists in this project");
            }
        }

        app.setName(request.name());
        app.setDescription(request.description());
        return toResponse(appRepository.save(app));
    }

    @Override
    @Transactional
    public void deleteApp(UUID appId, UUID requestingUserId) {
        App app = appRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException("App not found: " + appId));

        roleResolutionService.requireAdminOrAbove(requestingUserId, app.getProject().getSubOrg().getId());

        appRepository.delete(app);
    }

    private AppResponse toResponse(App app) {
        return new AppResponse(
                app.getId(),
                app.getProject().getId(),
                app.getName(),
                app.getDescription(),
                app.getCreatedAt());
    }
}
