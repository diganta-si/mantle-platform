ALTER TABLE projects
    ADD CONSTRAINT uq_project_name_per_suborg UNIQUE (sub_org_id, name);

ALTER TABLE apps
    ADD CONSTRAINT uq_app_name_per_project UNIQUE (project_id, name);
