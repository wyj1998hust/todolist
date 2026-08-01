<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Calendar } from '@element-plus/icons-vue'
import { ApiError } from '../services/api'
import { useAuthStore } from '../stores/auth'
import { loadLastLoginUsername, rememberLoginUsername } from '../utils/loginPreferences'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const form = reactive({ username: loadLastLoginUsername(), password: '' })
const formRef = ref(null)
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function login() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const username = form.username.trim()
    await auth.login({ ...form, username })
    rememberLoginUsername(username)
    ElMessage.success('登录成功')
    router.replace({ name: 'dashboard' })
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '无法连接到服务')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-card">
      <div class="login-brand"><span><Calendar /></span><div><h1>团队任务</h1><p>内部甘特图协作平台</p></div></div>
      <el-alert title="首次登录成功后请修改密码" type="info" :closable="false" show-icon />
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="login-form" @submit.prevent="login">
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" autocomplete="username" /></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="form.password" type="password" show-password autocomplete="current-password" @keyup.enter="login" /></el-form-item>
        <el-button type="primary" native-type="submit" :loading="loading" class="login-submit">登录</el-button>
      </el-form>
    </section>
  </main>
</template>
