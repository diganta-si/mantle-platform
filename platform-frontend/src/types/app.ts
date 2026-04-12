export interface AppRequest {
  name:         string
  description?: string
}

export interface AppResponse {
  id:          string
  projectId:   string
  name:        string
  description: string
  createdAt:   string
}
