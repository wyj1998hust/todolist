import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import LoginView from '../views/LoginView.vue'
import DashboardView from '../views/DashboardView.vue'
import CategoryView from '../views/CategoryView.vue'
import UserView from '../views/UserView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', name: 'login', component: LoginView },
    { path: '/', name: 'dashboard', component: DashboardView, meta: { requiresAuth: true } },
    { path: '/categories', name: 'categories', component: CategoryView, meta: { requiresAuth: true, admin: true } },
    { path: '/users', name: 'users', component: UserView, meta: { requiresAuth: true, admin: true } },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  await auth.restore()
  if (to.meta.requiresAuth && !auth.user) return { name: 'login' }
  if (to.meta.admin && !auth.isAdmin) return { name: 'dashboard' }
  if (to.name === 'login' && auth.user) return { name: 'dashboard' }
  return true
})

export default router
