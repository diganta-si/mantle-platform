import type { OrgType, UserRole } from './auth'

export interface OrgResponse {
  id:          string
  name:        string
  type:        OrgType
  memberCount: number
  subOrgCount: number
}

export interface SubOrgRequest {
  name: string
}

export interface SubOrgResponse {
  id:           string
  orgId:        string
  name:         string
  isDefault:    boolean
  isGlobal:     boolean
  projectCount: number
}

export type InviteStatus = 'PENDING' | 'ACCEPTED' | 'REVOKED'

export interface InviteResponse {
  id:            string
  orgId:         string
  email:         string
  status:        InviteStatus
  invitedByName: string
  createdAt:     string
}

export interface MemberResponse {
  userId:   string
  name:     string
  email:    string
  role:     UserRole
  joinedAt: string
}

export interface InviteDetailResponse {
  inviteId: string
  orgId:    string
  orgName:  string
  email:    string
  status:   InviteStatus
}
