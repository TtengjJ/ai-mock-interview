const TOKEN_KEY = 'candidate_token'
const NAME_KEY = 'candidate_name'

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

export function getCandidateName(): string {
  return localStorage.getItem(NAME_KEY) || '候选人'
}

export function setCandidateName(name: string) {
  localStorage.setItem(NAME_KEY, name)
}
