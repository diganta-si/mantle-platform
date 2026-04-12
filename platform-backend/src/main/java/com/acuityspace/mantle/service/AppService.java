package com.acuityspace.mantle.service;

import com.acuityspace.mantle.web.dto.AppRequest;
import com.acuityspace.mantle.web.dto.AppResponse;

import java.util.List;
import java.util.UUID;

public interface AppService {

    List<AppResponse> getApps(UUID projectId, UUID requestingUserId);

    AppResponse getApp(UUID appId, UUID requestingUserId);

    AppResponse createApp(UUID projectId, AppRequest request, UUID requestingUserId);

    AppResponse updateApp(UUID appId, AppRequest request, UUID requestingUserId);

    void deleteApp(UUID appId, UUID requestingUserId);
}
