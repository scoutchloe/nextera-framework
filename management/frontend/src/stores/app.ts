import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { ThemeMode, SidebarStatus, DeviceType } from '@/types'

export const useAppStore = defineStore('app', () => {
  // 状态
  const theme = ref<ThemeMode>('light')
  const sidebarStatus = ref<SidebarStatus>('opened')
  const device = ref<DeviceType>('desktop')
  const loading = ref(false)
  const breadcrumbs = ref<Array<{ title: string; path?: string }>>([])

  // 计算属性
  const isDark = computed(() => theme.value === 'dark')
  const isCollapsed = computed(() => sidebarStatus.value === 'closed')
  const isMobile = computed(() => device.value === 'mobile')
  const isTablet = computed(() => device.value === 'tablet')
  const isDesktop = computed(() => device.value === 'desktop')

  // 从localStorage恢复配置
  const initApp = () => {
    const storedTheme = localStorage.getItem('theme') as ThemeMode
    const storedSidebarStatus = localStorage.getItem('sidebarStatus') as SidebarStatus
    
    if (storedTheme) {
      theme.value = storedTheme
      applyTheme(storedTheme)
    } else {
      // 检测系统主题偏好
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      const defaultTheme = prefersDark ? 'dark' : 'light'
      setTheme(defaultTheme)
    }
    
    if (storedSidebarStatus) {
      sidebarStatus.value = storedSidebarStatus
    }

    // 检测设备类型
    detectDevice()
    
    // 监听窗口大小变化
    window.addEventListener('resize', detectDevice)
    
    // 监听系统主题变化
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    mediaQuery.addEventListener('change', (e) => {
      if (theme.value === 'auto') {
        applyTheme(e.matches ? 'dark' : 'light')
      }
    })
  }

  // 设置主题
  const setTheme = (newTheme: ThemeMode) => {
    theme.value = newTheme
    localStorage.setItem('theme', newTheme)
    applyTheme(newTheme)
  }

  // 应用主题
  const applyTheme = (themeMode: ThemeMode) => {
    const root = document.documentElement
    
    if (themeMode === 'dark') {
      root.setAttribute('data-theme', 'dark')
      root.classList.add('dark')
    } else if (themeMode === 'light') {
      root.setAttribute('data-theme', 'light')
      root.classList.remove('dark')
    } else {
      // auto mode
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      if (prefersDark) {
        root.setAttribute('data-theme', 'dark')
        root.classList.add('dark')
      } else {
        root.setAttribute('data-theme', 'light')
        root.classList.remove('dark')
      }
    }
  }

  // 切换主题
  const toggleTheme = () => {
    const newTheme = theme.value === 'light' ? 'dark' : 'light'
    setTheme(newTheme)
  }

  // 设置侧边栏状态
  const setSidebarStatus = (status: SidebarStatus) => {
    sidebarStatus.value = status
    localStorage.setItem('sidebarStatus', status)
  }

  // 切换侧边栏
  const toggleSidebar = () => {
    const newStatus = sidebarStatus.value === 'opened' ? 'closed' : 'opened'
    setSidebarStatus(newStatus)
  }

  // 检测设备类型
  const detectDevice = () => {
    const width = window.innerWidth
    
    if (width < 768) {
      device.value = 'mobile'
      // 移动设备自动收起侧边栏
      if (sidebarStatus.value === 'opened') {
        setSidebarStatus('closed')
      }
    } else if (width < 1024) {
      device.value = 'tablet'
    } else {
      device.value = 'desktop'
    }
  }

  // 设置加载状态
  const setLoading = (isLoading: boolean) => {
    loading.value = isLoading
  }

  // 设置面包屑
  const setBreadcrumbs = (crumbs: Array<{ title: string; path?: string }>) => {
    breadcrumbs.value = crumbs
  }

  // 添加面包屑项
  const addBreadcrumb = (crumb: { title: string; path?: string }) => {
    breadcrumbs.value.push(crumb)
  }

  // 清空面包屑
  const clearBreadcrumbs = () => {
    breadcrumbs.value = []
  }

  return {
    // 状态
    theme,
    sidebarStatus,
    device,
    loading,
    breadcrumbs,
    
    // 计算属性
    isDark,
    isCollapsed,
    isMobile,
    isTablet,
    isDesktop,
    
    // 方法
    initApp,
    setTheme,
    toggleTheme,
    setSidebarStatus,
    toggleSidebar,
    detectDevice,
    setLoading,
    setBreadcrumbs,
    addBreadcrumb,
    clearBreadcrumbs
  }
}) 