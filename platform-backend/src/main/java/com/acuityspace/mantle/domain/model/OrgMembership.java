package com.acuityspace.mantle.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
    name = "org_memberships",
    indexes = {
        @Index(name = "idx_org_membership_user", columnList = "user_id"),
        @Index(name = "idx_org_membership_org", columnList = "org_id")
    }
)
public class OrgMembership {

    @EmbeddedId
    private OrgMembershipId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("orgId")
    @JoinColumn(name = "org_id", insertable = false, updatable = false)
    private Org org;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public OrgMembership() {
    }

    public OrgMembership(User user, Org org) {
        this.id = new OrgMembershipId(user.getId(), org.getId());
        this.user = user;
        this.org = org;
    }

    public OrgMembershipId getId() {
        return id;
    }

    public void setId(OrgMembershipId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Org getOrg() {
        return org;
    }

    public void setOrg(Org org) {
        this.org = org;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Embeddable
    public static class OrgMembershipId implements Serializable {

        @Column(name = "user_id")
        private UUID userId;

        @Column(name = "org_id")
        private UUID orgId;

        public OrgMembershipId() {
        }

        public OrgMembershipId(UUID userId, UUID orgId) {
            this.userId = userId;
            this.orgId = orgId;
        }

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        public UUID getOrgId() {
            return orgId;
        }

        public void setOrgId(UUID orgId) {
            this.orgId = orgId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OrgMembershipId that)) return false;
            return Objects.equals(userId, that.userId) && Objects.equals(orgId, that.orgId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, orgId);
        }
    }
}
