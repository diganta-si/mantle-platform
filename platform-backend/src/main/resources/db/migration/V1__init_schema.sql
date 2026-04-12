CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- 1. users
CREATE TABLE users (
    id            UUID                     NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    email         VARCHAR(255)             NOT NULL UNIQUE,
    password_hash VARCHAR(255)             NOT NULL,
    name          VARCHAR(255)             NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users (email);

-- 2. orgs
CREATE TABLE orgs (
    id         UUID                     NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    name       VARCHAR(255)             NOT NULL,
    type       VARCHAR(50)              NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- 3. sub_orgs
CREATE TABLE sub_orgs (
    id         UUID                     NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    org_id     UUID                     NOT NULL REFERENCES orgs (id) ON DELETE RESTRICT,
    name       VARCHAR(255)             NOT NULL,
    is_default BOOLEAN                  NOT NULL DEFAULT false,
    is_global  BOOLEAN                  NOT NULL DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Partial unique indexes: only one default and one global sub-org per org
CREATE UNIQUE INDEX uq_sub_orgs_one_default_per_org
    ON sub_orgs (org_id) WHERE is_default = true;

CREATE UNIQUE INDEX uq_sub_orgs_one_global_per_org
    ON sub_orgs (org_id) WHERE is_global = true;

-- 4. org_memberships
CREATE TABLE org_memberships (
    user_id    UUID                     NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    org_id     UUID                     NOT NULL REFERENCES orgs (id) ON DELETE RESTRICT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, org_id)
);

CREATE INDEX idx_org_membership_user ON org_memberships (user_id);
CREATE INDEX idx_org_membership_org  ON org_memberships (org_id);

-- 5. sub_org_memberships
CREATE TABLE sub_org_memberships (
    user_id    UUID                     NOT NULL REFERENCES users (id) ON DELETE RESTRICT,
    sub_org_id UUID                     NOT NULL REFERENCES sub_orgs (id) ON DELETE RESTRICT,
    role       VARCHAR(50)              NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, sub_org_id)
);

CREATE INDEX idx_sub_org_membership_user    ON sub_org_memberships (user_id);
CREATE INDEX idx_sub_org_membership_sub_org ON sub_org_memberships (sub_org_id);

-- 6. projects
CREATE TABLE projects (
    id          UUID                     NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    sub_org_id  UUID                     NOT NULL REFERENCES sub_orgs (id) ON DELETE RESTRICT,
    name        VARCHAR(255)             NOT NULL,
    description VARCHAR(1000),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_project_sub_org ON projects (sub_org_id);

-- 7. apps
CREATE TABLE apps (
    id          UUID                     NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    project_id  UUID                     NOT NULL REFERENCES projects (id) ON DELETE RESTRICT,
    name        VARCHAR(255)             NOT NULL,
    description VARCHAR(1000),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_app_project ON apps (project_id);
