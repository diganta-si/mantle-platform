import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { AppShell } from '@/components/layout/AppShell'
import { CreateModal } from '@/components/common/CreateModal'
import { ErrorMessage } from '@/components/common/ErrorMessage'
import { useAuthStore } from '@/store/authStore'
import { getOrg } from '@/api/org'
import { getSubOrgs, createSubOrg } from '@/api/suborg'
import type { SubOrgResponse } from '@/types/org'

function SkeletonRows() {
  return (
    <div className="flex flex-col gap-2">
      {[0, 1, 2].map((i) => (
        <div key={i} className="bg-zinc-100 rounded-lg h-14 animate-pulse" />
      ))}
    </div>
  )
}

export function DashboardPage() {
  const user = useAuthStore((s) => s.user)!
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [showModal, setShowModal] = useState(false)
  const [modalError, setModalError] = useState<string | null>(null)
  const [isCreating, setIsCreating] = useState(false)

  const orgQuery = useQuery({
    queryKey: ['org', user.orgId],
    queryFn: () => getOrg(user.orgId),
  })

  const suborgsQuery = useQuery({
    queryKey: ['suborgs', user.orgId],
    queryFn: () => getSubOrgs(user.orgId),
  })

  const org = orgQuery.data
  const suborgs = suborgsQuery.data ?? []

  const canCreateSubOrg =
    org?.type === 'ENTERPRISE' &&
    (user.role === 'SUPER_ADMIN' || user.role === 'ADMIN')

  async function handleCreateSubOrg(data: { name: string }) {
    setModalError(null)
    setIsCreating(true)
    try {
      await createSubOrg(user.orgId, { name: data.name })
      await queryClient.invalidateQueries({ queryKey: ['suborgs', user.orgId] })
      await queryClient.invalidateQueries({ queryKey: ['org', user.orgId] })
      setShowModal(false)
    } catch (err: unknown) {
      setModalError(
        err instanceof Error ? err.message : 'Failed to create SubOrg'
      )
    } finally {
      setIsCreating(false)
    }
  }

  function handleSubOrgClick(suborg: SubOrgResponse) {
    navigate(`/suborgs/${suborg.id}`, {
      state: { subOrgName: suborg.name, orgName: user.orgName },
    })
  }

  const orgTypeLabel = org?.type === 'ENTERPRISE' ? 'Enterprise' : 'Individual'

  return (
    <AppShell>
      {/* Page header */}
      <div className="border-b border-zinc-100 pb-4 mb-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-lg font-semibold text-zinc-900">{user.orgName}</h1>
            <p className="text-xs text-zinc-400 uppercase tracking-wide mt-0.5">
              {orgTypeLabel}
            </p>
          </div>
          {canCreateSubOrg && (
            <button
              onClick={() => setShowModal(true)}
              className="bg-zinc-900 text-white text-sm rounded-md px-3 h-8 hover:bg-zinc-800 transition-colors duration-150"
            >
              New SubOrg
            </button>
          )}
        </div>
      </div>

      {/* Org stat chips */}
      {org && (
        <div className="flex gap-2 mb-6">
          <span className="text-xs text-zinc-500 border border-zinc-100 rounded-md px-3 py-1.5 bg-zinc-50">
            Members: {org.memberCount}
          </span>
          <span className="text-xs text-zinc-500 border border-zinc-100 rounded-md px-3 py-1.5 bg-zinc-50">
            SubOrgs: {org.subOrgCount}
          </span>
        </div>
      )}

      {/* SubOrg list */}
      {suborgsQuery.isLoading && <SkeletonRows />}

      {suborgsQuery.isError && (
        <ErrorMessage message="Failed to load SubOrgs" />
      )}

      {!suborgsQuery.isLoading && !suborgsQuery.isError && suborgs.length === 0 && (
        <div className="border border-dashed border-zinc-200 rounded-lg py-10 text-center">
          <p className="text-sm text-zinc-400">No SubOrgs yet</p>
          <p className="text-xs text-zinc-400 mt-1">Create one to get started</p>
        </div>
      )}

      {!suborgsQuery.isLoading && suborgs.length > 0 && (
        <div className="flex flex-col gap-2">
          {suborgs.map((suborg) => (
            <button
              key={suborg.id}
              onClick={() => handleSubOrgClick(suborg)}
              className="border border-zinc-200 rounded-lg px-4 py-3 hover:bg-zinc-50 transition-colors duration-150 cursor-pointer flex justify-between items-center w-full text-left"
            >
              <div>
                <div className="flex items-center gap-2">
                  <span className="text-sm font-medium text-zinc-900">
                    {suborg.name}
                  </span>
                  {suborg.isDefault && (
                    <span className="text-xs text-zinc-400 border border-zinc-100 rounded px-1.5 py-0.5">
                      default
                    </span>
                  )}
                  {suborg.isGlobal && (
                    <span className="text-xs text-zinc-400 border border-zinc-100 rounded px-1.5 py-0.5">
                      global
                    </span>
                  )}
                </div>
                <p className="text-xs text-zinc-400 mt-0.5">
                  {suborg.projectCount} projects
                </p>
              </div>
              <span className="text-zinc-300 text-lg leading-none">›</span>
            </button>
          ))}
        </div>
      )}

      {showModal && (
        <CreateModal
          title="New SubOrg"
          fieldLabel="Name"
          fieldPlaceholder="Engineering"
          showDescription={false}
          onSubmit={handleCreateSubOrg}
          onClose={() => { setShowModal(false); setModalError(null) }}
          isLoading={isCreating}
          error={modalError}
        />
      )}
    </AppShell>
  )
}
