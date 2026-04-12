package com.acuityspace.mantle.web.dto;

import com.acuityspace.mantle.domain.enums.InviteStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record InviteResponse(
        UUID id,
        UUID orgId,
        String email,
        InviteStatus status,
        String invitedByName,
        LocalDateTime createdAt
) {
}
