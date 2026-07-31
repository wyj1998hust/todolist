import { defineStore } from 'pinia'
import { todoApi } from '../services/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    initialized: false,
  }),
  getters: {
    isAdmin: (state) => state.user?.role === 'admin',
  },
  actions: {
    async restore() {
      if (this.initialized) return this.user
      try {
        this.user = await todoApi.me()
      } catch {
        this.user = null
      } finally {
        this.initialized = true
      }
      return this.user
    },
    async login(credentials) {
      this.user = await todoApi.login(credentials)
      this.initialized = true
      return this.user
    },
    async logout() {
      try {
        await todoApi.logout()
      } finally {
        this.user = null
        this.initialized = true
      }
    },
  },
})
