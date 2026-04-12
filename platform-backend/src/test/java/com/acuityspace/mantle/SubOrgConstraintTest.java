package com.acuityspace.mantle;

import com.acuityspace.mantle.config.TestContainersBase;
import com.acuityspace.mantle.domain.enums.OrgType;
import com.acuityspace.mantle.domain.model.Org;
import com.acuityspace.mantle.domain.model.SubOrg;
import com.acuityspace.mantle.domain.repository.OrgRepository;
import com.acuityspace.mantle.domain.repository.SubOrgRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubOrgConstraintTest extends TestContainersBase {

    @Autowired
    private OrgRepository orgRepository;

    @Autowired
    private SubOrgRepository subOrgRepository;

    @Test
    void onlyOneDefaultSubOrgPerOrg() {
        // No @Transactional here: constraint violations abort the PostgreSQL transaction,
        // so each save runs in its own committed transaction for a clean constraint test.
        Org org = orgRepository.save(new Org("DefaultOrg", OrgType.ENTERPRISE));
        subOrgRepository.saveAndFlush(new SubOrg(org, "Default1", true, false));

        assertThatThrownBy(() ->
                subOrgRepository.saveAndFlush(new SubOrg(org, "Default2", true, false))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void onlyOneGlobalSubOrgPerOrg() {
        // No @Transactional here: same reasoning as onlyOneDefaultSubOrgPerOrg.
        Org org = orgRepository.save(new Org("GlobalOrg", OrgType.ENTERPRISE));
        subOrgRepository.saveAndFlush(new SubOrg(org, "Global1", false, true));

        assertThatThrownBy(() ->
                subOrgRepository.saveAndFlush(new SubOrg(org, "Global2", false, true))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    void findByOrgAndIsDefaultTrue() {
        Org org = orgRepository.save(new Org("FindDefaultOrg", OrgType.INDIVIDUAL));
        SubOrg defaultSubOrg = subOrgRepository.save(new SubOrg(org, "DefaultSub", true, false));
        subOrgRepository.save(new SubOrg(org, "RegularSub", false, false));

        Optional<SubOrg> found = subOrgRepository.findByOrgAndIsDefaultTrue(org);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(defaultSubOrg.getId());
        assertThat(found.get().isDefault()).isTrue();
    }

    @Test
    @Transactional
    void findByOrgAndIsGlobalTrue() {
        Org org = orgRepository.save(new Org("FindGlobalOrg", OrgType.INDIVIDUAL));
        SubOrg globalSubOrg = subOrgRepository.save(new SubOrg(org, "GlobalSub", false, true));
        subOrgRepository.save(new SubOrg(org, "RegularSub", false, false));

        Optional<SubOrg> found = subOrgRepository.findByOrgAndIsGlobalTrue(org);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(globalSubOrg.getId());
        assertThat(found.get().isGlobal()).isTrue();
    }
}
