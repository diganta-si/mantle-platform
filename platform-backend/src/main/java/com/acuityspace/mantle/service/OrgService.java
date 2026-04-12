package com.acuityspace.mantle.service;

import com.acuityspace.mantle.web.dto.OrgResponse;

import java.util.UUID;

public interface OrgService {

    OrgResponse getOrg(UUID orgId, UUID requestingUserId);
}
