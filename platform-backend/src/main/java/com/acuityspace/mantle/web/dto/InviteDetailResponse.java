package com.acuityspace.mantle.web.dto;

import com.acuityspace.mantle.domain.enums.InviteStatus;

import java.util.UUID;

public record InviteDetailResponse(
        UUID inviteId,
        UUID orgId,
        String orgName,
        String email,
        InviteStatus status
) {
}
