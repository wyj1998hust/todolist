<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar, DataAnalysis, UserFilled, CollectionTag } from '@element-plus/icons-vue'
import { useAuthStore } from '../stores/auth'
import ChangePasswordDialog from './ChangePasswordDialog.vue'

const props = defineProps({ title: { type: String, required: true } })
const auth = useAuthStore()
const router = useRouter()
const isAdmin = computed(() => auth.isAdmin)
const passwordDialogVisible = ref(false)

async function logout() {
  await auth.logout()
  ElMessage.success('已退出登录')
  router.push({ name: 'login' })
}

function handlePasswordChanged() {
  auth.clearLocalSession()
  ElMessage.success('密码已修改，请重新登录')
  router.replace({ name: 'login' })
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
        <el-button text type="primary" @click="passwordDialogVisible = true">修改密码</el-button>
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
    <ChangePasswordDialog v-model="passwordDialogVisible" @changed="handlePasswordChanged" />
  </div>
</template>
