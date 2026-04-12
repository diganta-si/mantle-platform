package com.acuityspace.mantle;

import com.acuityspace.mantle.config.TestContainersBase;
import com.acuityspace.mantle.domain.enums.OrgType;
import com.acuityspace.mantle.domain.model.App;
import com.acuityspace.mantle.domain.model.Org;
import com.acuityspace.mantle.domain.model.Project;
import com.acuityspace.mantle.domain.model.SubOrg;
import com.acuityspace.mantle.domain.repository.AppRepository;
import com.acuityspace.mantle.domain.repository.OrgRepository;
import com.acuityspace.mantle.domain.repository.ProjectRepository;
import com.acuityspace.mantle.domain.repository.SubOrgRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class ProjectAndAppTest extends TestContainersBase {

    @Autowired
    private OrgRepository orgRepository;

    @Autowired
    private SubOrgRepository subOrgRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AppRepository appRepository;

    @Test
    void saveProjectUnderSubOrg() {
        Org org = orgRepository.save(new Org("ProjOrg", OrgType.ENTERPRISE));
        SubOrg subOrg = subOrgRepository.save(new SubOrg(org, "ProjSubOrg", false, false));
        Project project = projectRepository.save(new Project(subOrg, "MyProject", "A project"));

        List<Project> found = projectRepository.findBySubOrg(subOrg);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getId()).isEqualTo(project.getId());
        assertThat(found.get(0).getName()).isEqualTo("MyProject");
    }

    @Test
    void saveAppUnderProject() {
        Org org = orgRepository.save(new Org("AppOrg", OrgType.ENTERPRISE));
        SubOrg subOrg = subOrgRepository.save(new SubOrg(org, "AppSubOrg", false, false));
        Project project = projectRepository.save(new Project(subOrg, "AppProject", null));
        App app = appRepository.save(new App(project, "MyApp", "An app"));

        List<App> found = appRepository.findByProject(project);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getId()).isEqualTo(app.getId());
        assertThat(found.get(0).getName()).isEqualTo("MyApp");
    }

    @Test
    void projectScopedToSubOrg() {
        Org org = orgRepository.save(new Org("ScopedOrg", OrgType.ENTERPRISE));
        SubOrg subOrgA = subOrgRepository.save(new SubOrg(org, "SubOrgA", false, false));
        SubOrg subOrgB = subOrgRepository.save(new SubOrg(org, "SubOrgB", false, false));

        Project projectA = projectRepository.save(new Project(subOrgA, "ProjectA", null));
        projectRepository.save(new Project(subOrgB, "ProjectB", null));

        List<Project> foundForA = projectRepository.findBySubOrg(subOrgA);

        assertThat(foundForA).hasSize(1);
        assertThat(foundForA.get(0).getId()).isEqualTo(projectA.getId());
    }
}
