package com.acuityspace.mantle.domain.model;

import com.acuityspace.mantle.domain.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    name = "sub_org_memberships",
    indexes = {
        @Index(name = "idx_sub_org_membership_user", columnList = "user_id"),
        @Index(name = "idx_sub_org_membership_sub_org", columnList = "sub_org_id")
    }
)
public class SubOrgMembership {

    @EmbeddedId
    private SubOrgMembershipId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("subOrgId")
    @JoinColumn(name = "sub_org_id", insertable = false, updatable = false)
    private SubOrg subOrg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public SubOrgMembership() {
    }

    public SubOrgMembership(User user, SubOrg subOrg, UserRole role) {
        this.id = new SubOrgMembershipId(user.getId(), subOrg.getId());
        this.user = user;
        this.subOrg = subOrg;
        this.role = role;
    }

    public SubOrgMembershipId getId() {
        return id;
    }

    public void setId(SubOrgMembershipId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public SubOrg getSubOrg() {
        return subOrg;
    }

    public void setSubOrg(SubOrg subOrg) {
        this.subOrg = subOrg;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Embeddable
    public static class SubOrgMembershipId implements Serializable {

        @Column(name = "user_id")
        private UUID userId;

        @Column(name = "sub_org_id")
        private UUID subOrgId;

        public SubOrgMembershipId() {
        }

        public SubOrgMembershipId(UUID userId, UUID subOrgId) {
            this.userId = userId;
            this.subOrgId = subOrgId;
        }

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        public UUID getSubOrgId() {
            return subOrgId;
        }

        public void setSubOrgId(UUID subOrgId) {
            this.subOrgId = subOrgId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SubOrgMembershipId that)) return false;
            return Objects.equals(userId, that.userId) && Objects.equals(subOrgId, that.subOrgId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, subOrgId);
        }
    }
}
