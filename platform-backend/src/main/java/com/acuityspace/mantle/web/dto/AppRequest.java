package com.acuityspace.mantle.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AppRequest(
        @NotBlank String name,
        String description
) {
}
