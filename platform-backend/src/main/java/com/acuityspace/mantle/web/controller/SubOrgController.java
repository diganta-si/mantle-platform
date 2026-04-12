package com.acuityspace.mantle.web.controller;

import com.acuityspace.mantle.service.SubOrgService;
import com.acuityspace.mantle.web.dto.SubOrgRequest;
import com.acuityspace.mantle.web.dto.SubOrgResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orgs/{orgId}/suborgs")
public class SubOrgController {

    private final SubOrgService subOrgService;
    private final CurrentUserExtractor userExtractor;

    public SubOrgController(SubOrgService subOrgService, CurrentUserExtractor userExtractor) {
        this.subOrgService = subOrgService;
        this.userExtractor = userExtractor;
    }

    @GetMapping
    public List<SubOrgResponse> getSubOrgsForOrg(@PathVariable UUID orgId, HttpServletRequest request) {
        UUID userId = userExtractor.extractUserId(request);
        return subOrgService.getSubOrgsForOrg(orgId, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubOrgResponse createSubOrg(@PathVariable UUID orgId,
                                       @RequestBody @Valid SubOrgRequest subOrgRequest,
                                       HttpServletRequest request) {
        UUID userId = userExtractor.extractUserId(request);
        return subOrgService.createSubOrg(orgId, subOrgRequest, userId);
    }

    @DeleteMapping("/{subOrgId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubOrg(@PathVariable UUID orgId,
                             @PathVariable UUID subOrgId,
                             HttpServletRequest request) {
        UUID userId = userExtractor.extractUserId(request);
        subOrgService.deleteSubOrg(subOrgId, userId);
    }
}
