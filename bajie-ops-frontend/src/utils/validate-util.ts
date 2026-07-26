import { isEmpty } from './common-util'

export function validateEmpty(title: string, value: any) {
  if (isEmpty(value)) {
    throw `${title}不能为空`
  }
  return value
}

export function validatePhoneNumber(phoneNumber: string) {
  const regExp = /^1[3456789]\d{9}$/
  if (!regExp.test(phoneNumber)) {
    throw '请输入正确的手机号'
  }
  return phoneNumber
}
