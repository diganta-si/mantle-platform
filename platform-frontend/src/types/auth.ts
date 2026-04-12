export type OrgType = 'INDIVIDUAL' | 'ENTERPRISE'
export type UserRole = 'STANDARD' | 'ADMIN' | 'SUPER_ADMIN'

export interface AuthResponse {
  userId:   string
  email:    string
  name:     string
  orgId:    string
  orgName:  string
  orgType:  OrgType
  subOrgId: string
  role:     UserRole
}

export interface RegisterRequest {
  email:    string
  password: string
  name:     string
  orgName:  string
  orgType:  OrgType
}

export interface LoginRequest {
  email:    string
  password: string
}
