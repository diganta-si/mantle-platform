import apiClient from './client'
import type { OrgResponse, InviteResponse } from '@/types/org'

export async function getOrg(orgId: string): Promise<OrgResponse> {
  const res = await apiClient.get<OrgResponse>(`/orgs/${orgId}`)
  return res.data
}

export async function getInvites(orgId: string): Promise<InviteResponse[]> {
  const res = await apiClient.get<InviteResponse[]>(`/orgs/${orgId}/invites`)
  return res.data
}

export async function sendInvite(orgId: string, email: string): Promise<InviteResponse> {
  const res = await apiClient.post<InviteResponse>(`/orgs/${orgId}/invites`, { email })
  return res.data
}

export async function revokeInvite(orgId: string, inviteId: string): Promise<void> {
  await apiClient.delete(`/orgs/${orgId}/invites/${inviteId}/revoke`)
}
