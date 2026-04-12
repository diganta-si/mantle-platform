package com.acuityspace.mantle.web.dto;

import com.acuityspace.mantle.domain.enums.OrgType;

import java.util.UUID;

public record OrgResponse(
        UUID id,
        String name,
        OrgType type,
        long memberCount,
        long subOrgCount
) {
}
