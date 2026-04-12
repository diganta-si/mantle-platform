package com.acuityspace.mantle.service;

import com.acuityspace.mantle.domain.model.Org;
import com.acuityspace.mantle.domain.model.OrgMembership;
import com.acuityspace.mantle.domain.repository.OrgMembershipRepository;
import com.acuityspace.mantle.domain.repository.OrgRepository;
import com.acuityspace.mantle.domain.repository.SubOrgRepository;
import com.acuityspace.mantle.exception.AccessDeniedException;
import com.acuityspace.mantle.exception.ResourceNotFoundException;
import com.acuityspace.mantle.web.dto.OrgResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrgServiceImpl implements OrgService {

    private final OrgRepository orgRepository;
    private final OrgMembershipRepository orgMembershipRepository;
    private final SubOrgRepository subOrgRepository;

    public OrgServiceImpl(OrgRepository orgRepository,
                          OrgMembershipRepository orgMembershipRepository,
                          SubOrgRepository subOrgRepository) {
        this.orgRepository = orgRepository;
        this.orgMembershipRepository = orgMembershipRepository;
        this.subOrgRepository = subOrgRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public OrgResponse getOrg(UUID orgId, UUID requestingUserId) {
        Org org = orgRepository.findById(orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Org not found: " + orgId));

        boolean isMember = orgMembershipRepository.existsById(
                new OrgMembership.OrgMembershipId(requestingUserId, orgId));
        if (!isMember) {
            throw new AccessDeniedException("User is not a member of this org");
        }

        long memberCount = orgMembershipRepository.countByOrg(org);
        long subOrgCount = subOrgRepository.findByOrg(org).size();

        return new OrgResponse(org.getId(), org.getName(), org.getType(), memberCount, subOrgCount);
    }
}
