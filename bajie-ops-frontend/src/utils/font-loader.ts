// src/utils/font-loader.ts
// 导入字体文件，让Vite正确处理路径
import regularFontUrl from '@/assets/fonts/SourceHanSansCN-Regular.otf'
import mediumFontUrl from '@/assets/fonts/SourceHanSansCN-Medium.otf'

export async function loadFonts() {
    try {
        if ('fonts' in document) {
            // 使用导入的字体URL，Vite会自动处理哈希
            const regularFont = new FontFace('Source Han Sans SC', `url(${regularFontUrl})`, {
                weight: '400',
                display: 'swap'
            })

            const mediumFont = new FontFace('Source Han Sans SC', `url(${mediumFontUrl})`, {
                weight: '500',
                display: 'swap'
            })

            await Promise.all([regularFont.load(), mediumFont.load()])
            document.fonts.add(regularFont)
            document.fonts.add(mediumFont)

            document.documentElement.classList.add('font-loaded')
            return true
        }
        return false
    } catch (error) {
        console.error('字体加载失败:', error)
        return false
    }
}