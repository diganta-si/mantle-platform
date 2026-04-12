import { useEffect } from 'react'
import { useBreadcrumbStore, type BreadcrumbItem } from '@/store/breadcrumbStore'

export function useBreadcrumb(items: BreadcrumbItem[]) {
  const setItems = useBreadcrumbStore((s) => s.setItems)

  useEffect(() => {
    setItems(items)
    return () => setItems([])
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [JSON.stringify(items)])
}
