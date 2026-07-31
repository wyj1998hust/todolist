import { describe, expect, it } from 'vitest'
import { normalizeCategoryColor } from './categoryColor'

describe('category color normalization', () => {
  it('returns six-digit colors in uppercase', () => {
    expect(normalizeCategoryColor('#ff5733')).toBe('#FF5733')
  })

  it('expands shorthand hex colors', () => {
    expect(normalizeCategoryColor('#abc')).toBe('#AABBCC')
  })

  it('rejects rgba and other invalid color formats', () => {
    expect(normalizeCategoryColor('rgba(255, 87, 51, 1)')).toBeNull()
    expect(normalizeCategoryColor('#GG5733')).toBeNull()
  })
})
