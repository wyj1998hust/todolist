const HEX_COLOR_PATTERN = /^#[0-9a-f]{6}$/i
const SHORT_HEX_COLOR_PATTERN = /^#[0-9a-f]{3}$/i

export function normalizeCategoryColor(value) {
  const color = String(value ?? '').trim()
  if (HEX_COLOR_PATTERN.test(color)) return color.toUpperCase()
  if (SHORT_HEX_COLOR_PATTERN.test(color)) {
    return `#${color.slice(1).split('').map((part) => `${part}${part}`).join('')}`.toUpperCase()
  }
  return null
}
