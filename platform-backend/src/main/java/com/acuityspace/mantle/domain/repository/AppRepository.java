package com.acuityspace.mantle.domain.repository;

import com.acuityspace.mantle.domain.model.App;
import com.acuityspace.mantle.domain.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppRepository extends JpaRepository<App, UUID> {

    List<App> findByProject(Project project);
}
