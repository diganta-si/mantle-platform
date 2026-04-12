package com.acuityspace.mantle.web.controller;

import com.acuityspace.mantle.service.OrgService;
import com.acuityspace.mantle.web.dto.OrgResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/orgs")
public class OrgController {

    private final OrgService orgService;
    private final CurrentUserExtractor userExtractor;

    public OrgController(OrgService orgService, CurrentUserExtractor userExtractor) {
        this.orgService = orgService;
        this.userExtractor = userExtractor;
    }

    @GetMapping("/{orgId}")
    public OrgResponse getOrg(@PathVariable UUID orgId, HttpServletRequest request) {
        UUID userId = userExtractor.extractUserId(request);
        return orgService.getOrg(orgId, userId);
    }
}
