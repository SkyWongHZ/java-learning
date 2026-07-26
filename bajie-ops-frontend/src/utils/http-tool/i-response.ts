// src/utils/http/types.ts

// 和后端约定好接口返回的数据结构
export interface IResponse<T = any> {
  code: number
  msg: string
  errorDetail: string | null
  data: T | null
  tid: string
}
