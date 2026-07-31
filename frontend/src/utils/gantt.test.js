import { describe, expect, it } from 'vitest'
import { buildTimeline, differenceInDays, parseDate } from './gantt'

describe('gantt date helpers', () => {
  it('counts inclusive task days correctly', () => {
    expect(differenceInDays(parseDate('2026-08-03'), parseDate('2026-08-01')) + 1).toBe(3)
  })

  it('adds a small timeline padding around task dates', () => {
    const timeline = buildTimeline([{ startDate: '2026-08-01', deadline: '2026-08-02' }])
    expect(timeline).toHaveLength(7)
  })
})
