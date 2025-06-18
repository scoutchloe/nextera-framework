<template>
  <!-- 单级菜单项 -->
  <el-menu-item 
    v-if="!menu.children || menu.children.length === 0"
    :index="menu.path || String(menu.id)"
    @click="handleMenuClick(menu.path)"
  >
    <el-icon v-if="menu.icon">
      <component :is="menu.icon" />
    </el-icon>
    <template #title>
      <span>{{ menu.title }}</span>
    </template>
  </el-menu-item>

  <!-- 多级子菜单 -->
  <el-sub-menu 
    v-else
    :index="String(menu.id)"
    popper-class="sidebar-submenu-popper"
  >
    <template #title>
      <el-icon v-if="menu.icon">
        <component :is="menu.icon" />
      </el-icon>
      <span>{{ menu.title }}</span>
    </template>
    
    <sidebar-menu-item
      v-for="child in menu.children"
      :key="child.id"
      :menu="child"
    />
  </el-sub-menu>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import type { MenuItem } from '@/types'

interface Props {
  menu: MenuItem
}

defineProps<Props>()

const router = useRouter()
const appStore = useAppStore()

// 处理菜单点击
const handleMenuClick = (path?: string) => {
  if (path) {
    router.push(path)
  }
  
  // 移动端点击菜单后自动收起侧边栏
  if (appStore.isMobile) {
    appStore.setSidebarStatus('closed')
  }
}
</script>

<style lang="scss" scoped>
// 子菜单样式
:deep(.el-sub-menu__title) {
  height: 50px;
  line-height: 50px;
  padding-left: 20px !important;
  
  &:hover {
    background-color: var(--hover-bg-color, rgba(0, 0, 0, 0.06));
  }
}

:deep(.el-menu-item) {
  height: 45px;
  line-height: 45px;
  padding-left: 40px !important;
  
  &:hover {
    background-color: var(--hover-bg-color, rgba(0, 0, 0, 0.06));
  }
  
  &.is-active {
    background-color: var(--primary-color, #409eff);
    color: white;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      width: 3px;
      height: 100%;
      background-color: var(--primary-color, #409eff);
    }
  }
}
</style> 