package com.acuityspace.mantle.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppResponse(
        UUID id,
        UUID projectId,
        String name,
        String description,
        LocalDateTime createdAt
) {
}
