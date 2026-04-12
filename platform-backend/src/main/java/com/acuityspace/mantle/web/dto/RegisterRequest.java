package com.acuityspace.mantle.web.dto;

import com.acuityspace.mantle.domain.enums.OrgType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String name,
        @NotBlank String orgName,
        @NotNull OrgType orgType
) {
}
