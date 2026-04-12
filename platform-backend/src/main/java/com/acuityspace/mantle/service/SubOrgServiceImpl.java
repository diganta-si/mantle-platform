package com.acuityspace.mantle.service;

import com.acuityspace.mantle.domain.enums.OrgType;
import com.acuityspace.mantle.domain.enums.UserRole;
import com.acuityspace.mantle.domain.model.Org;
import com.acuityspace.mantle.domain.model.OrgMembership;
import com.acuityspace.mantle.domain.model.SubOrg;
import com.acuityspace.mantle.domain.model.SubOrgMembership;
import com.acuityspace.mantle.domain.model.User;
import com.acuityspace.mantle.domain.repository.OrgMembershipRepository;
import com.acuityspace.mantle.domain.repository.OrgRepository;
import com.acuityspace.mantle.domain.repository.ProjectRepository;
import com.acuityspace.mantle.domain.repository.SubOrgMembershipRepository;
import com.acuityspace.mantle.domain.repository.SubOrgRepository;
import com.acuityspace.mantle.domain.repository.UserRepository;
import com.acuityspace.mantle.exception.AccessDeniedException;
import com.acuityspace.mantle.exception.DuplicateNameException;
import com.acuityspace.mantle.exception.IllegalOperationException;
import com.acuityspace.mantle.exception.ResourceNotFoundException;
import com.acuityspace.mantle.web.dto.SubOrgRequest;
import com.acuityspace.mantle.web.dto.SubOrgResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SubOrgServiceImpl implements SubOrgService {

    private final OrgRepository orgRepository;
    private final SubOrgRepository subOrgRepository;
    private final SubOrgMembershipRepository subOrgMembershipRepository;
    private final OrgMembershipRepository orgMembershipRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final RoleResolutionService roleResolutionService;

    public SubOrgServiceImpl(OrgRepository orgRepository,
                              SubOrgRepository subOrgRepository,
                              SubOrgMembershipRepository subOrgMembershipRepository,
                              OrgMembershipRepository orgMembershipRepository,
                              ProjectRepository projectRepository,
                              UserRepository userRepository,
                              RoleResolutionService roleResolutionService) {
        this.orgRepository = orgRepository;
        this.subOrgRepository = subOrgRepository;
        this.subOrgMembershipRepository = subOrgMembershipRepository;
        this.orgMembershipRepository = orgMembershipRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.roleResolutionService = roleResolutionService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubOrgResponse> getSubOrgsForOrg(UUID orgId, UUID requestingUserId) {
        Org org = orgRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Org not found: " + orgId));

        boolean isMember = orgMembershipRepository.existsById(
                new OrgMembership.OrgMembershipId(requestingUserId, orgId));
        if (!isMember) {
            throw new AccessDeniedException("User is not a member of this org");
        }

        return subOrgRepository.findByOrg(org).stream()
                .map(subOrg -> new SubOrgResponse(
                        subOrg.getId(),
                        orgId,
                        subOrg.getName(),
                        subOrg.isDefault(),
                        subOrg.isGlobal(),
                        projectRepository.findBySubOrg(subOrg).size()))
                .toList();
    }

    @Override
    @Transactional
    public SubOrgResponse createSubOrg(UUID orgId, SubOrgRequest request, UUID requestingUserId) {
        Org org = orgRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Org not found: " + orgId));

        if (org.getType() != OrgType.ENTERPRISE) {
            throw new IllegalOperationException("Only ENTERPRISE orgs can create additional SubOrgs");
        }

        SubOrg defaultSubOrg = subOrgRepository.findByOrgAndIsDefaultTrue(org)
                .orElseThrow(() -> new ResourceNotFoundException("Default SubOrg not found"));

        UserRole requesterRole = roleResolutionService
                .getUserRoleOnSubOrg(requestingUserId, defaultSubOrg.getId())
                .orElseThrow(() -> new AccessDeniedException("User has no membership in the default SubOrg"));

        if (requesterRole != UserRole.SUPER_ADMIN) {
            throw new AccessDeniedException("Only SUPER_ADMIN on the default SubOrg can create SubOrgs");
        }

        boolean nameExists = subOrgRepository.findByOrg(org).stream()
                .anyMatch(s -> s.getName().equalsIgnoreCase(request.name()));
        if (nameExists) {
            throw new DuplicateNameException("A SubOrg named '" + request.name() + "' already exists in this org");
        }

        SubOrg newSubOrg = subOrgRepository.save(new SubOrg(org, request.name(), false, false));

        User creator = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + requestingUserId));
        subOrgMembershipRepository.save(new SubOrgMembership(creator, newSubOrg, UserRole.SUPER_ADMIN));

        return new SubOrgResponse(newSubOrg.getId(), orgId, newSubOrg.getName(),
                newSubOrg.isDefault(), newSubOrg.isGlobal(), 0L);
    }

    @Override
    @Transactional
    public void deleteSubOrg(UUID subOrgId, UUID requestingUserId) {
        SubOrg subOrg = subOrgRepository.findById(subOrgId)
                .orElseThrow(() -> new ResourceNotFoundException("SubOrg not found: " + subOrgId));

        UserRole requesterRole = roleResolutionService
                .getUserRoleOnSubOrg(requestingUserId, subOrgId)
                .orElseThrow(() -> new AccessDeniedException("User has no membership in this SubOrg"));

        if (requesterRole != UserRole.SUPER_ADMIN) {
            throw new AccessDeniedException("Only SUPER_ADMIN can delete a SubOrg");
        }

        if (subOrg.isDefault() || subOrg.isGlobal()) {
            throw new IllegalOperationException("Default and global SubOrgs cannot be deleted");
        }

        if (!projectRepository.findBySubOrg(subOrg).isEmpty()) {
            throw new IllegalOperationException("Cannot delete a SubOrg that has active projects");
        }

        subOrgMembershipRepository.deleteAll(subOrgMembershipRepository.findBySubOrg(subOrg));
        subOrgRepository.delete(subOrg);
    }
}
