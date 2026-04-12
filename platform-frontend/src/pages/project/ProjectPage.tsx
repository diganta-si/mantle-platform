import { useState } from 'react'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { AppShell } from '@/components/layout/AppShell'
import { Breadcrumb } from '@/components/layout/Breadcrumb'
import { CreateModal } from '@/components/common/CreateModal'
import { ErrorMessage } from '@/components/common/ErrorMessage'
import { useBreadcrumb } from '@/hooks/useBreadcrumb'
import { useAuthStore } from '@/store/authStore'
import { getProjects } from '@/api/project'
import { getApps, createApp } from '@/api/app'
import type { AppResponse } from '@/types/app'

interface LocationState {
  subOrgId?:    string
  subOrgName?:  string
  orgName?:     string
  projectName?: string
}

function SkeletonRows() {
  return (
    <div className="flex flex-col gap-2">
      {[0, 1, 2].map((i) => (
        <div key={i} className="bg-zinc-100 rounded-lg h-14 animate-pulse" />
      ))}
    </div>
  )
}

export function ProjectPage() {
  const { projectId = '' } = useParams<{ projectId: string }>()
  const navigate = useNavigate()
  const location = useLocation()
  const state = (location.state ?? {}) as LocationState
  const user = useAuthStore((s) => s.user)!
  const queryClient = useQueryClient()

  const [showModal, setShowModal] = useState(false)
  const [modalError, setModalError] = useState<string | null>(null)
  const [isCreating, setIsCreating] = useState(false)

  const subOrgId    = state.subOrgId    ?? ''
  const subOrgName  = state.subOrgName  ?? 'SubOrg'
  const orgName     = state.orgName     ?? user.orgName

  // Resolve project name from cached projects list if not in state
  const projectsQuery = useQuery({
    queryKey: ['projects', subOrgId],
    queryFn: () => getProjects(subOrgId),
    enabled: !!subOrgId && !state.projectName,
  })
  const projectName =
    state.projectName ??
    projectsQuery.data?.find((p) => p.id === projectId)?.name ??
    projectId

  const appsQuery = useQuery({
    queryKey: ['apps', projectId],
    queryFn: () => getApps(projectId),
    enabled: !!projectId,
  })

  const apps = appsQuery.data ?? []

  const canCreateApp =
    user.role === 'ADMIN' || user.role === 'SUPER_ADMIN'

  useBreadcrumb([
    { label: orgName,    href: '/dashboard' },
    { label: subOrgName, href: subOrgId ? `/suborgs/${subOrgId}` : undefined },
    { label: projectName },
  ])

  const breadcrumbItems = [
    { label: orgName,    href: '/dashboard' },
    { label: subOrgName, href: subOrgId ? `/suborgs/${subOrgId}` : undefined },
    { label: projectName },
  ]

  async function handleCreateApp(data: { name: string; description?: string }) {
    setModalError(null)
    setIsCreating(true)
    try {
      await createApp(projectId, data)
      await queryClient.invalidateQueries({ queryKey: ['apps', projectId] })
      await queryClient.invalidateQueries({ queryKey: ['projects', subOrgId] })
      setShowModal(false)
    } catch (err: unknown) {
      setModalError(
        err instanceof Error ? err.message : 'Failed to create app'
      )
    } finally {
      setIsCreating(false)
    }
  }

  function handleAppClick(app: AppResponse) {
    navigate(`/apps/${app.id}`, {
      state: {
        projectId,
        projectName,
        subOrgId,
        subOrgName,
        orgName,
        appName:     app.name,
        description: app.description,
      },
    })
  }

  return (
    <AppShell>
      {/* Page header */}
      <div className="border-b border-zinc-100 pb-4 mb-6">
        <Breadcrumb items={breadcrumbItems} />
        <div className="flex items-center justify-between mt-1">
          <h1 className="text-lg font-semibold text-zinc-900">{projectName}</h1>
          {canCreateApp && (
            <button
              onClick={() => setShowModal(true)}
              className="bg-zinc-900 text-white text-sm rounded-md px-3 h-8 hover:bg-zinc-800 transition-colors duration-150"
            >
              New App
            </button>
          )}
        </div>
      </div>

      {/* App list */}
      {appsQuery.isLoading && <SkeletonRows />}

      {appsQuery.isError && (
        <ErrorMessage message="Failed to load apps" />
      )}

      {!appsQuery.isLoading && !appsQuery.isError && apps.length === 0 && (
        <div className="border border-dashed border-zinc-200 rounded-lg py-10 text-center">
          <p className="text-sm text-zinc-400">No apps yet</p>
          <p className="text-xs text-zinc-400 mt-1">Create one to get started</p>
        </div>
      )}

      {!appsQuery.isLoading && apps.length > 0 && (
        <div className="flex flex-col gap-2">
          {apps.map((app) => (
            <button
              key={app.id}
              onClick={() => handleAppClick(app)}
              className="border border-zinc-200 rounded-lg px-4 py-3 hover:bg-zinc-50 transition-colors duration-150 cursor-pointer flex justify-between items-center w-full text-left"
            >
              <div>
                <p className="text-sm font-medium text-zinc-900">{app.name}</p>
                <p className="text-xs text-zinc-400 mt-0.5">
                  {app.description || (
                    <span className="text-zinc-300">—</span>
                  )}
                </p>
              </div>
              <span className="text-zinc-300 text-lg leading-none">›</span>
            </button>
          ))}
        </div>
      )}

      {showModal && (
        <CreateModal
          title="New App"
          fieldLabel="Name"
          fieldPlaceholder="My App"
          showDescription={true}
          onSubmit={handleCreateApp}
          onClose={() => { setShowModal(false); setModalError(null) }}
          isLoading={isCreating}
          error={modalError}
        />
      )}
    </AppShell>
  )
}
