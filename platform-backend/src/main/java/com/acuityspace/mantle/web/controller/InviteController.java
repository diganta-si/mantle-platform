package com.acuityspace.mantle.web.controller;

import com.acuityspace.mantle.service.InviteService;
import com.acuityspace.mantle.web.dto.AcceptInviteRequest;
import com.acuityspace.mantle.web.dto.AuthResponse;
import com.acuityspace.mantle.web.dto.InviteRequest;
import com.acuityspace.mantle.web.dto.InviteResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
@RequestMapping("/api/orgs/{orgId}/invites")
public class InviteController {

    private final InviteService inviteService;
    private final CurrentUserExtractor userExtractor;

    public InviteController(InviteService inviteService, CurrentUserExtractor userExtractor) {
        this.inviteService = inviteService;
        this.userExtractor = userExtractor;
    }

    @GetMapping
    public List<InviteResponse> getInvites(@PathVariable UUID orgId, HttpServletRequest request) {
        UUID userId = userExtractor.extractUserId(request);
        return inviteService.getInvites(orgId, userId);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InviteResponse sendInvite(@PathVariable UUID orgId,
                                     @RequestBody @Valid InviteRequest inviteRequest,
                                     HttpServletRequest request) {
        UUID userId = userExtractor.extractUserId(request);
        return inviteService.sendInvite(orgId, inviteRequest, userId);
    }

    @PostMapping("/{inviteId}/accept")
    public AuthResponse acceptInvite(@PathVariable UUID orgId,
                                     @PathVariable UUID inviteId,
                                     @RequestBody @Valid AcceptInviteRequest acceptRequest,
                                     HttpServletResponse response) {
        return inviteService.acceptInvite(inviteId, acceptRequest, response);
    }

    @DeleteMapping("/{inviteId}/revoke")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeInvite(@PathVariable UUID orgId,
                             @PathVariable UUID inviteId,
                             HttpServletRequest request) {
        UUID userId = userExtractor.extractUserId(request);
        inviteService.revokeInvite(inviteId, userId);
    }
}
