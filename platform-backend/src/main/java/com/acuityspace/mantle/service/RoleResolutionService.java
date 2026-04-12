package com.acuityspace.mantle.service;

import com.acuityspace.mantle.domain.enums.UserRole;

import java.util.Optional;
import java.util.UUID;

public interface RoleResolutionService {

    /**
     * Returns the role of the given user on the given SubOrg, or empty if not a member.
     */
    Optional<UserRole> getUserRoleOnSubOrg(UUID userId, UUID subOrgId);

    /**
     * Throws AccessDeniedException if the user has no membership on the SubOrg.
     */
    void requireMembership(UUID userId, UUID subOrgId);

    /**
     * Throws AccessDeniedException if the user's role is not ADMIN or SUPER_ADMIN.
     * Also throws if the user has no membership at all.
     */
    void requireAdminOrAbove(UUID userId, UUID subOrgId);
}
