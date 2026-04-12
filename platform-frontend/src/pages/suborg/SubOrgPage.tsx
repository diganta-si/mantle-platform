import { useState } from 'react'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { AppShell } from '@/components/layout/AppShell'
import { Breadcrumb } from '@/components/layout/Breadcrumb'
import { CreateModal } from '@/components/common/CreateModal'
import { ErrorMessage } from '@/components/common/ErrorMessage'
import { useBreadcrumb } from '@/hooks/useBreadcrumb'
import { useAuthStore } from '@/store/authStore'
import { getSubOrgs } from '@/api/suborg'
import { getProjects, createProject } from '@/api/project'
import type { ProjectResponse } from '@/types/project'

interface LocationState {
  subOrgName?: string
  orgName?: string
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

export function SubOrgPage() {
  const { subOrgId = '' } = useParams<{ subOrgId: string }>()
  const navigate = useNavigate()
  const location = useLocation()
  const state = (location.state ?? {}) as LocationState
  const user = useAuthStore((s) => s.user)!
  const queryClient = useQueryClient()

  const [showModal, setShowModal] = useState(false)
  const [modalError, setModalError] = useState<string | null>(null)
  const [isCreating, setIsCreating] = useState(false)

  // Fetch suborgs list to resolve the name when not in location.state
  const suborgsQuery = useQuery({
    queryKey: ['suborgs', user.orgId],
    queryFn: () => getSubOrgs(user.orgId),
  })

  const suborg = suborgsQuery.data?.find((s) => s.id === subOrgId)
  const subOrgName = state.subOrgName ?? suborg?.name ?? subOrgId
  const orgName = state.orgName ?? user.orgName

  const projectsQuery = useQuery({
    queryKey: ['projects', subOrgId],
    queryFn: () => getProjects(subOrgId),
    enabled: !!subOrgId,
  })

  const projects = projectsQuery.data ?? []

  const canCreateProject =
    user.role === 'ADMIN' || user.role === 'SUPER_ADMIN'

  useBreadcrumb([
    { label: orgName, href: '/dashboard' },
    { label: subOrgName },
  ])

  const breadcrumbItems = [
    { label: orgName, href: '/dashboard' },
    { label: subOrgName },
  ]

  async function handleCreateProject(data: { name: string; description?: string }) {
    setModalError(null)
    setIsCreating(true)
    try {
      await createProject(subOrgId, data)
      await queryClient.invalidateQueries({ queryKey: ['projects', subOrgId] })
      await queryClient.invalidateQueries({ queryKey: ['suborgs', user.orgId] })
      setShowModal(false)
    } catch (err: unknown) {
      setModalError(
        err instanceof Error ? err.message : 'Failed to create project'
      )
    } finally {
      setIsCreating(false)
    }
  }

  function handleProjectClick(project: ProjectResponse) {
    navigate(`/projects/${project.id}`, {
      state: {
        subOrgId,
        subOrgName,
        orgName,
        projectName: project.name,
      },
    })
  }

  return (
    <AppShell>
      {/* Page header */}
      <div className="border-b border-zinc-100 pb-4 mb-6">
        <Breadcrumb items={breadcrumbItems} />
        <div className="flex items-center justify-between mt-1">
          <h1 className="text-lg font-semibold text-zinc-900">{subOrgName}</h1>
          {canCreateProject && (
            <button
              onClick={() => setShowModal(true)}
              className="bg-zinc-900 text-white text-sm rounded-md px-3 h-8 hover:bg-zinc-800 transition-colors duration-150"
            >
              New Project
            </button>
          )}
        </div>
      </div>

      {/* Project list */}
      {projectsQuery.isLoading && <SkeletonRows />}

      {projectsQuery.isError && (
        <ErrorMessage message="Failed to load projects" />
      )}

      {!projectsQuery.isLoading && !projectsQuery.isError && projects.length === 0 && (
        <div className="border border-dashed border-zinc-200 rounded-lg py-10 text-center">
          <p className="text-sm text-zinc-400">No projects yet</p>
          <p className="text-xs text-zinc-400 mt-1">Create one to get started</p>
        </div>
      )}

      {!projectsQuery.isLoading && projects.length > 0 && (
        <div className="flex flex-col gap-2">
          {projects.map((project) => {
            const subtitle =
              project.appCount > 0
                ? `${project.appCount} apps`
                : project.description || `${project.appCount} apps`
            return (
              <button
                key={project.id}
                onClick={() => handleProjectClick(project)}
                className="border border-zinc-200 rounded-lg px-4 py-3 hover:bg-zinc-50 transition-colors duration-150 cursor-pointer flex justify-between items-center w-full text-left"
              >
                <div>
                  <p className="text-sm font-medium text-zinc-900">{project.name}</p>
                  <p className="text-xs text-zinc-400 mt-0.5">{subtitle}</p>
                </div>
                <span className="text-zinc-300 text-lg leading-none">›</span>
              </button>
            )
          })}
        </div>
      )}

      {showModal && (
        <CreateModal
          title="New Project"
          fieldLabel="Name"
          fieldPlaceholder="My Project"
          showDescription={true}
          onSubmit={handleCreateProject}
          onClose={() => { setShowModal(false); setModalError(null) }}
          isLoading={isCreating}
          error={modalError}
        />
      )}
    </AppShell>
  )
}
