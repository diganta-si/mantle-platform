import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useForm, Controller } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { register as registerUser } from '@/api/auth'
import { useAuthStore } from '@/store/authStore'
import { LoadingSpinner } from '@/components/common/LoadingSpinner'
import { ErrorMessage } from '@/components/common/ErrorMessage'
import type { OrgType } from '@/types/auth'

const schema = z.object({
  name:     z.string().min(1, 'Required'),
  email:    z.string().email('Invalid email'),
  password: z.string().min(8, 'Min 8 characters'),
  orgName:  z.string().min(1, 'Required'),
  orgType:  z.enum(['INDIVIDUAL', 'ENTERPRISE']),
})

type FormValues = z.infer<typeof schema>

const orgTypeOptions: { value: OrgType; label: string }[] = [
  { value: 'INDIVIDUAL', label: 'Individual' },
  { value: 'ENTERPRISE', label: 'Enterprise' },
]

export function RegisterPage() {
  const navigate = useNavigate()
  const setUser = useAuthStore((s) => s.setUser)
  const [apiError, setApiError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    control,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { orgType: 'INDIVIDUAL' },
  })

  async function onSubmit(data: FormValues) {
    setApiError(null)
    try {
      const user = await registerUser(data)
      setUser(user)
      navigate('/dashboard')
    } catch (err: unknown) {
      const message =
        err instanceof Error ? err.message : 'Registration failed. Please try again.'
      setApiError(message)
    }
  }

  return (
    <div className="min-h-screen bg-white flex items-center justify-center px-4 py-8">
      <div className="w-full max-w-sm">
        <div
          className="border border-zinc-200 rounded-lg p-8"
          style={{ boxShadow: '0 1px 3px 0 rgb(0 0 0 / 0.07)' }}
        >
          <h1 className="text-xl font-semibold text-zinc-900">Create your account</h1>
          <p className="text-sm text-zinc-500 mt-1 mb-6">Get started with Mantle</p>

          <form onSubmit={handleSubmit(onSubmit)} noValidate className="flex flex-col gap-4">
            <div className="flex flex-col gap-1">
              <label className="text-xs text-zinc-500" htmlFor="name">
                Full name
              </label>
              <input
                id="name"
                type="text"
                autoComplete="name"
                {...register('name')}
                className="h-9 rounded-md border border-zinc-200 bg-white px-3 text-sm text-zinc-900 outline-none placeholder:text-zinc-400 focus-visible:ring-2 focus-visible:ring-zinc-900 transition-colors duration-150"
                placeholder="Jane Smith"
              />
              {errors.name && (
                <span className="text-xs text-red-500">{errors.name.message}</span>
              )}
            </div>

            <div className="flex flex-col gap-1">
              <label className="text-xs text-zinc-500" htmlFor="reg-email">
                Email
              </label>
              <input
                id="reg-email"
                type="email"
                autoComplete="email"
                {...register('email')}
                className="h-9 rounded-md border border-zinc-200 bg-white px-3 text-sm text-zinc-900 outline-none placeholder:text-zinc-400 focus-visible:ring-2 focus-visible:ring-zinc-900 transition-colors duration-150"
                placeholder="you@example.com"
              />
              {errors.email && (
                <span className="text-xs text-red-500">{errors.email.message}</span>
              )}
            </div>

            <div className="flex flex-col gap-1">
              <label className="text-xs text-zinc-500" htmlFor="reg-password">
                Password
              </label>
              <input
                id="reg-password"
                type="password"
                autoComplete="new-password"
                {...register('password')}
                className="h-9 rounded-md border border-zinc-200 bg-white px-3 text-sm text-zinc-900 outline-none placeholder:text-zinc-400 focus-visible:ring-2 focus-visible:ring-zinc-900 transition-colors duration-150"
                placeholder="Min 8 characters"
              />
              {errors.password && (
                <span className="text-xs text-red-500">{errors.password.message}</span>
              )}
            </div>

            <div className="flex flex-col gap-1">
              <label className="text-xs text-zinc-500" htmlFor="orgName">
                Organisation name
              </label>
              <input
                id="orgName"
                type="text"
                {...register('orgName')}
                className="h-9 rounded-md border border-zinc-200 bg-white px-3 text-sm text-zinc-900 outline-none placeholder:text-zinc-400 focus-visible:ring-2 focus-visible:ring-zinc-900 transition-colors duration-150"
                placeholder="Acme Corp"
              />
              {errors.orgName && (
                <span className="text-xs text-red-500">{errors.orgName.message}</span>
              )}
            </div>

            <div className="flex flex-col gap-1">
              <span className="text-xs text-zinc-500">Organisation type</span>
              <Controller
                control={control}
                name="orgType"
                render={({ field }) => (
                  <div className="flex gap-2">
                    {orgTypeOptions.map((opt) => {
                      const selected = field.value === opt.value
                      return (
                        <button
                          key={opt.value}
                          type="button"
                          onClick={() => field.onChange(opt.value)}
                          className={`flex-1 h-9 rounded-md text-sm font-medium transition-colors duration-150 focus-visible:ring-2 focus-visible:ring-zinc-900 ${
                            selected
                              ? 'bg-zinc-900 text-white'
                              : 'bg-white text-zinc-600 border border-zinc-200 hover:bg-zinc-50'
                          }`}
                        >
                          {opt.label}
                        </button>
                      )
                    })}
                  </div>
                )}
              />
              {errors.orgType && (
                <span className="text-xs text-red-500">{errors.orgType.message}</span>
              )}
            </div>

            {apiError && <ErrorMessage message={apiError} />}

            <button
              type="submit"
              disabled={isSubmitting}
              className="mt-1 h-9 w-full rounded-md bg-zinc-900 text-white text-sm font-medium hover:bg-zinc-800 transition-colors duration-150 flex items-center justify-center focus-visible:ring-2 focus-visible:ring-zinc-900 disabled:opacity-60"
            >
              {isSubmitting ? <LoadingSpinner size="sm" /> : 'Create account'}
            </button>
          </form>
        </div>

        <p className="mt-4 text-center text-sm text-zinc-500">
          Already have an account?{' '}
          <Link
            to="/login"
            className="text-zinc-900 underline underline-offset-2"
          >
            Sign in
          </Link>
        </p>
      </div>
    </div>
  )
}
