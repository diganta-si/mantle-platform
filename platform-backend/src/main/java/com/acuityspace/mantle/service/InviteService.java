package com.acuityspace.mantle.service;

import com.acuityspace.mantle.web.dto.AcceptInviteRequest;
import com.acuityspace.mantle.web.dto.AuthResponse;
import com.acuityspace.mantle.web.dto.InviteDetailResponse;
import com.acuityspace.mantle.web.dto.InviteRequest;
import com.acuityspace.mantle.web.dto.InviteResponse;
import com.acuityspace.mantle.web.dto.MemberResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.UUID;

public interface InviteService {

    InviteResponse sendInvite(UUID orgId, InviteRequest request, UUID requestingUserId);

    AuthResponse acceptInvite(UUID inviteId, AcceptInviteRequest request, HttpServletResponse response);

    void revokeInvite(UUID inviteId, UUID requestingUserId);

    List<InviteResponse> getInvites(UUID orgId, UUID requestingUserId);

    List<MemberResponse> getMembers(UUID orgId, UUID requestingUserId);

    InviteDetailResponse getInviteDetail(UUID inviteId);
}
