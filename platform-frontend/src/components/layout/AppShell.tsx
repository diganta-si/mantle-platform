import { TopNav } from './TopNav'

interface AppShellProps {
  children: React.ReactNode
}

export function AppShell({ children }: AppShellProps) {
  return (
    <div className="min-h-screen bg-white">
      <TopNav />
      <div className="pt-12 min-h-screen bg-white">
        <div className="max-w-6xl mx-auto px-6 py-8">
          {children}
        </div>
      </div>
    </div>
  )
}
