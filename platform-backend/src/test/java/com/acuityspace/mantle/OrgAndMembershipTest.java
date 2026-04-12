package com.acuityspace.mantle;

import com.acuityspace.mantle.config.TestContainersBase;
import com.acuityspace.mantle.domain.enums.OrgType;
import com.acuityspace.mantle.domain.model.Org;
import com.acuityspace.mantle.domain.model.OrgMembership;
import com.acuityspace.mantle.domain.model.User;
import com.acuityspace.mantle.domain.repository.OrgMembershipRepository;
import com.acuityspace.mantle.domain.repository.OrgRepository;
import com.acuityspace.mantle.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class OrgAndMembershipTest extends TestContainersBase {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrgRepository orgRepository;

    @Autowired
    private OrgMembershipRepository orgMembershipRepository;

    @Test
    void saveOrgAndFindById() {
        Org org = new Org("Acme Corp", OrgType.ENTERPRISE);
        Org saved = orgRepository.save(org);

        Optional<Org> found = orgRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Acme Corp");
        assertThat(found.get().getType()).isEqualTo(OrgType.ENTERPRISE);
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void saveOrgMembership() {
        User user = userRepository.save(new User("member@example.com", "pw", "Member"));
        Org org = orgRepository.save(new Org("OrgA", OrgType.INDIVIDUAL));
        OrgMembership membership = new OrgMembership(user, org);
        orgMembershipRepository.save(membership);

        Optional<OrgMembership> found = orgMembershipRepository.findByUser(user);

        assertThat(found).isPresent();
        assertThat(found.get().getId().getUserId()).isEqualTo(user.getId());
        assertThat(found.get().getId().getOrgId()).isEqualTo(org.getId());
    }

    @Test
    void countByOrg() {
        Org org = orgRepository.save(new Org("CountOrg", OrgType.ENTERPRISE));
        User user1 = userRepository.save(new User("u1@example.com", "pw", "User1"));
        User user2 = userRepository.save(new User("u2@example.com", "pw", "User2"));
        orgMembershipRepository.save(new OrgMembership(user1, org));
        orgMembershipRepository.save(new OrgMembership(user2, org));

        long count = orgMembershipRepository.countByOrg(org);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void oneOrgPerUser() {
        // Business rule: one org per user is enforced at the service layer, NOT the DB level.
        // At the DB level, a user CAN have memberships in multiple orgs (composite PK allows it).
        // This test documents that the DB permits it — the constraint must be enforced in the service.
        User user = userRepository.save(new User("multi@example.com", "pw", "MultiOrg"));
        Org orgA = orgRepository.save(new Org("OrgA", OrgType.ENTERPRISE));
        Org orgB = orgRepository.save(new Org("OrgB", OrgType.INDIVIDUAL));

        orgMembershipRepository.save(new OrgMembership(user, orgA));
        orgMembershipRepository.save(new OrgMembership(user, orgB));
        orgMembershipRepository.flush();

        List<OrgMembership> memberships = orgMembershipRepository.findAll().stream()
                .filter(m -> m.getId().getUserId().equals(user.getId()))
                .toList();

        // Both memberships can coexist at the DB level
        assertThat(memberships).hasSize(2);
    }
}
