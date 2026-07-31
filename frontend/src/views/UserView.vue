<script setup>
import { onMounted, reactive, ref } from 'vue'
import { EditPen, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import AppShell from '../components/AppShell.vue'
import { ApiError, todoApi } from '../services/api'

const users = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editing = ref(null)
const formRef = ref(null)
const form = reactive(emptyForm())
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  displayName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ min: 8, message: '密码至少需要8个字符', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
}

function emptyForm() { return { username: '', displayName: '', password: '', role: 'member', active: true } }
async function load() {
  loading.value = true
  try { users.value = await todoApi.users(true) } catch (error) { ElMessage.error(error instanceof ApiError ? error.message : '加载用户失败') } finally { loading.value = false }
}
function open(user = null) {
  editing.value = user
  Object.assign(form, emptyForm(), user || {})
  form.password = ''
  dialogVisible.value = true
}
async function save() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid || (!editing.value && !form.password)) return
  const payload = { ...form }
  if (editing.value && !payload.password) delete payload.password
  if (editing.value) delete payload.username
  try {
    if (editing.value) await todoApi.updateUser(editing.value.id, payload)
    else await todoApi.createUser(payload)
    dialogVisible.value = false
    await load()
    ElMessage.success(editing.value ? '用户已更新' : '用户已创建')
  } catch (error) { ElMessage.error(error instanceof ApiError ? error.message : '保存用户失败') }
}
onMounted(load)
</script>

<template>
  <AppShell title="用户管理">
    <template #actions><el-button type="primary" :icon="Plus" @click="open()">新增用户</el-button></template>
    <el-alert title="停用用户后不能登录，也不能被指派为任务跟进人；系统始终至少保留一个启用的管理员。" type="info" :closable="false" show-icon class="page-alert" />
    <el-table :data="users" v-loading="loading" class="data-table">
      <el-table-column prop="displayName" label="姓名" min-width="150" />
      <el-table-column prop="username" label="用户名" min-width="160" />
      <el-table-column label="角色" width="120"><template #default="{ row }"><el-tag :type="row.role === 'admin' ? 'warning' : 'info'">{{ row.role === 'admin' ? '管理员' : '成员' }}</el-tag></template></el-table-column>
      <el-table-column label="状态" width="110"><template #default="{ row }"><el-tag :type="row.active ? 'success' : 'info'">{{ row.active ? '启用' : '已停用' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="120"><template #default="{ row }"><el-button link type="primary" :icon="EditPen" @click="open(row)">编辑</el-button></template></el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" :title="editing ? '编辑用户' : '新增用户'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="username"><el-input v-model="form.username" :disabled="Boolean(editing)" maxlength="64" /></el-form-item>
        <el-form-item label="姓名" prop="displayName"><el-input v-model="form.displayName" maxlength="100" /></el-form-item>
        <el-form-item :label="editing ? '重置密码（留空表示不修改）' : '初始密码'" prop="password"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="角色" prop="role"><el-select v-model="form.role" style="width: 100%"><el-option label="成员" value="member" /><el-option label="管理员" value="admin" /></el-select></el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.active" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </AppShell>
</template>
