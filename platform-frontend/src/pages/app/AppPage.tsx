import { useParams, useLocation } from 'react-router-dom'
import { AppShell } from '@/components/layout/AppShell'
import { Breadcrumb } from '@/components/layout/Breadcrumb'
import { useBreadcrumb } from '@/hooks/useBreadcrumb'
import { useAuthStore } from '@/store/authStore'

interface LocationState {
  projectId?:   string
  projectName?: string
  subOrgId?:    string
  subOrgName?:  string
  orgName?:     string
  appName?:     string
  description?: string
}

export function AppPage() {
  const { appId = '' } = useParams<{ appId: string }>()
  const location = useLocation()
  const state = (location.state ?? {}) as LocationState
  const user = useAuthStore((s) => s.user)!

  const orgName     = state.orgName     ?? user.orgName
  const subOrgName  = state.subOrgName  ?? 'SubOrg'
  const subOrgId    = state.subOrgId    ?? ''
  const projectName = state.projectName ?? 'Project'
  const projectId   = state.projectId   ?? ''
  const appName     = state.appName     ?? appId
  const description = state.description

  const breadcrumbItems = [
    { label: orgName,     href: '/dashboard' },
    { label: subOrgName,  href: subOrgId  ? `/suborgs/${subOrgId}`   : undefined },
    { label: projectName, href: projectId ? `/projects/${projectId}` : undefined },
    { label: appName },
  ]

  useBreadcrumb(breadcrumbItems)

  return (
    <AppShell>
      {/* Page header */}
      <div className="border-b border-zinc-100 pb-4 mb-6">
        <Breadcrumb items={breadcrumbItems} />
        <div className="mt-1">
          <h1 className="text-lg font-semibold text-zinc-900">{appName}</h1>
          {description && (
            <p className="text-sm text-zinc-500 mt-0.5">{description}</p>
          )}
        </div>
      </div>

      <p className="text-sm text-zinc-400">App workspace — coming soon</p>
    </AppShell>
  )
}
