interface ErrorMessageProps {
  message: string
}

export function ErrorMessage({ message }: ErrorMessageProps) {
  return (
    <div className="flex items-center gap-2 bg-red-50 border border-red-200 text-red-600 text-sm rounded-md px-3 py-2">
      <span aria-hidden="true">✕</span>
      <span>{message}</span>
    </div>
  )
}
