<template>
  <div class="main-layout" :class="{ 'is-collapsed': appStore.isCollapsed, 'is-mobile': appStore.isMobile }">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ 'is-collapse': appStore.isCollapsed }">
      <div class="sidebar-header">
        <div class="logo">
          <div class="logo-icon">
            <el-icon :size="28">
              <Avatar />
            </el-icon>
          </div>
          <transition name="logo-text">
            <span v-show="!appStore.isCollapsed" class="logo-text gradient-text">Nextera</span>
          </transition>
        </div>
      </div>
      
      <div class="sidebar-content">
        <sidebar-menu />
      </div>
    </aside>
    
    <!-- 移动端遮罩 -->
    <div v-if="appStore.isMobile && !appStore.isCollapsed" class="mobile-overlay" @click="appStore.toggleSidebar"></div>
    
    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 头部 -->
      <header class="header">
        <div class="header-left">
          <el-button 
            class="sidebar-toggle" 
            :icon="appStore.isCollapsed ? 'Expand' : 'Fold'"
            @click="appStore.toggleSidebar"
          />
          <app-breadcrumb />
        </div>
        
        <div class="header-right">
          <header-actions />
        </div>
      </header>
      
      <!-- 内容区域 -->
      <main class="content">
        <div class="content-wrapper">
          <router-view v-slot="{ Component, route }">
            <transition name="fade-transform" mode="out-in">
              <keep-alive :include="cachedViews">
                <component :is="Component" :key="route.path" />
              </keep-alive>
            </transition>
          </router-view>
        </div>
      </main>
      
      <!-- 底部 -->
      <footer class="footer">
        <div class="footer-content">
          <p>&copy; 2025 Nextera Management System. All rights reserved. Version 1.0.0</p>
        </div>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import SidebarMenu from '@/components/Sidebar/SidebarMenu.vue'
import AppBreadcrumb from '@/components/Header/AppBreadcrumb.vue'
import HeaderActions from '@/components/Header/HeaderActions.vue'

const appStore = useAppStore()
const userStore = useUserStore()

// 页面缓存
const cachedViews = ref<string[]>([])

// 处理窗口大小变化
const handleResize = () => {
  appStore.detectDevice()
}

onMounted(() => {
  // 初始化应用
  appStore.initApp()
  userStore.initStore()
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.main-layout {
  display: flex;
  height: 100vh;
  background-color: var(--bg-secondary);
  overflow: hidden;
  // margin-left: -200px;
}

.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1001;
  width: 250px;
  height: 100vh;
  background: var(--bg-primary);
  border-right: 1px solid var(--border-color);
  transition: all 0.3s ease;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);

  &.is-collapse {
    width: 64px;
  }

  .sidebar-header {
    height: 60px;
    display: flex;
    align-items: center;
    padding: 0 20px;
    border-bottom: 1px solid var(--border-color);
    
    .logo {
      display: flex;
      align-items: center;
      cursor: pointer;
      
      .logo-icon {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 40px;
        height: 40px;
        background: var(--gradient-tech);
        border-radius: 50%;
        color: white;
        margin-right: 12px;
        
        .is-collapse & {
          margin-right: 0;
        }
      }
      
      .logo-text {
        font-size: 1.5rem;
        font-weight: 700;
        white-space: nowrap;
      }
    }
  }
  
  .sidebar-content {
    height: calc(100vh - 60px);
    overflow-y: auto;
    
    &::-webkit-scrollbar {
      width: 4px;
    }
    
    &::-webkit-scrollbar-thumb {
      background: var(--border-dark);
      border-radius: 2px;
    }
  }
}

.mobile-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.3);
  z-index: 1000;
  backdrop-filter: blur(2px);
}

.main-content {
  flex: 1;
  margin-left: 200px;
  display: flex;
  flex-direction: column;
  height: 100vh;
  transition: margin-left 0.3s ease;
  
  .is-collapsed & {
    margin-left: 64px;
  }
  
  .is-mobile & {
    margin-left: 0;
  }
}

.header {
  height: 60px;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  flex-shrink: 0;
  z-index: 999;
  
  .header-left {
    display: flex;
    align-items: center;
    flex: 1;
    
    .sidebar-toggle {
      margin-right: 16px;
      color: var(--text-secondary);
      
      &:hover {
        color: var(--primary-color);
        background: var(--bg-secondary);
      }
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
  }
}

.content {
  flex: 1;
  // padding: 20px;
  overflow-x: visible;
  overflow-y: auto;
  background: var(--bg-secondary);
  
  .content-wrapper {
    width: 100%;
    min-height: 100%;
    background: var(--bg-primary);
    border-radius: var(--radius-lg);
    padding: 24px;
    box-shadow: var(--shadow-sm);
    border: 1px solid var(--border-color);
  }
}

.footer {
  height: 50px;
  background: var(--bg-primary);
  border-top: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  padding: 0 20px;
  flex-shrink: 0;
  
  .footer-content {
    width: 100%;
    text-align: center;
    color: var(--text-secondary);
    font-size: 0.875rem;
  }
}

// 动画
.logo-text-enter-active,
.logo-text-leave-active {
  transition: all 0.3s ease;
}

.logo-text-enter-from,
.logo-text-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s ease;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

// 响应式设计
@media (max-width: 1024px) {
  .sidebar {
    width: 250px;
    
    &.is-collapse {
      width: 250px;
      transform: translateX(-100%);
    }
  }
  
  .main-content {
    margin-left: 0;
  }
}

@media (max-width: 768px) {
  .header {
    padding: 0 16px;
  }
  
  .content {
    padding: 16px;
    
    .content-wrapper {
      padding: 16px;
    }
  }
  
  .footer {
    padding: 0 16px;
  }
}
</style> 