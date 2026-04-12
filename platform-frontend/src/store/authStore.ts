import { create } from 'zustand'
import { persist, createJSONStorage } from 'zustand/middleware'
import type { AuthResponse } from '@/types/auth'

interface AuthState {
  user: AuthResponse | null
  isAuthenticated: boolean
  setUser: (user: AuthResponse) => void
  clearUser: () => void
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      isAuthenticated: false,
      setUser: (user) => set({ user, isAuthenticated: true }),
      clearUser: () => set({ user: null, isAuthenticated: false }),
    }),
    {
      name: 'mantle-auth',
      storage: createJSONStorage(() => sessionStorage),
    }
  )
)
