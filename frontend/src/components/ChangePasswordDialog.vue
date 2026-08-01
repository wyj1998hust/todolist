<script setup>
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ApiError, todoApi } from '../services/api'

const props = defineProps({ modelValue: Boolean })
const emit = defineEmits(['update:modelValue', 'changed'])
const formRef = ref(null)
const saving = ref(false)
const form = reactive({ currentPassword: '', newPassword: '', confirmPassword: '' })

const validateConfirmPassword = (_rule, value, callback) => {
  if (value !== form.newPassword) callback(new Error('两次输入的新密码不一致'))
  else callback()
}

const rules = {
  currentPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 100, message: '密码长度必须为8到100个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

function reset() {
  Object.assign(form, { currentPassword: '', newPassword: '', confirmPassword: '' })
  formRef.value?.clearValidate()
}

function close() {
  emit('update:modelValue', false)
}

watch(() => props.modelValue, (visible) => {
  if (visible) reset()
})

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await todoApi.changePassword({
      currentPassword: form.currentPassword,
      newPassword: form.newPassword,
    })
    emit('update:modelValue', false)
    emit('changed')
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '修改密码失败')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <el-dialog :model-value="modelValue" title="修改密码" width="min(460px, calc(100vw - 32px))" destroy-on-close @close="close">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
      <el-form-item label="当前密码" prop="currentPassword">
        <el-input v-model="form.currentPassword" type="password" show-password autocomplete="current-password" />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="form.newPassword" type="password" show-password autocomplete="new-password" />
      </el-form-item>
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" type="password" show-password autocomplete="new-password" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="close">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>
