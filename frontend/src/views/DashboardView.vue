<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppShell from '../components/AppShell.vue'
import GanttChart from '../components/GanttChart.vue'
import TaskDialog from '../components/TaskDialog.vue'
import { ApiError, todoApi } from '../services/api'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const tasks = ref([])
const categories = ref([])
const users = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const selectedTask = ref(null)
const saving = ref(false)
const filters = reactive({ categoryId: '', assigneeId: '', status: '' })

const filteredTasks = computed(() => tasks.value.filter((task) =>
  (!filters.categoryId || task.category?.id === filters.categoryId)
  && (!filters.assigneeId || task.assignee?.id === filters.assigneeId)
  && (!filters.status || task.status === filters.status)))
const canCreate = computed(() => Boolean(auth.user))

function canEdit(task) {
  return auth.isAdmin || task.assignee?.id === auth.user?.id
}

async function loadReferenceData() {
  const [categoryData, userData] = await Promise.all([todoApi.categories(), todoApi.users()])
  categories.value = categoryData
  users.value = userData
}

async function refreshTasks() {
  loading.value = true
  try {
    tasks.value = await todoApi.tasks()
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '加载任务失败')
  } finally {
    loading.value = false
  }
}

async function initialize() {
  loading.value = true
  try {
    await Promise.all([loadReferenceData(), refreshTasks()])
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '加载基础数据失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  selectedTask.value = null
  dialogVisible.value = true
}

function openTask(task) {
  selectedTask.value = task
  dialogVisible.value = true
}

async function saveTask(payload) {
  if (payload.startDate > payload.deadline) {
    ElMessage.warning('开始日期不能晚于截止日期')
    return
  }
  saving.value = true
  try {
    const saved = selectedTask.value
      ? await todoApi.updateTask(selectedTask.value.id, payload)
      : await todoApi.createTask(payload)
    const index = tasks.value.findIndex((task) => task.id === saved.id)
    if (index === -1) tasks.value.push(saved)
    else tasks.value.splice(index, 1, saved)
    dialogVisible.value = false
    ElMessage.success(selectedTask.value ? '任务已更新' : '任务已创建')
  } catch (error) {
    if (error instanceof ApiError && error.status === 409) {
      ElMessage.warning('任务已被其他用户更新，正在刷新数据')
      await refreshTasks()
    } else {
      ElMessage.error(error instanceof ApiError ? error.message : '保存任务失败')
    }
  } finally {
    saving.value = false
  }
}

async function removeTask(task) {
  try {
    await ElMessageBox.confirm(`确定删除“${task.title}”吗？此操作不可恢复。`, '删除任务', { type: 'warning' })
    await todoApi.deleteTask(task.id)
    tasks.value = tasks.value.filter((item) => item.id !== task.id)
    dialogVisible.value = false
    ElMessage.success('任务已删除')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error instanceof ApiError ? error.message : '删除任务失败')
    }
  }
}

onMounted(initialize)
</script>

<template>
  <AppShell title="任务甘特图">
    <template #actions>
      <el-space>
        <el-button :icon="Refresh" :loading="loading" @click="refreshTasks">刷新</el-button>
        <el-button v-if="canCreate" type="primary" :icon="Plus" @click="openCreate">新增任务</el-button>
      </el-space>
    </template>

    <section class="filter-panel" aria-label="任务筛选">
      <el-select v-model="filters.categoryId" clearable placeholder="全部分类">
        <el-option v-for="category in categories" :key="category.id" :value="category.id" :label="category.name" />
      </el-select>
      <el-select v-model="filters.assigneeId" clearable placeholder="全部跟进人">
        <el-option v-for="user in users" :key="user.id" :value="user.id" :label="user.displayName" />
      </el-select>
      <el-select v-model="filters.status" clearable placeholder="全部状态">
        <el-option label="未开始" value="not_started" />
        <el-option label="进行中" value="in_progress" />
        <el-option label="已完成" value="completed" />
      </el-select>
      <span class="task-count">显示 {{ filteredTasks.length }} / {{ tasks.length }} 个任务</span>
    </section>

    <el-skeleton :loading="loading && !tasks.length" animated :rows="8">
      <GanttChart :tasks="filteredTasks" @select="openTask" />
    </el-skeleton>

    <TaskDialog
      v-model="dialogVisible"
      :task="selectedTask"
      :categories="categories"
      :users="users"
      :current-user="auth.user"
      :read-only="Boolean(selectedTask && !canEdit(selectedTask))"
      :saving="saving"
      @submit="saveTask"
      @remove="removeTask"
    />
  </AppShell>
</template>
