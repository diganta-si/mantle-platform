export interface ProjectRequest {
  name:         string
  description?: string
}

export interface ProjectResponse {
  id:          string
  subOrgId:    string
  name:        string
  description: string
  appCount:    number
  createdAt:   string
}
