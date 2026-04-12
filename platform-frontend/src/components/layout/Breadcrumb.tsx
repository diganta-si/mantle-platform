import { Link } from 'react-router-dom'
import type { BreadcrumbItem } from '@/store/breadcrumbStore'

interface BreadcrumbProps {
  items: BreadcrumbItem[]
}

export function Breadcrumb({ items }: BreadcrumbProps) {
  if (items.length === 0) return null

  return (
    <nav className="flex items-center">
      {items.map((item, i) => (
        <span key={i} className="flex items-center">
          {i > 0 && (
            <span className="text-zinc-300 mx-1.5 text-xs">›</span>
          )}
          {item.href ? (
            <Link
              to={item.href}
              className="text-xs text-zinc-400 hover:text-zinc-600 transition-colors duration-150"
            >
              {item.label}
            </Link>
          ) : (
            <span className="text-xs text-zinc-400">{item.label}</span>
          )}
        </span>
      ))}
    </nav>
  )
}
