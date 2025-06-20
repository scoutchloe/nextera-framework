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

  // 屏幕尺寸状态
  const screenWidth = ref(window.innerWidth)
  const screenHeight = ref(window.innerHeight)

  // 计算属性
  const isDark = computed(() => theme.value === 'dark')
  const isCollapsed = computed(() => sidebarStatus.value === 'closed')
  const isMobile = computed(() => device.value === 'mobile')
  const isTablet = computed(() => device.value === 'tablet')
  const isDesktop = computed(() => device.value === 'desktop')
  
  // 屏幕尺寸计算属性
  const isSmallMobile = computed(() => screenWidth.value < 480)
  const isMediumMobile = computed(() => screenWidth.value >= 480 && screenWidth.value < 768)
  const isSmallTablet = computed(() => screenWidth.value >= 768 && screenWidth.value < 1024)
  const isLargeTablet = computed(() => screenWidth.value >= 1024 && screenWidth.value < 1280)
  const isSmallDesktop = computed(() => screenWidth.value >= 1280 && screenWidth.value < 1440)
  const isLargeDesktop = computed(() => screenWidth.value >= 1440 && screenWidth.value < 1920)
  const isXLDesktop = computed(() => screenWidth.value >= 1920)
  
  // 横屏检测
  const isLandscape = computed(() => screenWidth.value > screenHeight.value)
  const isPortrait = computed(() => screenWidth.value <= screenHeight.value)

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
    window.addEventListener('resize', handleResize)
    
    // 监听系统主题变化
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    mediaQuery.addEventListener('change', (e) => {
      if (theme.value === 'auto') {
        applyTheme(e.matches ? 'dark' : 'light')
      }
    })

    // 监听设备方向变化
    window.addEventListener('orientationchange', () => {
      setTimeout(() => {
        detectDevice()
      }, 100)
    })
  }

  // 处理窗口大小变化
  const handleResize = () => {
    screenWidth.value = window.innerWidth
    screenHeight.value = window.innerHeight
    detectDevice()
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
    const height = window.innerHeight
    const userAgent = navigator.userAgent.toLowerCase()
    
    // 更新屏幕尺寸
    screenWidth.value = width
    screenHeight.value = height
    
    // 检测是否为触摸设备
    const isTouchDevice = 'ontouchstart' in window || navigator.maxTouchPoints > 0
    
    // 检测设备类型
    const isMobileUA = /android|webos|iphone|ipad|ipod|blackberry|iemobile|opera mini/i.test(userAgent)
    const isTabletUA = /ipad|android(?!.*mobile)|tablet/i.test(userAgent)
    
    if (width < 768 || (isMobileUA && !isTabletUA)) {
      device.value = 'mobile'
      // 移动设备自动收起侧边栏
      if (sidebarStatus.value === 'opened') {
        setSidebarStatus('closed')
      }
    } else if (width < 1024 || (isTabletUA && isTouchDevice)) {
      device.value = 'tablet'
      // 平板设备在竖屏时收起侧边栏
      if (width < height && sidebarStatus.value === 'opened') {
        setSidebarStatus('closed')
      }
    } else {
      device.value = 'desktop'
      // 桌面设备在足够宽度时展开侧边栏
      if (width >= 1280 && sidebarStatus.value === 'closed') {
        const storedSidebarStatus = localStorage.getItem('sidebarStatus') as SidebarStatus
        if (!storedSidebarStatus || storedSidebarStatus === 'opened') {
          setSidebarStatus('opened')
        }
      }
    }

    // 特殊处理：横屏移动设备
    if (isMobile.value && width > height && width >= 640) {
      // 在横屏模式下，可以考虑显示更多内容
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

  // 获取当前断点信息
  const getBreakpointInfo = () => {
    return {
      width: screenWidth.value,
      height: screenHeight.value,
      device: device.value,
      isSmallMobile: isSmallMobile.value,
      isMediumMobile: isMediumMobile.value,
      isSmallTablet: isSmallTablet.value,
      isLargeTablet: isLargeTablet.value,
      isSmallDesktop: isSmallDesktop.value,
      isLargeDesktop: isLargeDesktop.value,
      isXLDesktop: isXLDesktop.value,
      isLandscape: isLandscape.value,
      isPortrait: isPortrait.value
    }
  }

  return {
    // 状态
    theme,
    sidebarStatus,
    device,
    loading,
    breadcrumbs,
    screenWidth,
    screenHeight,
    
    // 计算属性
    isDark,
    isCollapsed,
    isMobile,
    isTablet,
    isDesktop,
    isSmallMobile,
    isMediumMobile,
    isSmallTablet,
    isLargeTablet,
    isSmallDesktop,
    isLargeDesktop,
    isXLDesktop,
    isLandscape,
    isPortrait,
    
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
    clearBreadcrumbs,
    getBreakpointInfo
  }
}) 