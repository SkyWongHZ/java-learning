import service from './axios'
import type { IRequestConfig } from './i-request-config'

export * from './i-response'

function filteredParams(params: object) {
  return Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== '')
  )
}

function request<T>(config: IRequestConfig): Promise<T> {
  return service.request(config) as Promise<T>
}

const http = {
  get<T>(url: string, params: object = {}, isLoading = false, config?: IRequestConfig) {
    return request<T>({ url, params: filteredParams(params), isLoading, ...config, method: 'GET' })
  },
  post<T>(url: string, data: unknown = {}, isLoading = false, config?: IRequestConfig) {
    return request<T>({ url, data, isLoading, ...config, method: 'POST' })
  },
  put<T>(url: string, data: unknown = {}, isLoading = false, config?: IRequestConfig) {
    return request<T>({ url, data, isLoading, ...config, method: 'PUT' })
  },
  delete<T>(url: string, isLoading = false, config?: IRequestConfig) {
    return request<T>({ url, isLoading, ...config, method: 'DELETE' })
  }
}

export default http
