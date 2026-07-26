import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// https://vitejs.dev/config/
export default defineConfig({
  base: '/operation/',
  plugins: [
    vue(),
    AutoImport({
      imports: ['vue', 'vue-router'],
      resolvers: [ElementPlusResolver()],
      dts: './auto-import.d.ts',
      eslintrc: {
        enabled: false, // 1、改为true用于生成eslint配置。2、生成后改回false，避免重复生成消耗
        filepath: './.eslintrc-auto-import.json'
      }
    }),
    Components({
      resolvers: [
        ElementPlusResolver({
          importStyle: 'sass'
        })
      ]
    })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        api: 'modern-compiler',
        charset: false,
        additionalData: `@use "@/scss/constants.scss" as *;`
      }
    }
  },
  build: {
    terserOptions: {
      compress: {
        drop_console: true, // 生产环境移除console
        drop_debugger: true // 生产环境移除debugger
      }
    },
    assetsInlineLimit: 4096,
    rollupOptions: {
      output: {
        assetFileNames: (assetInfo) => {
          // 字体文件输出到单独的 fonts 目录
          if (/\.(woff2?|eot|ttf|otf)$/.test(assetInfo.name ?? '')) {
            return 'assets/fonts/[name].[hash][extname]'
          }
          return 'assets/[name].[hash][extname]'
        }
      }
    }
  }
})
