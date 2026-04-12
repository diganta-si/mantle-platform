package com.acuityspace.mantle;

import com.acuityspace.mantle.config.TestContainersBase;
import com.acuityspace.mantle.domain.enums.InviteStatus;
import com.acuityspace.mantle.domain.enums.OrgType;
import com.acuityspace.mantle.exception.MemberLimitExceededException;
import com.acuityspace.mantle.service.AuthService;
import com.acuityspace.mantle.service.InviteService;
import com.acuityspace.mantle.web.dto.AuthResponse;
import com.acuityspace.mantle.web.dto.InviteRequest;
import com.acuityspace.mantle.web.dto.InviteResponse;
import com.acuityspace.mantle.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class InviteLimitTest extends TestContainersBase {

    @Autowired
    private AuthService authService;

    @Autowired
    private InviteService inviteService;

    @Test
    void individual_canInviteUpToThreeUsers() {
        AuthResponse owner = authService.register(new RegisterRequest(
                "ind-owner@limit.com", "password123", "IndOwner", "IndOrg", OrgType.INDIVIDUAL));

        for (int i = 1; i <= 3; i++) {
            InviteResponse resp = inviteService.sendInvite(
                    owner.orgId(),
                    new InviteRequest("invited" + i + "@limit.com"),
                    owner.userId());
            assertThat(resp.status()).isEqualTo(InviteStatus.PENDING);
        }
    }

    @Test
    void individual_fourthInvite_throwsMemberLimitExceededException() {
        AuthResponse owner = authService.register(new RegisterRequest(
                "ind-owner2@limit.com", "password123", "IndOwner2", "IndOrg2", OrgType.INDIVIDUAL));

        for (int i = 1; i <= 3; i++) {
            inviteService.sendInvite(
                    owner.orgId(),
                    new InviteRequest("slot" + i + "@limit.com"),
                    owner.userId());
        }

        assertThatThrownBy(() -> inviteService.sendInvite(
                owner.orgId(),
                new InviteRequest("slot4@limit.com"),
                owner.userId()))
                .isInstanceOf(MemberLimitExceededException.class);
    }

    @Test
    void enterprise_canInviteUnlimitedUsers() {
        AuthResponse owner = authService.register(new RegisterRequest(
                "ent-owner@limit.com", "password123", "EntOwner", "EntOrg", OrgType.ENTERPRISE));

        for (int i = 1; i <= 5; i++) {
            InviteResponse resp = inviteService.sendInvite(
                    owner.orgId(),
                    new InviteRequest("ent-user" + i + "@limit.com"),
                    owner.userId());
            assertThat(resp.status()).isEqualTo(InviteStatus.PENDING);
        }
    }
}
