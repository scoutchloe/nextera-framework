import { createI18n } from 'vue-i18n'
import zhCN from './locales/zh-CN'
import enUS from './locales/en-US'
import type { SupportedLocale } from '@/types'

export type { SupportedLocale }

// 检测浏览器语言
const getBrowserLanguage = (): SupportedLocale => {
  const browserLang = navigator.language.toLowerCase()
  
  // 检查是否为中文
  if (browserLang.includes('zh')) {
    return 'zh-CN'
  }
  
  // 检查是否为英文
  if (browserLang.includes('en')) {
    return 'en-US'
  }
  
  // 默认返回中文
  return 'zh-CN'
}

// 获取存储的语言或检测浏览器语言
const getStoredLanguage = (): SupportedLocale => {
  const stored = localStorage.getItem('locale') as SupportedLocale
  if (stored && ['zh-CN', 'en-US'].includes(stored)) {
    return stored
  }
  return getBrowserLanguage()
}

// 创建 i18n 实例
const i18n = createI18n({
  legacy: false, // 使用 Composition API 模式
  locale: getStoredLanguage(), // 设置语言
  fallbackLocale: 'zh-CN', // 设置备用语言
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS
  }
})

export default i18n 