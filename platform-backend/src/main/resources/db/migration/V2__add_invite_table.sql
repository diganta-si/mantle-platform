CREATE TABLE invites (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    org_id           UUID NOT NULL REFERENCES orgs(id) ON DELETE CASCADE,
    invited_by       UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    email            VARCHAR(255) NOT NULL,
    status           VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_invite_org_email UNIQUE (org_id, email)
);

CREATE INDEX idx_invite_org ON invites(org_id);
CREATE INDEX idx_invite_email ON invites(email);
