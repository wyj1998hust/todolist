<script setup>
import { onMounted, reactive, ref } from 'vue'
import { EditPen, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppShell from '../components/AppShell.vue'
import { ApiError, todoApi } from '../services/api'
import { normalizeCategoryColor } from '../utils/categoryColor'
import ColorPalettePicker from '../components/ColorPalettePicker.vue'

const categories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editing = ref(null)
const formRef = ref(null)
const form = reactive(emptyForm())
const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  color: [{ required: true, message: '请选择分类颜色', trigger: 'change' }],
}

function emptyForm() { return { name: '', color: '#155EEF', sortOrder: 0, active: true } }
async function load() {
  loading.value = true
  try { categories.value = await todoApi.categories(true) } catch (error) { ElMessage.error(error instanceof ApiError ? error.message : '加载分类失败') } finally { loading.value = false }
}
function open(category = null) {
  editing.value = category
  Object.assign(form, emptyForm(), category || {})
  dialogVisible.value = true
}
async function save() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  const normalizedColor = normalizeCategoryColor(form.color)
  if (!normalizedColor) {
    ElMessage.error('颜色必须为六位十六进制格式')
    return
  }
  const payload = { ...form, color: normalizedColor }
  try {
    if (editing.value) await todoApi.updateCategory(editing.value.id, payload)
    else await todoApi.createCategory(payload)
    dialogVisible.value = false
    await load()
    ElMessage.success('分类已保存')
  } catch (error) { ElMessage.error(error instanceof ApiError ? error.message : '保存分类失败') }
}
async function disable(category) {
  try {
    await ElMessageBox.confirm(`停用分类“${category.name}”后，已有任务仍会保留该分类。`, '停用分类', { type: 'warning' })
    await todoApi.disableCategory(category.id)
    await load()
    ElMessage.success('分类已停用')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(error instanceof ApiError ? error.message : '停用分类失败')
  }
}
async function enable(category) {
  const color = normalizeCategoryColor(category.color)
  if (!color) {
    ElMessage.error('分类颜色格式无效')
    return
  }
  try {
    await todoApi.updateCategory(category.id, {
      name: category.name,
      color,
      sortOrder: category.sortOrder,
      active: true,
    })
    await load()
    ElMessage.success('分类已启用')
  } catch (error) { ElMessage.error(error instanceof ApiError ? error.message : '启用分类失败') }
}
onMounted(load)
</script>

<template>
  <AppShell title="任务分类">
    <template #actions><el-button type="primary" :icon="Plus" @click="open()">新增分类</el-button></template>
    <el-table :data="categories" v-loading="loading" class="data-table">
      <el-table-column label="分类" min-width="220"><template #default="{ row }"><span class="category-cell"><i :style="{ background: row.color }" />{{ row.name }}</span></template></el-table-column>
      <el-table-column label="颜色" width="120"><template #default="{ row }"><span class="category-color-swatch" :style="{ backgroundColor: row.color }" :title="row.color" role="img" :aria-label="`颜色 ${row.color}`" /></template></el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="100" />
      <el-table-column label="状态" width="110"><template #default="{ row }"><el-tag :type="row.active ? 'success' : 'info'">{{ row.active ? '启用' : '已停用' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="200"><template #default="{ row }"><el-button link type="primary" :icon="EditPen" @click="open(row)">编辑</el-button><el-button v-if="row.active" link type="danger" @click="disable(row)">停用</el-button><el-button v-else link type="success" @click="enable(row)">启用</el-button></template></el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" :title="editing ? '编辑分类' : '新增分类'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="分类名称" prop="name"><el-input v-model="form.name" maxlength="64" /></el-form-item>
        <el-form-item label="颜色" prop="color"><ColorPalettePicker v-model="form.color" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.active" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible = false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </AppShell>
</template>

<style scoped>
.category-color-swatch { display: inline-block; width: 32px; height: 22px; border: 1px solid rgb(16 24 40 / 20%); border-radius: 3px; box-shadow: inset 0 0 0 1px rgb(255 255 255 / 25%); }
</style>
