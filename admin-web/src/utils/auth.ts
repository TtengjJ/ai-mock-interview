const TOKEN_KEY = 'admin_token'
const NAME_KEY = 'admin_name'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(NAME_KEY)
}

export function getAdminName(): string {
  return localStorage.getItem(NAME_KEY) || '管理员'
}

export function setAdminName(name: string) {
  localStorage.setItem(NAME_KEY, name)
}
