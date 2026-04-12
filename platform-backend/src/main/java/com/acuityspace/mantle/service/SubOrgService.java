package com.acuityspace.mantle.service;

import com.acuityspace.mantle.web.dto.SubOrgRequest;
import com.acuityspace.mantle.web.dto.SubOrgResponse;

import java.util.List;
import java.util.UUID;

public interface SubOrgService {

    List<SubOrgResponse> getSubOrgsForOrg(UUID orgId, UUID requestingUserId);

    SubOrgResponse createSubOrg(UUID orgId, SubOrgRequest request, UUID requestingUserId);

    void deleteSubOrg(UUID subOrgId, UUID requestingUserId);
}
