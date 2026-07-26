import axios from 'axios'
import type { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElLoading } from 'element-plus'
import type { IRequestConfig } from './i-request-config'
import type { IResponse } from './i-response'
import { useCachedLoginModelStore } from '@/stores/cachedLoginModel'
import { logout } from '@/utils/common-util'

const service = axios.create({
  baseURL: import.meta.env.VITE_BASE_URL,
  timeout: 60_000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

let loadingCount = 0
let loadingInstance: ReturnType<typeof ElLoading.service> | undefined

function openLoading(config: IRequestConfig) {
  if (!config.isLoading) return
  loadingCount += 1
  if (loadingCount === 1) {
    loadingInstance = ElLoading.service({ lock: true, customClass: 'loading' })
  }
}

function closeLoading(config?: IRequestConfig) {
  if (!config?.isLoading) return
  loadingCount = Math.max(loadingCount - 1, 0)
  if (loadingCount === 0) {
    loadingInstance?.close()
    loadingInstance = undefined
  }
}

service.interceptors.request.use(
  (config) => {
    config.headers.system = '1'
    const token = useCachedLoginModelStore().token
    if (token) config.headers.token = token
    openLoading(config)
    return config
  },
  (error: AxiosError) => Promise.reject(error)
)

service.interceptors.response.use(
  (response: AxiosResponse<IResponse>) => {
    closeLoading(response.config)
    const body = response.data
    if (body.code === 1) return body.data
    if ([8, 11].includes(body.code)) logout()
    return Promise.reject(body.msg || '请求失败')
  },
  (error: AxiosError<IResponse>) => {
    closeLoading(error.config)
    const message = error.response?.data?.msg || (error.code === 'ECONNABORTED' ? '请求超时' : '服务器出错')
    return Promise.reject(message)
  }
)

export type { AxiosRequestConfig, AxiosResponse }
export default service
