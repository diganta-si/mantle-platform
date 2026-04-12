import apiClient from './client'
import type { AppRequest, AppResponse } from '@/types/app'

export async function getApps(projectId: string): Promise<AppResponse[]> {
  const res = await apiClient.get<AppResponse[]>(`/projects/${projectId}/apps`)
  return res.data
}

export async function getApp(
  projectId: string,
  appId: string
): Promise<AppResponse> {
  const res = await apiClient.get<AppResponse>(
    `/projects/${projectId}/apps/${appId}`
  )
  return res.data
}

export async function createApp(
  projectId: string,
  data: AppRequest
): Promise<AppResponse> {
  const res = await apiClient.post<AppResponse>(
    `/projects/${projectId}/apps`,
    data
  )
  return res.data
}
