package com.acuityspace.mantle.domain.repository;

import com.acuityspace.mantle.domain.model.Org;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrgRepository extends JpaRepository<Org, UUID> {
}
