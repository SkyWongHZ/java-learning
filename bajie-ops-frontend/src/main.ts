import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import App from './App.vue'
import router from './router'
import '@/scss/reset.scss'
import '@/scss/fonts.scss' // 导入字体定义
import '@/scss/global.scss'
import '@/scss/element-overrides.scss'
import '@/scss/management.scss'
import 'element-plus/theme-chalk/el-message.css'

const app = createApp(App)
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)
app.use(pinia)
app.use(router)

app.mount('#app')
