package com.acuityspace.mantle.web.dto;

import com.acuityspace.mantle.domain.enums.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record MemberResponse(
        UUID userId,
        String name,
        String email,
        UserRole role,
        LocalDateTime joinedAt
) {
}
