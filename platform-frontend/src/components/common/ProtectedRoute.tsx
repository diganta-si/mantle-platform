import { Navigate } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'
import { LoadingSpinner } from './LoadingSpinner'

interface ProtectedRouteProps {
  children: React.ReactNode
}

export function ProtectedRoute({ children }: ProtectedRouteProps) {
  const { isAuthenticated, user } = useAuthStore()

  // During rehydration from sessionStorage, user may still be null for a brief moment.
  // If the store has not yet rehydrated, show a spinner.
  const hasRehydrated = useAuthStore.persist.hasHydrated()

  if (!hasRehydrated) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    )
  }

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />
  }

  return <>{children}</>
}
