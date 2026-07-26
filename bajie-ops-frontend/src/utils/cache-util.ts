import { useCachedLoginModelStore } from '@/stores/cachedLoginModel'
import type { LoginModel } from '@/views/LoginView/LoginModel'

class CacheTool {
  static LOGIN_INFO_KEY = 'loginInfo'

  // loginInfo
  static saveLoginInfo(loginInfo: LoginModel) {
    localStorage.setItem(CacheTool.LOGIN_INFO_KEY, JSON.stringify(loginInfo))
  }

  static removeLoginInfo() {
    localStorage.removeItem(CacheTool.LOGIN_INFO_KEY)
  }

  static getLoginInfo() {
    const loginInfo = localStorage.getItem(CacheTool.LOGIN_INFO_KEY)
    return loginInfo ? <LoginModel>JSON.parse(loginInfo) : null
  }

  // 退出登录时清空所有缓存
  static removeAll() {
    useCachedLoginModelStore().remove()
  }
}

export { CacheTool }
