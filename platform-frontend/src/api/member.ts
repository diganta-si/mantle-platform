import apiClient from './client'
import type { MemberResponse } from '@/types/org'

export async function getMembers(orgId: string): Promise<MemberResponse[]> {
  const res = await apiClient.get<MemberResponse[]>(`/orgs/${orgId}/members`)
  return res.data
}
