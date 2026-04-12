package com.acuityspace.mantle.web.dto;

import java.util.UUID;

public record SubOrgResponse(
        UUID id,
        UUID orgId,
        String name,
        boolean isDefault,
        boolean isGlobal,
        long projectCount
) {
}
