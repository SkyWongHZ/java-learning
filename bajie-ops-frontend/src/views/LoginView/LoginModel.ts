import type { ILogin } from '@/interfaces/response/ILogin'

/**
 * 登录信息模型。脚手架阶段只保留 token 与基础用户信息，
 * 后续接入业务（如子系统权限树）时再在此扩展。
 */
export class LoginModel {
  token: string // 为了后端从 localStorage 中取得方便
  model: ILogin

  constructor(value: ILogin) {
    this.token = value.token
    this.model = value
  }
}
