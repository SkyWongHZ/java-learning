<template>
  <form class="password" @submit.prevent="onLogin">
    <div class="password-fields">
      <label class="password-field">
        <span class="password-field-title">账号</span>
        <login-input
          class="password-input"
          v-model:value="states.username"
          :icon="accountIcon"
          placeholder="请输入账号"
        />
      </label>
      <label class="password-field">
        <span class="password-field-title">密码</span>
        <login-input
          class="password-input"
          v-model:value="states.password"
          type="password"
          :icon="passwordIcon"
          placeholder="请输入密码"
        />
      </label>
    </div>
    <button class="password-login" type="submit" :disabled="loading">
      {{ loading ? '登录中...' : '登 录' }}
    </button>
  </form>
</template>

<script lang="ts" setup>
import LoginInput from '@/components/login/LoginInput.vue'
import accountIcon from '@/assets/login-account.png'
import passwordIcon from '@/assets/login-password.png'
import { reactive, ref } from 'vue'
import { showFailureToast } from '@/utils/toast-util'
import type { ILoginRequest } from '@/interfaces/request/IPasswordLoginRequest'
import type { ILogin } from '@/interfaces/response/ILogin'
import { API } from '@/api'
import { validateEmpty } from '@/utils/validate-util'

defineOptions({ name: 'LoginPassword' })

const emits = defineEmits<{
  onLoginSucceed: [value: ILogin]
}>()

const states = reactive({
  username: '',
  password: ''
})
const loading = ref(false)

async function onLogin() {
  if (loading.value) return
  try {
    const params: ILoginRequest = {
      username: validateEmpty('账号', states.username.trim()),
      password: validateEmpty('密码', states.password)
    }
    loading.value = true
    emits('onLoginSucceed', await API.login(params))
  } catch (error) {
    showFailureToast(error)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.password {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: stretch;

  &-fields {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 18px;
  }

  &-field {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 10px;
  }

  &-field-title {
    color: #596273;
    font-size: 14px;
    font-weight: 500;
    line-height: 20px;
  }

  &-input {
    width: 100%;
  }

  &-login {
    width: 100%;
    height: 48px;
    margin-top: 28px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 10px;
    background: $theme-color;
    box-shadow: 0 12px 24px -10px $theme-color;
    color: #ffffff;
    font-size: 15px;
    font-weight: 600;

    &:disabled {
      cursor: not-allowed;
      opacity: 0.65;
    }
  }
}
</style>
