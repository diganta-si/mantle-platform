import { Link, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'
import { useBreadcrumbStore } from '@/store/breadcrumbStore'
import { logout } from '@/api/auth'
import { Breadcrumb } from './Breadcrumb'

export function TopNav() {
  const { isAuthenticated, user, clearUser } = useAuthStore()
  const items = useBreadcrumbStore((s) => s.items)
  const navigate = useNavigate()

  async function handleSignOut() {
    try {
      await logout()
    } catch {
      // proceed regardless of logout API result
    }
    clearUser()
    navigate('/login')
  }

  return (
    <header className="fixed top-0 left-0 right-0 h-12 bg-white border-b border-zinc-200 z-50 flex items-center px-6 gap-4">
      <span className="text-sm font-semibold text-zinc-900 shrink-0">Mantle</span>
      {items.length > 0 && (
        <>
          <span className="text-zinc-200 text-sm">/</span>
          <Breadcrumb items={items} />
        </>
      )}
      {isAuthenticated && user && (
        <div className="ml-auto flex items-center gap-4 shrink-0">
          <Link
            to={`/orgs/${user.orgId}/members`}
            className="text-sm text-zinc-500 hover:text-zinc-900 transition-colors duration-150"
          >
            Members
          </Link>
          <span className="text-sm text-zinc-500">{user.name}</span>
          <button
            onClick={handleSignOut}
            className="text-sm text-zinc-500 hover:text-zinc-900 transition-colors duration-150"
          >
            Sign out
          </button>
        </div>
      )}
    </header>
  )
}
