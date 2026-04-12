import apiClient from './client'
import type { ProjectRequest, ProjectResponse } from '@/types/project'

export async function getProjects(subOrgId: string): Promise<ProjectResponse[]> {
  const res = await apiClient.get<ProjectResponse[]>(`/suborgs/${subOrgId}/projects`)
  return res.data
}

export async function getProject(
  subOrgId: string,
  projectId: string
): Promise<ProjectResponse> {
  const res = await apiClient.get<ProjectResponse>(
    `/suborgs/${subOrgId}/projects/${projectId}`
  )
  return res.data
}

export async function createProject(
  subOrgId: string,
  data: ProjectRequest
): Promise<ProjectResponse> {
  const res = await apiClient.post<ProjectResponse>(
    `/suborgs/${subOrgId}/projects`,
    data
  )
  return res.data
}
