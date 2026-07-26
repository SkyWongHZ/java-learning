import { CacheTool } from './cache-util'
import router from '@/router'

export function logout() {
  CacheTool.removeAll()
  router.push('/login')
}

export function isEmpty(value: any): boolean {
  return (
    value === null ||
    value === undefined ||
    value === '' ||
    (typeof value === 'number' && isNaN(value)) ||
    (Array.isArray(value) && !value.length)
  )
}
