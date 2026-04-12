package com.acuityspace.mantle.web.dto;

import jakarta.validation.constraints.NotBlank;

public record SubOrgRequest(
        @NotBlank String name
) {
}
