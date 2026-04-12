package com.acuityspace.mantle.web.dto;

import com.acuityspace.mantle.domain.enums.OrgType;
import com.acuityspace.mantle.domain.enums.UserRole;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String email,
        String name,
        UUID orgId,
        String orgName,
        OrgType orgType,
        UUID subOrgId,
        UserRole role
) {
}
