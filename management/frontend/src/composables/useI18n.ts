import { useI18n as useVueI18n } from 'vue-i18n'
import { useAppStore } from '@/stores/app'
import { watch } from 'vue'
import type { SupportedLocale } from '@/types'

export function useI18n() {
  const { t, locale } = useVueI18n()
  const appStore = useAppStore()

  // 监听 store 中的语言变化，同步到 i18n
  watch(
    () => appStore.locale,
    (newLocale) => {
      locale.value = newLocale
    },
    { immediate: true }
  )

  // 切换语言
  const changeLanguage = (newLocale: SupportedLocale) => {
    appStore.setLocale(newLocale)
    locale.value = newLocale
  }

  // 获取当前语言显示名称
  const getCurrentLanguageName = () => {
    return locale.value === 'zh-CN' ? '中文' : 'English'
  }

  // 获取下一个语言显示名称
  const getNextLanguageName = () => {
    return locale.value === 'zh-CN' ? 'English' : '中文'
  }

  return {
    t,
    locale,
    changeLanguage,
    toggleLanguage: appStore.toggleLocale,
    getCurrentLanguageName,
    getNextLanguageName
  }
} 