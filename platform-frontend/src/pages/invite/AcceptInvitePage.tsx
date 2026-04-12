import { useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { getInviteDetail, acceptInvite } from '@/api/invite'
import { useAuthStore } from '@/store/authStore'
import { LoadingSpinner } from '@/components/common/LoadingSpinner'
import { ErrorMessage } from '@/components/common/ErrorMessage'

const schema = z.object({
  name:     z.string().min(1, 'Required'),
  password: z.string().min(8, 'Min 8 characters'),
})
type FormValues = z.infer<typeof schema>

export function AcceptInvitePage() {
  const { inviteId = '' } = useParams<{ inviteId: string }>()
  const navigate = useNavigate()
  const setUser = useAuthStore((s) => s.setUser)
  const [apiError, setApiError] = useState<string | null>(null)

  const detailQuery = useQuery({
    queryKey: ['inviteDetail', inviteId],
    queryFn:  () => getInviteDetail(inviteId),
    enabled:  !!inviteId,
    retry:    false,
  })

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema) })

  const detail = detailQuery.data

  async function onSubmit(data: FormValues) {
    if (!detail) return
    setApiError(null)
    try {
      const user = await acceptInvite(detail.orgId, inviteId, {
        email:    detail.email,
        name:     data.name,
        password: data.password,
      })
      setUser(user)
      navigate('/dashboard')
    } catch (err: unknown) {
      setApiError(err instanceof Error ? err.message : 'Failed to accept invite')
    }
  }

  if (detailQuery.isLoading) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center">
        <LoadingSpinner size="md" />
      </div>
    )
  }

  if (detailQuery.isError || (detail && detail.status !== 'PENDING')) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center px-4">
        <div className="w-full max-w-sm border border-zinc-200 rounded-lg p-8"
          style={{ boxShadow: '0 1px 3px 0 rgb(0 0 0 / 0.07)' }}
        >
          <p className="text-sm font-semibold text-zinc-900 mb-2">
            This invite is no longer valid
          </p>
          <p className="text-sm text-zinc-500">
            The invite may have been accepted or revoked.
          </p>
          <Link
            to="/login"
            className="mt-4 inline-block text-sm text-zinc-900 underline underline-offset-2"
          >
            Go to login
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-white flex items-center justify-center px-4 py-8">
      <div className="w-full max-w-sm">
        <div
          className="border border-zinc-200 rounded-lg p-8"
          style={{ boxShadow: '0 1px 3px 0 rgb(0 0 0 / 0.07)' }}
        >
          <h1 className="text-xl font-semibold text-zinc-900">You've been invited</h1>
          <p className="text-sm text-zinc-500 mt-1 mb-6">
            Join {detail?.orgName} on Mantle
          </p>

          <form onSubmit={handleSubmit(onSubmit)} noValidate className="flex flex-col gap-4">
            <div className="flex flex-col gap-1">
              <label className="text-xs text-zinc-500" htmlFor="accept-email">
                Email address
              </label>
              <input
                id="accept-email"
                type="email"
                value={detail?.email ?? ''}
                disabled
                readOnly
                className="h-9 rounded-md border border-zinc-200 bg-zinc-50 px-3 text-sm text-zinc-400 outline-none cursor-not-allowed"
              />
            </div>

            <div className="flex flex-col gap-1">
              <label className="text-xs text-zinc-500" htmlFor="accept-name">
                Full name
              </label>
              <input
                id="accept-name"
                type="text"
                autoComplete="name"
                {...register('name')}
                placeholder="Jane Smith"
                className="h-9 rounded-md border border-zinc-200 bg-white px-3 text-sm text-zinc-900 outline-none placeholder:text-zinc-400 focus-visible:ring-2 focus-visible:ring-zinc-900 transition-colors duration-150"
              />
              {errors.name && (
                <span className="text-xs text-red-500">{errors.name.message}</span>
              )}
            </div>

            <div className="flex flex-col gap-1">
              <label className="text-xs text-zinc-500" htmlFor="accept-password">
                Password
              </label>
              <input
                id="accept-password"
                type="password"
                autoComplete="new-password"
                {...register('password')}
                placeholder="Min 8 characters"
                className="h-9 rounded-md border border-zinc-200 bg-white px-3 text-sm text-zinc-900 outline-none placeholder:text-zinc-400 focus-visible:ring-2 focus-visible:ring-zinc-900 transition-colors duration-150"
              />
              {errors.password && (
                <span className="text-xs text-red-500">{errors.password.message}</span>
              )}
            </div>

            {apiError && <ErrorMessage message={apiError} />}

            <button
              type="submit"
              disabled={isSubmitting}
              className="mt-1 h-9 w-full rounded-md bg-zinc-900 text-white text-sm font-medium hover:bg-zinc-800 transition-colors duration-150 flex items-center justify-center focus-visible:ring-2 focus-visible:ring-zinc-900 disabled:opacity-60"
            >
              {isSubmitting ? <LoadingSpinner size="sm" /> : 'Accept & join'}
            </button>
          </form>
        </div>
      </div>
    </div>
  )
}
