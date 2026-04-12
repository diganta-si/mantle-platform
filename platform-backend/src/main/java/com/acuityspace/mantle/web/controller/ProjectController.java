package com.acuityspace.mantle.web.controller;

import com.acuityspace.mantle.service.ProjectService;
import com.acuityspace.mantle.web.dto.ProjectRequest;
import com.acuityspace.mantle.web.dto.ProjectResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/suborgs/{subOrgId}/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final CurrentUserExtractor userExtractor;

    public ProjectController(ProjectService projectService, CurrentUserExtractor userExtractor) {
        this.projectService = projectService;
        this.userExtractor = userExtractor;
    }

    @GetMapping
    public List<ProjectResponse> getProjects(@PathVariable UUID subOrgId, HttpServletRequest request) {
        UUID userId = userExtractor.extractUserId(request);
        return projectService.getProjects(subOrgId, userId);
    }

    @GetMapping("/{projectId}")
    public ProjectResponse getProject(@PathVariable UUID subOrgId,
                                      @PathVariable UUID projectId,
                                      HttpServletRequest request) {
        UUID userId = userExtractor.extractUserId(request);
        return projectService.getProject(projectId, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(@PathVariable UUID subOrgId,
                                         @RequestBody @Valid ProjectRequest projectRequest,
                                         HttpServletRequest request) {
        UUID userId = userExtractor.extractUserId(request);
        return projectService.createProject(subOrgId, projectRequest, userId);
    }

    @PutMapping("/{projectId}")
    public ProjectResponse updateProject(@PathVariable UUID subOrgId,
                                         @PathVariable UUID projectId,
                                         @RequestBody @Valid ProjectRequest projectRequest,
                                         HttpServletRequest request) {
        UUID userId = userExtractor.extractUserId(request);
        return projectService.updateProject(projectId, projectRequest, userId);
    }

    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable UUID subOrgId,
                              @PathVariable UUID projectId,
                              HttpServletRequest request) {
        UUID userId = userExtractor.extractUserId(request);
        projectService.deleteProject(projectId, userId);
    }
}
