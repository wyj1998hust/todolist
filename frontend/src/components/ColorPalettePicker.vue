<script setup>
import { computed, ref } from 'vue'
import { normalizeCategoryColor } from '../utils/categoryColor'

defineOptions({ name: 'ColorPalettePicker' })

const props = defineProps({
  modelValue: { type: String, default: '#155EEF' },
})

const emit = defineEmits(['update:modelValue'])
const colorPopover = ref(null)
const customColorInput = ref(null)

const themeRows = [
  ['#FFFFFF', '#F2F2F2', '#D9E1F2', '#DDEBF7', '#E2F0D9', '#FFF2CC', '#FCE4D6', '#E4DFEC', '#F4CCCC', '#D9EAD3'],
  ['#000000', '#7F7F7F', '#4472C4', '#5B9BD5', '#70AD47', '#FFC000', '#ED7D31', '#8064A2', '#C00000', '#00B0F0'],
  ['#1F1F1F', '#595959', '#2F5597', '#2F75B5', '#548235', '#BF9000', '#C65911', '#5B2C83', '#7F0000', '#007A8A'],
  ['#404040', '#A5A5A5', '#8FAADC', '#9DC3E6', '#A9D18E', '#FFD966', '#F4B183', '#B4A7D6', '#E06666', '#76A5AF'],
  ['#D9D9D9', '#BFBFBF', '#B4C7E7', '#BDD7EE', '#C6E0B4', '#FFE699', '#F8CBAD', '#D9E1F2', '#F4CCCC', '#B7DEE8'],
]

const standardColors = ['#C00000', '#FF0000', '#FFC000', '#FFFF00', '#92D050', '#00B050', '#00B0F0', '#0070C0', '#002060', '#7030A0']
const selectedColor = computed(() => normalizeCategoryColor(props.modelValue) || '#155EEF')

function selectColor(color) {
  const normalized = normalizeCategoryColor(color)
  if (!normalized) return
  emit('update:modelValue', normalized)
  colorPopover.value?.hide()
}

function openCustomColor() {
  customColorInput.value?.click()
}

function handleCustomColor(event) {
  selectColor(event.target.value)
}
</script>

<template>
  <el-popover ref="colorPopover" placement="bottom-start" :width="276" trigger="click" :show-arrow="false">
    <template #reference>
      <button type="button" class="color-picker-trigger" :aria-label="`当前颜色 ${selectedColor}`">
        <span class="color-picker-trigger-swatch" :style="{ backgroundColor: selectedColor }" />
        <span>{{ selectedColor }}</span>
        <span class="color-picker-trigger-arrow">▾</span>
      </button>
    </template>

    <div class="office-color-picker">
      <div class="office-color-label">主题色</div>
      <div class="office-color-grid" role="listbox" aria-label="主题色">
        <button
          v-for="color in themeRows.flat()"
          :key="`theme-${color}`"
          type="button"
          class="office-color-cell"
          :class="{ selected: selectedColor === color }"
          :style="{ backgroundColor: color }"
          :aria-label="color"
          :aria-pressed="selectedColor === color"
          @click="selectColor(color)"
        />
      </div>

      <div class="office-color-label">标准色</div>
      <div class="office-color-grid" role="listbox" aria-label="标准色">
        <button
          v-for="color in standardColors"
          :key="`standard-${color}`"
          type="button"
          class="office-color-cell"
          :class="{ selected: selectedColor === color }"
          :style="{ backgroundColor: color }"
          :aria-label="color"
          :aria-pressed="selectedColor === color"
          @click="selectColor(color)"
        />
      </div>

      <button type="button" class="office-custom-color" @click="openCustomColor">其他颜色...</button>
      <input ref="customColorInput" class="office-native-color-input" type="color" :value="selectedColor" @change="handleCustomColor" />
    </div>
  </el-popover>
</template>

<style scoped>
.color-picker-trigger {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 150px;
  height: 34px;
  padding: 0 10px;
  color: #344054;
  font-size: 14px;
  background: #fff;
  border: 1px solid #d0d5dd;
  border-radius: 6px;
  cursor: pointer;
}

.color-picker-trigger:hover { border-color: #98a2b3; }
.color-picker-trigger-swatch { width: 22px; height: 22px; border: 1px solid rgb(16 24 40 / 20%); border-radius: 3px; }
.color-picker-trigger-arrow { margin-left: auto; color: #98a2b3; font-size: 13px; }
.office-color-picker { padding: 2px; }
.office-color-label { margin: 2px 0 7px; color: #667085; font-size: 12px; font-weight: 700; }
.office-color-grid { display: grid; grid-template-columns: repeat(10, 20px); gap: 4px; margin-bottom: 14px; }
.office-color-cell { width: 20px; height: 20px; padding: 0; border: 1px solid rgb(16 24 40 / 15%); border-radius: 2px; cursor: pointer; }
.office-color-cell:hover, .office-color-cell.selected { outline: 2px solid #155eef; outline-offset: 1px; }
.office-custom-color { width: 100%; padding: 7px 4px; color: #344054; text-align: left; background: transparent; border: 0; border-radius: 4px; cursor: pointer; }
.office-custom-color:hover { background: #f2f4f7; }
.office-native-color-input { position: absolute; width: 1px; height: 1px; opacity: 0; pointer-events: none; }
</style>
