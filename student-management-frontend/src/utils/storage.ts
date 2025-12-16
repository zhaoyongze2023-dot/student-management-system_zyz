const TOKEN_KEY = 'student_management_token'
const REFRESH_TOKEN_KEY = 'student_management_refresh_token'
const USER_KEY = 'student_management_user'

export const storage = {
  // Token
  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY)
  },
  setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token)
  },
  removeToken(): void {
    localStorage.removeItem(TOKEN_KEY)
  },

  // Refresh Token
  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY)
  },
  setRefreshToken(token: string): void {
    localStorage.setItem(REFRESH_TOKEN_KEY, token)
  },
  removeRefreshToken(): void {
    localStorage.removeItem(REFRESH_TOKEN_KEY)
  },

  // User
  getUser<T>(): T | null {
    const user = localStorage.getItem(USER_KEY)
    return user ? JSON.parse(user) : null
  },
  setUser<T>(user: T): void {
    localStorage.setItem(USER_KEY, JSON.stringify(user))
  },
  removeUser(): void {
    localStorage.removeItem(USER_KEY)
  },

  // 清除所有认证信息
  clearAuth(): void {
    this.removeToken()
    this.removeRefreshToken()
    this.removeUser()
  }
}
