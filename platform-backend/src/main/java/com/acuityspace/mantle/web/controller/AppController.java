package com.acuityspace.mantle.web.controller;

import com.acuityspace.mantle.service.AppService;
import com.acuityspace.mantle.web.dto.AppRequest;
import com.acuityspace.mantle.web.dto.AppResponse;
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
@RequestMapping("/api/projects/{projectId}/apps")
public class AppController {

    private final AppService appService;
    private final CurrentUserExtractor userExtractor;

    public AppController(AppService appService, CurrentUserExtractor userExtractor) {
        this.appService = appService;
        this.userExtractor = userExtractor;
    }

    @GetMapping
    public List<AppResponse> getApps(@PathVariable UUID projectId, HttpServletRequest request) {
        UUID userId = userExtractor.extractUserId(request);
        return appService.getApps(projectId, userId);
    }

    @GetMapping("/{appId}")
    public AppResponse getApp(@PathVariable UUID projectId,
                              @PathVariable UUID appId,
                              HttpServletRequest request) {
        UUID userId = userExtractor.extractUserId(request);
        return appService.getApp(appId, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppResponse createApp(@PathVariable UUID projectId,
                                 @RequestBody @Valid AppRequest appRequest,
                                 HttpServletRequest request) {
        UUID userId = userExtractor.extractUserId(request);
        return appService.createApp(projectId, appRequest, userId);
    }

    @PutMapping("/{appId}")
    public AppResponse updateApp(@PathVariable UUID projectId,
                                 @PathVariable UUID appId,
                                 @RequestBody @Valid AppRequest appRequest,
                                 HttpServletRequest request) {
        UUID userId = userExtractor.extractUserId(request);
        return appService.updateApp(appId, appRequest, userId);
    }

    @DeleteMapping("/{appId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteApp(@PathVariable UUID projectId,
                          @PathVariable UUID appId,
                          HttpServletRequest request) {
        UUID userId = userExtractor.extractUserId(request);
        appService.deleteApp(appId, userId);
    }
}
