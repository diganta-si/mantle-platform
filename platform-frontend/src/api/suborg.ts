import apiClient from './client'
import type { SubOrgRequest, SubOrgResponse } from '@/types/org'

export async function getSubOrgs(orgId: string): Promise<SubOrgResponse[]> {
  const res = await apiClient.get<SubOrgResponse[]>(`/orgs/${orgId}/suborgs`)
  return res.data
}

export async function createSubOrg(
  orgId: string,
  data: SubOrgRequest
): Promise<SubOrgResponse> {
  const res = await apiClient.post<SubOrgResponse>(`/orgs/${orgId}/suborgs`, data)
  return res.data
}
