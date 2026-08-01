import { describe, expect, it } from 'vitest'
import {
  DEFAULT_LOGIN_USERNAME,
  LAST_LOGIN_USERNAME_KEY,
  loadLastLoginUsername,
  rememberLoginUsername,
} from './loginPreferences'

function createStorage(initial = {}) {
  const values = { ...initial }
  return {
    getItem: (key) => values[key] ?? null,
    setItem: (key, value) => { values[key] = String(value) },
  }
}

describe('login preferences', () => {
  it('uses an empty username when there is no saved username', () => {
    expect(loadLastLoginUsername(createStorage())).toBe('')
    expect(DEFAULT_LOGIN_USERNAME).toBe('')
  })

  it('loads and normalizes the saved username', () => {
    const storage = createStorage({ [LAST_LOGIN_USERNAME_KEY]: '  member1  ' })
    expect(loadLastLoginUsername(storage)).toBe('member1')
  })

  it('remembers only the normalized username', () => {
    const storage = createStorage()
    rememberLoginUsername('  member1  ', storage)
    expect(storage.getItem(LAST_LOGIN_USERNAME_KEY)).toBe('member1')
  })

  it('falls back safely when storage is unavailable', () => {
    const unavailableStorage = {
      getItem: () => { throw new Error('storage blocked') },
      setItem: () => { throw new Error('storage blocked') },
    }
    expect(loadLastLoginUsername(unavailableStorage)).toBe('')
    expect(() => rememberLoginUsername('member1', unavailableStorage)).not.toThrow()
  })
})
