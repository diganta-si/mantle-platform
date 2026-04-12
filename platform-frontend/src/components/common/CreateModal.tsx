import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { ErrorMessage } from './ErrorMessage'
import { LoadingSpinner } from './LoadingSpinner'

const schema = z.object({
  name:        z.string().min(1, 'Required'),
  description: z.string().optional(),
})

type FormValues = z.infer<typeof schema>

interface CreateModalProps {
  title:            string
  fieldLabel:       string
  fieldPlaceholder: string
  showDescription:  boolean
  onSubmit:         (data: { name: string; description?: string }) => void
  onClose:          () => void
  isLoading:        boolean
  error:            string | null
}

export function CreateModal({
  title,
  fieldLabel,
  fieldPlaceholder,
  showDescription,
  onSubmit,
  onClose,
  isLoading,
  error,
}: CreateModalProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormValues>({ resolver: zodResolver(schema) })

  return (
    <>
      {/* Overlay */}
      <div
        className="fixed inset-0 bg-black/20 z-50"
        onClick={onClose}
      />

      {/* Panel */}
      <div className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 z-50 w-full max-w-sm">
        <div
          className="bg-white border border-zinc-200 rounded-lg p-6"
          style={{ boxShadow: '0 1px 3px 0 rgb(0 0 0 / 0.07)' }}
        >
          <p className="text-sm font-semibold text-zinc-900 mb-4">{title}</p>

          <form onSubmit={handleSubmit(onSubmit)} noValidate className="flex flex-col gap-3">
            <div className="flex flex-col gap-1">
              <label className="text-xs text-zinc-500">{fieldLabel}</label>
              <input
                type="text"
                {...register('name')}
                placeholder={fieldPlaceholder}
                className="h-9 rounded-md border border-zinc-200 bg-white px-3 text-sm text-zinc-900 outline-none placeholder:text-zinc-400 focus-visible:ring-2 focus-visible:ring-zinc-900 transition-colors duration-150"
              />
              {errors.name && (
                <span className="text-xs text-red-500">{errors.name.message}</span>
              )}
            </div>

            {showDescription && (
              <div className="flex flex-col gap-1">
                <label className="text-xs text-zinc-500">Description (optional)</label>
                <textarea
                  rows={3}
                  {...register('description')}
                  placeholder="Brief description"
                  className="rounded-md border border-zinc-200 bg-white px-3 py-2 text-sm text-zinc-900 outline-none placeholder:text-zinc-400 focus-visible:ring-2 focus-visible:ring-zinc-900 transition-colors duration-150 resize-none"
                />
              </div>
            )}

            {error && <ErrorMessage message={error} />}

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
                disabled={isLoading}
                className="h-8 rounded-md bg-zinc-900 text-white text-sm px-3 hover:bg-zinc-800 transition-colors duration-150 flex items-center gap-2 disabled:opacity-60"
              >
                {isLoading && <LoadingSpinner size="sm" />}
                Create
              </button>
            </div>
          </form>
        </div>
      </div>
    </>
  )
}
