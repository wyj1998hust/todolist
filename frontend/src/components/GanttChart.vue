<script setup>
import { computed } from 'vue'
import { buildTimeline, differenceInDays, formatMonth, parseDate, toDateKey } from '../utils/gantt'

const props = defineProps({ tasks: { type: Array, default: () => [] } })
const emit = defineEmits(['select'])

const timeline = computed(() => buildTimeline(props.tasks))
const dayWidth = computed(() => {
  if (timeline.value.length <= 45) return 42
  if (timeline.value.length <= 120) return 30
  return 22
})
const timelineWidth = computed(() => Math.max(timeline.value.length * dayWidth.value, 720))
const monthLabels = computed(() => timeline.value
  .map((date, index) => ({ date, index }))
  .filter(({ date, index }) => index === 0 || date.getDate() === 1))
const sortedTasks = computed(() => [...props.tasks].sort((left, right) =>
  left.startDate.localeCompare(right.startDate) || left.deadline.localeCompare(right.deadline) || left.id - right.id))

function barStyle(task) {
  const start = parseDate(task.startDate)
  const end = parseDate(task.deadline)
  const origin = timeline.value[0]
  const left = differenceInDays(start, origin) * dayWidth.value
  const width = (differenceInDays(end, start) + 1) * dayWidth.value
  return {
    left: `${left}px`,
    width: `${Math.max(width, dayWidth.value)}px`,
    '--task-color': task.status === 'completed' ? '#16a34a' : task.category?.color || '#64748b',
  }
}

function dayClass(date) {
  const day = date.getDay()
  return { weekend: day === 0 || day === 6, 'month-start': date.getDate() === 1 }
}

function dateLabel(date) {
  return `${date.getMonth() + 1}/${date.getDate()}`
}

function todayOffset() {
  if (!timeline.value.length) return null
  const offset = differenceInDays(parseDate(toDateKey(new Date())), timeline.value[0])
  return offset >= 0 && offset < timeline.value.length ? offset * dayWidth.value : null
}
</script>

<template>
  <div v-if="!tasks.length" class="empty-gantt">
    暂无符合筛选条件的任务。新增任务后会在这里汇总展示。
  </div>
  <div v-else class="gantt-wrap">
    <section class="gantt-meta" aria-label="任务信息">
      <header class="gantt-header gantt-meta-header">
        <span>任务 / 分类</span><span>跟进人</span><span>状态</span><span>进度</span>
      </header>
      <button v-for="task in sortedTasks" :key="task.id" class="gantt-meta-row" type="button" @click="emit('select', task)">
        <span class="task-name"><i :style="{ background: task.category?.color || '#64748b' }" />{{ task.title }}<small>{{ task.category?.name || '未分类' }}</small></span>
        <span>{{ task.assignee?.displayName || task.legacyAssignee || '未分配' }}</span>
        <span><em :class="`status-${task.status}`">{{ task.status === 'completed' ? '已完成' : task.status === 'in_progress' ? '进行中' : '未开始' }}</em></span>
        <span>{{ task.progress }}%</span>
      </button>
    </section>
    <section class="gantt-scroller" aria-label="甘特图时间轴">
      <div class="gantt-timeline" :style="{ width: `${timelineWidth}px` }">
        <header class="gantt-header gantt-time-header">
          <div v-for="month in monthLabels" :key="`${month.index}-${formatMonth(month.date)}`" class="month-label" :style="{ left: `${month.index * dayWidth}px` }">
            {{ formatMonth(month.date) }}
          </div>
          <div v-for="date in timeline" :key="toDateKey(date)" class="day-label" :class="dayClass(date)" :style="{ width: `${dayWidth}px` }">{{ dateLabel(date) }}</div>
        </header>
        <div v-for="task in sortedTasks" :key="task.id" class="gantt-track">
          <span v-for="date in timeline" :key="toDateKey(date)" class="day-grid" :class="dayClass(date)" :style="{ width: `${dayWidth}px` }" />
          <button class="gantt-bar" type="button" :style="barStyle(task)" :title="`${task.title}: ${task.startDate} 至 ${task.deadline}`" @click="emit('select', task)">
            <span class="gantt-progress" :style="{ width: `${task.progress}%` }" />
            <strong>{{ task.title }}</strong>
          </button>
        </div>
        <span v-if="todayOffset() !== null" class="today-marker" :style="{ left: `${todayOffset()}px` }" title="今天" />
      </div>
    </section>
  </div>
</template>
