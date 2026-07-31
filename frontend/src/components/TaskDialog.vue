<script setup>
import { computed, reactive, ref, watch } from 'vue'

const props = defineProps({
  modelValue: Boolean,
  task: { type: Object, default: null },
  categories: { type: Array, default: () => [] },
  users: { type: Array, default: () => [] },
  currentUser: { type: Object, required: true },
  readOnly: Boolean,
  saving: Boolean,
})

const emit = defineEmits(['update:modelValue', 'submit', 'remove'])
const formRef = ref(null)
const form = reactive(emptyForm())

function emptyForm() {
  const today = new Date().toISOString().slice(0, 10)
  return {
    title: '', startDate: today, deadline: today, categoryId: null, assigneeId: null,
    progress: 0, status: 'not_started', version: null,
  }
}

watch(() => [props.modelValue, props.task], () => {
  Object.assign(form, emptyForm(), props.task ? {
    title: props.task.title,
    startDate: props.task.startDate,
    deadline: props.task.deadline,
    categoryId: props.task.category?.id ?? null,
    assigneeId: props.task.assignee?.id ?? null,
    progress: props.task.progress,
    status: props.task.status,
    version: props.task.version,
  } : {})
}, { immediate: true, deep: true })

const canChangeAssignee = computed(() => !props.task || props.currentUser.role === 'admin')
const title = computed(() => props.task ? (props.readOnly ? '任务详情' : '编辑任务') : '新增任务')
const activeCategories = computed(() => props.categories.filter((category) => category.active))
const activeUsers = computed(() => props.users.filter((user) => user.active))
const rules = {
  title: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  deadline: [{ required: true, message: '请选择截止日期', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择任务分类', trigger: 'change' }],
  assigneeId: [{ required: true, message: '请选择跟进人', trigger: 'change' }],
}

function close() {
  emit('update:modelValue', false)
}

function syncProgress() {
  if (form.progress >= 100) form.status = 'completed'
  else if (form.progress > 0 && form.status === 'not_started') form.status = 'in_progress'
}

function syncStatus() {
  if (form.status === 'completed') form.progress = 100
}

async function submit() {
  if (props.readOnly) return close()
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (form.startDate > form.deadline) return
  const payload = { ...form, title: form.title.trim() }
  if (!props.task) delete payload.version
  emit('submit', payload)
}
</script>

<template>
  <el-dialog :model-value="modelValue" :title="title" width="min(680px, calc(100vw - 32px))" destroy-on-close @close="close">
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
      <el-form-item label="任务名称" prop="title">
        <el-input v-model="form.title" :disabled="readOnly" maxlength="255" show-word-limit placeholder="例如：完成需求评审" />
      </el-form-item>
      <div class="form-grid">
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="form.startDate" :disabled="readOnly" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="截止日期" prop="deadline">
          <el-date-picker v-model="form.deadline" :disabled="readOnly" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="任务分类" prop="categoryId">
          <el-select v-model="form.categoryId" :disabled="readOnly" placeholder="选择分类" style="width: 100%">
            <el-option v-for="category in activeCategories" :key="category.id" :value="category.id" :label="category.name">
              <span class="color-dot" :style="{ background: category.color }" />{{ category.name }}
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="跟进人" prop="assigneeId">
          <el-select v-model="form.assigneeId" :disabled="readOnly || !canChangeAssignee" placeholder="选择成员" style="width: 100%">
            <el-option v-for="user in activeUsers" :key="user.id" :value="user.id" :label="user.displayName" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" :disabled="readOnly" style="width: 100%" @change="syncStatus">
            <el-option label="未开始" value="not_started" />
            <el-option label="进行中" value="in_progress" />
            <el-option label="已完成" value="completed" />
          </el-select>
        </el-form-item>
        <el-form-item label="进度">
          <el-input-number v-model="form.progress" :disabled="readOnly" :min="0" :max="100" style="width: 100%" @change="syncProgress" />
        </el-form-item>
      </div>
    </el-form>
    <template #footer>
      <el-button v-if="task && currentUser.role === 'admin' && !readOnly" type="danger" plain @click="emit('remove', task)">删除任务</el-button>
      <span class="dialog-actions">
        <el-button @click="close">{{ readOnly ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!readOnly" type="primary" :loading="saving" @click="submit">保存</el-button>
      </span>
    </template>
  </el-dialog>
</template>
