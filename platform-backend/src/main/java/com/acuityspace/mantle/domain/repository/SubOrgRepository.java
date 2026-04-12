package com.acuityspace.mantle.domain.repository;

import com.acuityspace.mantle.domain.model.Org;
import com.acuityspace.mantle.domain.model.SubOrg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubOrgRepository extends JpaRepository<SubOrg, UUID> {

    List<SubOrg> findByOrg(Org org);

    Optional<SubOrg> findByOrgAndIsDefaultTrue(Org org);

    Optional<SubOrg> findByOrgAndIsGlobalTrue(Org org);
}
