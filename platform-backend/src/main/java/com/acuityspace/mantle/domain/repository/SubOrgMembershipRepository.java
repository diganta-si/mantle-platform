package com.acuityspace.mantle.domain.repository;

import com.acuityspace.mantle.domain.model.SubOrg;
import com.acuityspace.mantle.domain.model.SubOrgMembership;
import com.acuityspace.mantle.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubOrgMembershipRepository extends JpaRepository<SubOrgMembership, SubOrgMembership.SubOrgMembershipId> {

    List<SubOrgMembership> findByUser(User user);

    List<SubOrgMembership> findBySubOrg(SubOrg subOrg);

    Optional<SubOrgMembership> findByUserAndSubOrg(User user, SubOrg subOrg);
}
