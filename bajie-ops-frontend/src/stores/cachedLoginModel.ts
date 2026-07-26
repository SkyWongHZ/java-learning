import { CacheTool } from '@/utils/cache-util'
import type { LoginModel } from '@/views/LoginView/LoginModel'
import { defineStore } from 'pinia'
import { computed, ref, type Ref } from 'vue'

export const useCachedLoginModelStore = defineStore(
  'loginModelStore',
  () => {
    const loginModel: Ref<LoginModel | null> = ref(null)

    const token = computed(() => {
      return loginModel.value?.token ?? CacheTool.getLoginInfo()?.token
    })

    const userName = computed(() => {
      const model = loginModel.value?.model ?? CacheTool.getLoginInfo()?.model
      return model?.displayName || model?.username || ''
    })

    function save(tempLoginInfo: LoginModel) {
      loginModel.value = tempLoginInfo
      CacheTool.saveLoginInfo(loginModel.value)
    }

    function remove() {
      loginModel.value = null
      CacheTool.removeLoginInfo()
    }

    return {
      loginModel,
      token,
      userName,
      save,
      remove
    }
  },
  {
    persist: true
  }
)
