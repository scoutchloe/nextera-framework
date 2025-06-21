import './assets/main.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import { setupPermissionDirectives } from '@/directives/permission'
import i18n from '@/i18n'

// 导入全局样式
// import '@/styles/variables.scss'
import '@/styles/global.scss'

// 创建应用实例
const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册权限指令
setupPermissionDirectives(app)

// 配置Pinia
const pinia = createPinia()

// 使用插件
app.use(pinia)
app.use(router)
app.use(i18n)
app.use(ElementPlus, {
  size: 'default',
  zIndex: 3000,
})

// 初始化应用store
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'

// 应用加载完成处理
const initializeApp = async () => {
  try {
    // 初始化应用store
    const appStore = useAppStore()
    appStore.initApp()
    
    // 同步语言设置到 i18n
    i18n.global.locale.value = appStore.locale
    
    // 初始化用户store
    const userStore = useUserStore()
    userStore.initStore()
    
    // 等待DOM加载完成
    await new Promise(resolve => {
      if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', resolve)
      } else {
        resolve(void 0)
      }
    })
    
    // 标记应用已加载完成，隐藏加载动画
    document.body.classList.add('app-loaded')
    
    // 移除加载容器
    setTimeout(() => {
      const loadingContainer = document.querySelector('.loading-container')
      if (loadingContainer) {
        loadingContainer.remove()
      }
    }, 300)
    
  } catch (error) {
    console.error('应用初始化失败:', error)
    // 即使初始化失败也要隐藏加载动画
    document.body.classList.add('app-loaded')
  }
}

// 挂载应用
app.mount('#app')

// 初始化应用
initializeApp()

// 处理视口变化（主要针对移动端）
let resizeTimer: number | null = null
const handleViewportChange = () => {
  if (resizeTimer) {
    clearTimeout(resizeTimer)
  }
  
  resizeTimer = window.setTimeout(() => {
    // 处理移动端100vh问题
    const vh = window.innerHeight * 0.01
    document.documentElement.style.setProperty('--vh', `${vh}px`)
    
    // 强制重绘以修复某些移动端浏览器的显示问题
    if (window.innerWidth <= 768) {
      document.body.style.height = `${window.innerHeight}px`
      setTimeout(() => {
        document.body.style.height = ''
      }, 0)
    }
  }, 100)
}

// 监听视口变化
window.addEventListener('resize', handleViewportChange)
window.addEventListener('orientationchange', () => {
  setTimeout(handleViewportChange, 100)
})

// 初始设置视口高度
handleViewportChange()

// PWA支持
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    // 这里可以注册Service Worker
    // navigator.serviceWorker.register('/sw.js')
  })
}

// 阻止双击缩放（移动端）
let lastTouchEnd = 0
document.addEventListener('touchend', (event) => {
  const now = (new Date()).getTime()
  if (now - lastTouchEnd <= 300) {
    event.preventDefault()
  }
  lastTouchEnd = now
}, false)

// 阻止默认的触摸行为（移动端优化）
document.addEventListener('touchstart', (event) => {
  if (event.touches.length > 1) {
    event.preventDefault()
  }
}, { passive: false })

// 处理移动端安全区域
const updateSafeArea = () => {
  const root = document.documentElement
  const safeAreaTop = getComputedStyle(root).getPropertyValue('env(safe-area-inset-top)') || '0px'
  const safeAreaBottom = getComputedStyle(root).getPropertyValue('env(safe-area-inset-bottom)') || '0px'
  const safeAreaLeft = getComputedStyle(root).getPropertyValue('env(safe-area-inset-left)') || '0px'
  const safeAreaRight = getComputedStyle(root).getPropertyValue('env(safe-area-inset-right)') || '0px'
  
  root.style.setProperty('--safe-area-top', safeAreaTop)
  root.style.setProperty('--safe-area-bottom', safeAreaBottom)
  root.style.setProperty('--safe-area-left', safeAreaLeft)
  root.style.setProperty('--safe-area-right', safeAreaRight)
}

// 更新安全区域
updateSafeArea()
window.addEventListener('resize', updateSafeArea)
