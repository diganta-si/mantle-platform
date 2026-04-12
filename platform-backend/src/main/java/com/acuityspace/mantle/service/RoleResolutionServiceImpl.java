package com.acuityspace.mantle.service;

import com.acuityspace.mantle.domain.enums.UserRole;
import com.acuityspace.mantle.domain.model.SubOrgMembership;
import com.acuityspace.mantle.domain.repository.SubOrgMembershipRepository;
import com.acuityspace.mantle.exception.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class RoleResolutionServiceImpl implements RoleResolutionService {

    private final SubOrgMembershipRepository subOrgMembershipRepository;

    public RoleResolutionServiceImpl(SubOrgMembershipRepository subOrgMembershipRepository) {
        this.subOrgMembershipRepository = subOrgMembershipRepository;
    }

    @Override
    public Optional<UserRole> getUserRoleOnSubOrg(UUID userId, UUID subOrgId) {
        return subOrgMembershipRepository
                .findById(new SubOrgMembership.SubOrgMembershipId(userId, subOrgId))
                .map(SubOrgMembership::getRole);
    }

    @Override
    public void requireMembership(UUID userId, UUID subOrgId) {
        getUserRoleOnSubOrg(userId, subOrgId)
                .orElseThrow(() -> new AccessDeniedException("User is not a member of this SubOrg"));
    }

    @Override
    public void requireAdminOrAbove(UUID userId, UUID subOrgId) {
        UserRole role = getUserRoleOnSubOrg(userId, subOrgId)
                .orElseThrow(() -> new AccessDeniedException("User is not a member of this SubOrg"));
        if (role != UserRole.ADMIN && role != UserRole.SUPER_ADMIN) {
            throw new AccessDeniedException("ADMIN or SUPER_ADMIN role required");
        }
    }
}
