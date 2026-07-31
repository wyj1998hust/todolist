export function parseDate(value) {
  if (!value) return null
  const [year, month, day] = value.split('-').map(Number)
  return new Date(year, month - 1, day)
}

export function toDateKey(date) {
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function addDays(date, count) {
  const result = new Date(date)
  result.setDate(result.getDate() + count)
  return result
}

export function differenceInDays(left, right) {
  const leftUtc = Date.UTC(left.getFullYear(), left.getMonth(), left.getDate())
  const rightUtc = Date.UTC(right.getFullYear(), right.getMonth(), right.getDate())
  return Math.round((leftUtc - rightUtc) / 86_400_000)
}

export function buildTimeline(tasks) {
  const taskDates = tasks
    .flatMap((task) => [parseDate(task.startDate), parseDate(task.deadline)])
    .filter(Boolean)
  if (!taskDates.length) return []
  const min = addDays(new Date(Math.min(...taskDates.map((date) => date.getTime()))), -2)
  const max = addDays(new Date(Math.max(...taskDates.map((date) => date.getTime()))), 3)
  const days = []
  for (let current = min; current <= max; current = addDays(current, 1)) {
    days.push(current)
  }
  return days
}

export function formatMonth(date) {
  return `${date.getFullYear()}年${date.getMonth() + 1}月`
}
