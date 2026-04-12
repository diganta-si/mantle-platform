import apiClient from './client'
import type { InviteDetailResponse } from '@/types/org'
import type { AuthResponse } from '@/types/auth'

export interface AcceptInviteRequest {
  email:    string
  name:     string
  password: string
}

export async function getInviteDetail(inviteId: string): Promise<InviteDetailResponse> {
  const res = await apiClient.get<InviteDetailResponse>(`/auth/invite/${inviteId}`)
  return res.data
}

export async function acceptInvite(
  orgId: string,
  inviteId: string,
  data: AcceptInviteRequest,
): Promise<AuthResponse> {
  const res = await apiClient.post<AuthResponse>(
    `/orgs/${orgId}/invites/${inviteId}/accept`,
    data,
  )
  return res.data
}
