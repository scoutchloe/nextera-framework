import './assets/main.css'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import { setupPermissionDirectives } from '@/directives/permission'

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
app.use(ElementPlus, {
  size: 'default',
  zIndex: 3000,
})

// 初始化用户store
import { useUserStore } from '@/stores/user'
const userStore = useUserStore()
userStore.initStore()

// 挂载应用
app.mount('#app')
