package com.acuityspace.mantle;

import com.acuityspace.mantle.config.TestContainersBase;
import com.acuityspace.mantle.domain.enums.OrgType;
import com.acuityspace.mantle.domain.enums.UserRole;
import com.acuityspace.mantle.domain.model.Org;
import com.acuityspace.mantle.domain.model.SubOrg;
import com.acuityspace.mantle.domain.model.SubOrgMembership;
import com.acuityspace.mantle.domain.model.User;
import com.acuityspace.mantle.domain.repository.OrgRepository;
import com.acuityspace.mantle.domain.repository.SubOrgMembershipRepository;
import com.acuityspace.mantle.domain.repository.SubOrgRepository;
import com.acuityspace.mantle.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class SubOrgMembershipRoleTest extends TestContainersBase {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrgRepository orgRepository;

    @Autowired
    private SubOrgRepository subOrgRepository;

    @Autowired
    private SubOrgMembershipRepository subOrgMembershipRepository;

    @Test
    void assignRoleToUser() {
        User user = userRepository.save(new User("role@example.com", "pw", "RoleUser"));
        Org org = orgRepository.save(new Org("RoleOrg", OrgType.ENTERPRISE));
        SubOrg subOrg = subOrgRepository.save(new SubOrg(org, "RoleSubOrg", false, false));
        subOrgMembershipRepository.save(new SubOrgMembership(user, subOrg, UserRole.ADMIN));

        Optional<SubOrgMembership> found = subOrgMembershipRepository.findByUserAndSubOrg(user, subOrg);

        assertThat(found).isPresent();
        assertThat(found.get().getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void userCanHaveDifferentRolesAcrossSubOrgs() {
        User user = userRepository.save(new User("multirole@example.com", "pw", "MultiRole"));
        Org org = orgRepository.save(new Org("MultiRoleOrg", OrgType.ENTERPRISE));
        SubOrg subOrgA = subOrgRepository.save(new SubOrg(org, "SubOrgA", false, false));
        SubOrg subOrgB = subOrgRepository.save(new SubOrg(org, "SubOrgB", false, false));

        subOrgMembershipRepository.save(new SubOrgMembership(user, subOrgA, UserRole.ADMIN));
        subOrgMembershipRepository.save(new SubOrgMembership(user, subOrgB, UserRole.STANDARD));

        Optional<SubOrgMembership> membershipA = subOrgMembershipRepository.findByUserAndSubOrg(user, subOrgA);
        Optional<SubOrgMembership> membershipB = subOrgMembershipRepository.findByUserAndSubOrg(user, subOrgB);

        assertThat(membershipA).isPresent();
        assertThat(membershipA.get().getRole()).isEqualTo(UserRole.ADMIN);

        assertThat(membershipB).isPresent();
        assertThat(membershipB.get().getRole()).isEqualTo(UserRole.STANDARD);
    }
}
