package com.acuityspace.mantle;

import com.acuityspace.mantle.config.TestContainersBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MigrationIntegrityTest extends TestContainersBase {

    @Autowired
    private DataSource dataSource;

    @Test
    void verifyAllTablesExist() throws Exception {
        List<String> tables = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT table_name FROM information_schema.tables " +
                     "WHERE table_schema = 'public' AND table_type = 'BASE TABLE'");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tables.add(rs.getString("table_name"));
            }
        }
        assertThat(tables).contains(
                "users",
                "orgs",
                "sub_orgs",
                "org_memberships",
                "sub_org_memberships",
                "projects",
                "apps"
        );
    }

    @Test
    void verifyPartialUniqueIndexesExist() throws Exception {
        List<String> indexes = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT indexname FROM pg_indexes WHERE schemaname = 'public'");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                indexes.add(rs.getString("indexname"));
            }
        }
        assertThat(indexes).contains(
                "uq_sub_orgs_one_default_per_org",
                "uq_sub_orgs_one_global_per_org"
        );
    }
}
