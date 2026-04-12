import { create } from 'zustand'

export interface BreadcrumbItem {
  label: string
  href?: string
}

interface BreadcrumbState {
  items: BreadcrumbItem[]
  setItems: (items: BreadcrumbItem[]) => void
}

export const useBreadcrumbStore = create<BreadcrumbState>()((set) => ({
  items: [],
  setItems: (items) => set({ items }),
}))
