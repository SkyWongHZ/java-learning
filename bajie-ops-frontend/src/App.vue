<template>
  <ElConfigProvider :locale="locale">
    <router-view></router-view>
  </ElConfigProvider>
</template>

<script lang="ts" setup>
import { ElConfigProvider } from 'element-plus'

import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'dayjs/locale/zh-cn'
import { loadFonts } from '@/utils/font-loader'
import { onMounted } from 'vue'

const locale = zhCn

onMounted(() => {
  // 延迟加载字体，优先渲染内容
  setTimeout(() => {
    loadFonts()
  }, 200)
})

</script>

<style scoped lang="scss">

</style>
