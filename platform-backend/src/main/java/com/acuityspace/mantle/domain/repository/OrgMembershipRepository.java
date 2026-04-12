package com.acuityspace.mantle.domain.repository;

import com.acuityspace.mantle.domain.model.Org;
import com.acuityspace.mantle.domain.model.OrgMembership;
import com.acuityspace.mantle.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrgMembershipRepository extends JpaRepository<OrgMembership, OrgMembership.OrgMembershipId> {

    Optional<OrgMembership> findByUser(User user);

    List<OrgMembership> findByOrg(Org org);

    long countByOrg(Org org);
}
