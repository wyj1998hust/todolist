<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar, DataAnalysis, UserFilled, CollectionTag } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'

const props = defineProps({ title: { type: String, required: true } })
const auth = useAuthStore()
const router = useRouter()
const isAdmin = computed(() => auth.isAdmin)

async function logout() {
  await auth.logout()
  ElMessage.success('已退出登录')
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="app-shell">
    <header class="topbar">
      <RouterLink class="brand" :to="{ name: 'dashboard' }">
        <span class="brand-mark"><Calendar /></span>
        <span>团队任务</span>
      </RouterLink>
      <nav class="topbar-nav" aria-label="主导航">
        <RouterLink :to="{ name: 'dashboard' }"><DataAnalysis /> 甘特图</RouterLink>
        <RouterLink v-if="isAdmin" :to="{ name: 'categories' }"><CollectionTag /> 分类</RouterLink>
        <RouterLink v-if="isAdmin" :to="{ name: 'users' }"><UserFilled /> 用户</RouterLink>
      </nav>
      <div class="topbar-user">
        <div>
          <strong>{{ auth.user?.displayName }}</strong>
          <span>{{ isAdmin ? '管理员' : '成员' }}</span>
        </div>
        <el-button text type="primary" @click="logout">退出</el-button>
      </div>
    </header>
    <main class="page-content">
      <div class="page-heading">
        <div>
          <h1>{{ props.title }}</h1>
          <p>内部团队任务协作平台</p>
        </div>
        <slot name="actions" />
      </div>
      <slot />
    </main>
  </div>
</template>
