package com.acuityspace.mantle.domain.repository;

import com.acuityspace.mantle.domain.enums.InviteStatus;
import com.acuityspace.mantle.domain.model.Invite;
import com.acuityspace.mantle.domain.model.Org;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InviteRepository extends JpaRepository<Invite, UUID> {

    Optional<Invite> findByOrgAndEmail(Org org, String email);

    List<Invite> findByOrg(Org org);

    long countByOrgAndStatus(Org org, InviteStatus status);

    boolean existsByEmail(String email);
}
