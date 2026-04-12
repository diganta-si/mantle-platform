package com.acuityspace.mantle.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        UUID subOrgId,
        String name,
        String description,
        long appCount,
        LocalDateTime createdAt
) {
}
