<template>
  <div class="main-layout" :class="{ 
    'is-collapsed': appStore.isCollapsed, 
    'is-mobile': appStore.isMobile,
    'is-tablet': appStore.isTablet,
    'is-desktop': appStore.isDesktop
  }">
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
            <span v-show="!appStore.isCollapsed || appStore.isMobile" class="logo-text gradient-text">Nextera</span>
          </transition>
        </div>
        <!-- 移动端关闭按钮 -->
        <el-button v-if="appStore.isMobile" 
          class="mobile-close" 
          :icon="'Close'"
          @click="appStore.toggleSidebar"
          text
        />
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
          <app-breadcrumb v-if="!appStore.isMobile" />
        </div>
        
        <div class="header-right">
          <header-actions />
        </div>
      </header>
      
      <!-- 移动端面包屑 -->
      <div v-if="appStore.isMobile" class="mobile-breadcrumb">
        <app-breadcrumb />
      </div>
      
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
  min-height: 100vh;
  background-color: var(--bg-secondary);
  overflow: hidden;
  position: relative;
}

.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1001;
  width: 240px;
  height: 100vh;
  background: var(--bg-primary);
  border-right: 1px solid var(--border-color);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  transform: translateX(0);

  &.is-collapse {
    width: 56px;
  }

  .sidebar-header {
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 16px;
    border-bottom: 1px solid var(--border-color);
    
    .logo {
      display: flex;
      align-items: center;
      cursor: pointer;
      flex: 1;
      
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
        flex-shrink: 0;
        
        .is-collapse & {
          margin-right: 0;
        }
      }
      
      .logo-text {
        font-size: 1.5rem;
        font-weight: 700;
        white-space: nowrap;
        overflow: hidden;
      }
    }

    .mobile-close {
      color: var(--text-secondary);
      
      &:hover {
        color: var(--primary-color);
      }
    }
  }
  
  .sidebar-content {
    height: calc(100vh - 64px);
    overflow-y: auto;
    overflow-x: hidden;
    
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
  transition: all 0.3s ease;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  margin-left: 180px;
  transition: margin-left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  width: calc(100% - 240px);
  max-width: calc(100% - 240px);
  
  .is-collapsed & {
    margin-left: 56px;
    width: calc(100% - 56px);
    max-width: calc(100% - 56px);
  }
  
  .is-mobile & {
    margin-left: 0;
    width: 100%;
    max-width: 100%;
  }
}

.header {
  height: 64px;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  flex-shrink: 0;
  z-index: 999;
  position: relative;
  
  .header-left {
    display: flex;
    align-items: center;
    flex: 1;
    min-width: 0;
    
    .sidebar-toggle {
      margin-right: 16px;
      color: var(--text-secondary);
      flex-shrink: 0;
      
      &:hover {
        color: var(--primary-color);
        background: var(--bg-secondary);
      }
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    flex-shrink: 0;
  }
}

.mobile-breadcrumb {
  padding: 12px 16px;
  background: var(--bg-primary);
  border-bottom: 1px solid var(--border-color);
}

.content {
  flex: 1;
  padding: 16px 0;
  overflow-x: hidden;
  overflow-y: auto;
  background: var(--bg-secondary);
  
  .content-wrapper {
    width: 100%;
    min-height: calc(100vh - 64px - 50px - 32px);
    background: var(--bg-primary);
    border-radius: var(--radius-lg);
    padding: 20px;
    margin: 0 16px;
    box-shadow: var(--shadow-sm);
    border: 1px solid var(--border-color);
    overflow: hidden;
  }
}

.footer {
  height: 50px;
  background: var(--bg-primary);
  border-top: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  padding: 0 16px;
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
// 超大屏幕 (1920px+)
@media (min-width: 1920px) {
  .main-content {
    .content {
      .content-wrapper {
        max-width: none;
        margin: 0 -10px;
        padding: 24px;
      }
    }
  }
}

// 大屏幕 (1280px - 1919px)
@media (min-width: 1280px) and (max-width: 1919px) {
  .main-content {
    .content {
      .content-wrapper {
        max-width: none;
        margin: 0 20px;
        padding: 22px;
      }
    }
  }
}

// 中等屏幕 (1024px - 1279px)
@media (min-width: 1024px) and (max-width: 1279px) {
  .sidebar {
    width: 180px;
  }
  
  .main-content {
    margin-left: 180px;
    width: calc(100% - 180px);
    max-width: calc(100% - 180px);
    
    .is-collapsed & {
      margin-left: 56px;
      width: calc(100% - 56px);
      max-width: calc(100% - 56px);
    }
  }
  
  .header {
    padding: 0 16px;
  }
  
  .content {
    padding: 14px 0;
    
    .content-wrapper {
      margin: 0 14px;
      padding: 18px;
    }
  }
  
  .footer {
    padding: 0 16px;
  }
}

// 平板设备 (768px - 1023px)
@media (min-width: 768px) and (max-width: 1023px) {
  .sidebar {
    width: 240px;
    transform: translateX(-100%);
    
    &:not(.is-collapse) {
      transform: translateX(0);
    }
  }
  
  .main-content {
    margin-left: 0;
    width: 100%;
    max-width: 100%;
  }
  
  .header {
    padding: 0 16px;
  }
  
  .content {
    padding: 12px 0;
    
    .content-wrapper {
      min-height: calc(100vh - 64px - 50px - 24px);
      padding: 16px;
      margin: 0 12px;
    }
  }
  
  .footer {
    padding: 0 16px;
  }
}

// 移动设备 (320px - 767px)
@media (max-width: 767px) {
  .sidebar {
    width: 260px;
    transform: translateX(-100%);
    
    &:not(.is-collapse) {
      transform: translateX(0);
    }
    
    .sidebar-header {
      padding: 0 16px;
      height: 56px;
    }
  }
  
  .main-content {
    margin-left: 0;
    width: 100%;
    max-width: 100%;
  }
  
  .header {
    padding: 0 12px;
    height: 56px;
  }
  
  .mobile-breadcrumb {
    padding: 8px 12px;
  }
  
  .content {
    padding: 8px 0;
    
    .content-wrapper {
      min-height: calc(100vh - 56px - 40px - 16px);
      padding: 12px;
      margin: 0 8px;
      border-radius: var(--radius-md);
    }
  }
  
  .footer {
    padding: 0 12px;
    height: 40px;
    
    .footer-content {
      font-size: 0.75rem;
    }
  }
}

// 小屏幕移动设备 (320px - 479px)
@media (max-width: 479px) {
  .sidebar {
    width: 100vw;
    
    .sidebar-header {
      .logo-text {
        font-size: 1.25rem;
      }
    }
  }
  
  .header {
    padding: 0 8px;
  }
  
  .mobile-breadcrumb {
    padding: 6px 8px;
  }
  
  .content {
    padding: 6px 0;
    
    .content-wrapper {
      padding: 10px;
      margin: 0 6px;
      min-height: calc(100vh - 56px - 40px - 12px);
    }
  }
  
  .footer {
    padding: 0 8px;
  }
}

// 横屏适配
@media (orientation: landscape) and (max-height: 600px) {
  .footer {
    display: none;
  }
  
  .content {
    .content-wrapper {
      min-height: calc(100vh - 64px - 48px);
    }
  }
}

// 触摸设备优化
@media (hover: none) and (pointer: coarse) {
  .sidebar {
    .sidebar-header .logo:hover {
      background: none;
    }
  }
  
  .header {
    .sidebar-toggle:hover {
      background: none;
    }
  }
}
</style> 