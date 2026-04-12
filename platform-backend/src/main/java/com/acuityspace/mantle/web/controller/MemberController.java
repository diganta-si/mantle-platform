package com.acuityspace.mantle.web.controller;

import com.acuityspace.mantle.service.InviteService;
import com.acuityspace.mantle.web.dto.MemberResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orgs/{orgId}/members")
public class MemberController {

    private final InviteService inviteService;
    private final CurrentUserExtractor userExtractor;

    public MemberController(InviteService inviteService, CurrentUserExtractor userExtractor) {
        this.inviteService = inviteService;
        this.userExtractor = userExtractor;
    }

    @GetMapping
    public List<MemberResponse> getMembers(@PathVariable UUID orgId, HttpServletRequest request) {
        UUID userId = userExtractor.extractUserId(request);
        return inviteService.getMembers(orgId, userId);
    }
}
