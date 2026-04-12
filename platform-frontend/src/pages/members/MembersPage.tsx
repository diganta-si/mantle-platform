import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { AppShell } from '@/components/layout/AppShell'
import { Breadcrumb } from '@/components/layout/Breadcrumb'
import { ErrorMessage } from '@/components/common/ErrorMessage'
import { LoadingSpinner } from '@/components/common/LoadingSpinner'
import { useAuthStore } from '@/store/authStore'
import { getMembers } from '@/api/member'
import { getInvites, sendInvite, revokeInvite } from '@/api/org'
import type { UserRole } from '@/types/auth'
import type { MemberResponse, InviteResponse } from '@/types/org'

const inviteSchema = z.object({
  email: z.string().email('Invalid email'),
})
type InviteForm = z.infer<typeof inviteSchema>

function RoleBadge({ role }: { role: UserRole }) {
  const classes: Record<UserRole, string> = {
    SUPER_ADMIN: 'bg-zinc-900 text-white',
    ADMIN:       'bg-zinc-100 text-zinc-700',
    STANDARD:    'bg-zinc-50 text-zinc-500',
  }
  const labels: Record<UserRole, string> = {
    SUPER_ADMIN: 'Super Admin',
    ADMIN:       'Admin',
    STANDARD:    'Standard',
  }
  return (
    <span className={`text-xs rounded px-2 py-0.5 ${classes[role]}`}>
      {labels[role]}
    </span>
  )
}

function MemberRow({ member }: { member: MemberResponse }) {
  return (
    <div className="border border-zinc-200 rounded-lg px-4 py-3 flex items-center justify-between">
      <div>
        <p className="text-sm font-medium text-zinc-900">{member.name}</p>
        <p className="text-xs text-zinc-400">{member.email}</p>
      </div>
      <RoleBadge role={member.role} />
    </div>
  )
}

function InviteRow({
  invite,
  onRevoke,
}: {
  invite:   InviteResponse
  onRevoke: (id: string) => void
}) {
  return (
    <div className="border border-zinc-200 rounded-lg px-4 py-3 flex items-center justify-between">
      <div>
        <p className="text-sm text-zinc-600">{invite.email}</p>
        <p className="text-xs text-zinc-400">Invited by {invite.invitedByName}</p>
      </div>
      <button
        onClick={() => onRevoke(invite.id)}
        className="text-xs text-zinc-400 hover:text-red-500 transition-colors duration-150"
      >
        Revoke
      </button>
    </div>
  )
}

function InviteModal({
  orgId,
  onClose,
}: {
  orgId:   string
  onClose: () => void
}) {
  const queryClient = useQueryClient()
  const [apiError, setApiError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<InviteForm>({ resolver: zodResolver(inviteSchema) })

  async function onSubmit(data: InviteForm) {
    setApiError(null)
    try {
      await sendInvite(orgId, data.email)
      await queryClient.invalidateQueries({ queryKey: ['members', orgId] })
      await queryClient.invalidateQueries({ queryKey: ['invites', orgId] })
      onClose()
    } catch (err: unknown) {
      setApiError(err instanceof Error ? err.message : 'Failed to send invite')
    }
  }

  return (
    <>
      <div className="fixed inset-0 bg-black/20 z-50" onClick={onClose} />
      <div className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 z-50 w-full max-w-sm">
        <div
          className="bg-white border border-zinc-200 rounded-lg p-6"
          style={{ boxShadow: '0 1px 3px 0 rgb(0 0 0 / 0.07)' }}
        >
          <p className="text-sm font-semibold text-zinc-900 mb-4">Invite member</p>
          <form onSubmit={handleSubmit(onSubmit)} noValidate className="flex flex-col gap-3">
            <div className="flex flex-col gap-1">
              <label className="text-xs text-zinc-500">Email address</label>
              <input
                type="email"
                {...register('email')}
                placeholder="colleague@example.com"
                className="h-9 rounded-md border border-zinc-200 bg-white px-3 text-sm text-zinc-900 outline-none placeholder:text-zinc-400 focus-visible:ring-2 focus-visible:ring-zinc-900 transition-colors duration-150"
              />
              {errors.email && (
                <span className="text-xs text-red-500">{errors.email.message}</span>
              )}
            </div>
            {apiError && <ErrorMessage message={apiError} />}
            <div className="flex items-center justify-end gap-2 mt-1">
              <button
                type="button"
                onClick={onClose}
                className="text-sm text-zinc-500 hover:text-zinc-900 transition-colors duration-150"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={isSubmitting}
                className="h-8 rounded-md bg-zinc-900 text-white text-sm px-3 hover:bg-zinc-800 transition-colors duration-150 flex items-center gap-2 disabled:opacity-60"
              >
                {isSubmitting && <LoadingSpinner size="sm" />}
                Send invite
              </button>
            </div>
          </form>
        </div>
      </div>
    </>
  )
}

export function MembersPage() {
  const { orgId = '' } = useParams<{ orgId: string }>()
  const user = useAuthStore((s) => s.user)!
  const queryClient = useQueryClient()
  const [showModal, setShowModal] = useState(false)

  const isAdminOrAbove = user.role === 'ADMIN' || user.role === 'SUPER_ADMIN'

  const membersQuery = useQuery({
    queryKey: ['members', orgId],
    queryFn:  () => getMembers(orgId),
    enabled:  !!orgId,
  })

  const invitesQuery = useQuery({
    queryKey: ['invites', orgId],
    queryFn:  () => getInvites(orgId),
    enabled:  !!orgId && isAdminOrAbove,
  })

  const members = membersQuery.data ?? []
  const pendingInvites = (invitesQuery.data ?? []).filter((i) => i.status === 'PENDING')

  const breadcrumbItems = [
    { label: user.orgName, href: '/dashboard' },
    { label: 'Members' },
  ]

  async function handleRevoke(inviteId: string) {
    try {
      await revokeInvite(orgId, inviteId)
      await queryClient.invalidateQueries({ queryKey: ['invites', orgId] })
    } catch {
      // silently ignore — could show toast in future
    }
  }

  return (
    <AppShell>
      {/* Page header */}
      <div className="border-b border-zinc-100 pb-4 mb-6">
        <Breadcrumb items={breadcrumbItems} />
        <div className="flex items-center justify-between mt-1">
          <h1 className="text-lg font-semibold text-zinc-900">Members</h1>
          {isAdminOrAbove && (
            <button
              onClick={() => setShowModal(true)}
              className="bg-zinc-900 text-white text-sm rounded-md px-3 h-8 hover:bg-zinc-800 transition-colors duration-150"
            >
              Invite
            </button>
          )}
        </div>
      </div>

      {/* Members section */}
      <p className="text-xs font-medium text-zinc-400 uppercase tracking-wide mb-3">Members</p>

      {membersQuery.isLoading && (
        <div className="flex flex-col gap-2">
          {[0, 1, 2].map((i) => (
            <div key={i} className="bg-zinc-100 rounded-lg h-14 animate-pulse" />
          ))}
        </div>
      )}

      {membersQuery.isError && <ErrorMessage message="Failed to load members" />}

      {!membersQuery.isLoading && members.length > 0 && (
        <div className="flex flex-col gap-2">
          {members.map((m) => (
            <MemberRow key={m.userId} member={m} />
          ))}
        </div>
      )}

      {/* Pending invites section (admin+) */}
      {isAdminOrAbove && (
        <div className="mt-8">
          <p className="text-xs font-medium text-zinc-400 uppercase tracking-wide mb-3">
            Pending Invites
          </p>

          {invitesQuery.isLoading && (
            <div className="flex flex-col gap-2">
              {[0, 1].map((i) => (
                <div key={i} className="bg-zinc-100 rounded-lg h-14 animate-pulse" />
              ))}
            </div>
          )}

          {invitesQuery.isError && <ErrorMessage message="Failed to load invites" />}

          {!invitesQuery.isLoading && pendingInvites.length === 0 && (
            <p className="text-sm text-zinc-400">No pending invites</p>
          )}

          {!invitesQuery.isLoading && pendingInvites.length > 0 && (
            <div className="flex flex-col gap-2">
              {pendingInvites.map((invite) => (
                <InviteRow key={invite.id} invite={invite} onRevoke={handleRevoke} />
              ))}
            </div>
          )}
        </div>
      )}

      {showModal && (
        <InviteModal orgId={orgId} onClose={() => setShowModal(false)} />
      )}
    </AppShell>
  )
}
