ALTER TABLE sub_orgs
    ADD CONSTRAINT uq_sub_org_name_per_org UNIQUE (org_id, name);
