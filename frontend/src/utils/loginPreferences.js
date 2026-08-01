export const DEFAULT_LOGIN_USERNAME = ''
export const LAST_LOGIN_USERNAME_KEY = 'todo_last_login_username'

function getStorage(storage) {
  return storage ?? globalThis.localStorage
}

export function loadLastLoginUsername(storage) {
  try {
    const username = getStorage(storage)?.getItem(LAST_LOGIN_USERNAME_KEY)?.trim()
    return username || DEFAULT_LOGIN_USERNAME
  } catch {
    return DEFAULT_LOGIN_USERNAME
  }
}

export function rememberLoginUsername(username, storage) {
  try {
    const normalizedUsername = String(username ?? '').trim()
    if (normalizedUsername) getStorage(storage)?.setItem(LAST_LOGIN_USERNAME_KEY, normalizedUsername)
  } catch {
    // Local storage may be unavailable or blocked by the browser.
  }
}
