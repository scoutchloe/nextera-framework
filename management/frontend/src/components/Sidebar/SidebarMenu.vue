<template>
  <div class="sidebar-menu">
    <el-menu
      :default-active="activeMenu"
      :default-openeds="['2', '3', '4']"
      :collapse="appStore.isCollapsed"
      :unique-opened="false"
      mode="vertical"
      class="menu"
      background-color="transparent"
      text-color="var(--text-primary)"
      active-text-color="var(--primary-color)"
    >
      <sidebar-menu-item
        v-for="menu in menuList"
        :key="menu.id"
        :menu="menu"
      />
    </el-menu>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import SidebarMenuItem from './SidebarMenuItem.vue'
import type { MenuItem } from '@/types'

const route = useRoute()
const appStore = useAppStore()
const userStore = useUserStore()

// 计算当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 计算默认展开的菜单
const defaultOpeneds = computed(() => {
  const currentPath = route.path
  const openeds: string[] = []
  
  // 根据当前路径确定需要展开的父菜单
  if (currentPath.startsWith('/system')) {
    openeds.push('/system')
  } else if (currentPath.startsWith('/user')) {
    openeds.push('/user')
  } else if (currentPath.startsWith('/article')) {
    openeds.push('/article')
  }
  
  return openeds
})

// 模拟菜单数据
const menuList = computed<MenuItem[]>(() => [
  {
    id: 1,
    title: '仪表盘',
    path: '/dashboard',
    icon: 'DataBoard',
    meta: {
      title: '仪表盘',
      icon: 'DataBoard',
      requireAuth: true
    }
  },
  {
    id: 2,
    title: '系统管理',
    path: '/system',
    icon: 'Setting',
    children: [
      {
        id: 21,
        title: '管理员管理',
        path: '/system/admin',
        icon: 'UserFilled',
        meta: {
          title: '管理员管理',
          icon: 'UserFilled',
          requireAuth: true,
          permissions: ['system:admin:list']
        }
      },
      {
        id: 22,
        title: '角色管理',
        path: '/system/role',
        icon: 'Avatar',
        meta: {
          title: '角色管理',
          icon: 'Avatar',
          requireAuth: true,
          permissions: ['system:role:list']
        }
      },
      {
        id: 23,
        title: '权限管理',
        path: '/system/permission',
        icon: 'Lock',
        meta: {
          title: '权限管理',
          icon: 'Lock',
          requireAuth: true,
          permissions: ['system:permission:list']
        }
      },
      {
        id: 24,
        title: '操作日志',
        path: '/system/log',
        icon: 'Document',
        meta: {
          title: '操作日志',
          icon: 'Document',
          requireAuth: true,
          permissions: ['system:log:list']
        }
      }
    ],
    meta: {
      title: '系统管理',
      icon: 'Setting',
      requireAuth: true
    }
  },
  {
    id: 3,
    title: '用户管理',
    path: '/user',
    icon: 'User',
    children: [
      {
        id: 31,
        title: '用户列表',
        path: '/user/list',
        icon: 'User',
        meta: {
          title: '用户列表',
          icon: 'User',
          requireAuth: true,
          permissions: ['user:list']
        }
      },
      {
        id: 32,
        title: '用户分析',
        path: '/user/analysis',
        icon: 'TrendCharts',
        meta: {
          title: '用户分析',
          icon: 'TrendCharts',
          requireAuth: true,
          permissions: ['user:analysis']
        }
      }
    ],
    meta: {
      title: '用户管理',
      icon: 'User',
      requireAuth: true
    }
  },
  {
    id: 4,
    title: '文章管理',
    path: '/article',
    icon: 'EditPen',
    children: [
      {
        id: 41,
        title: '文章列表',
        path: '/article/list',
        icon: 'Document',
        meta: {
          title: '文章列表',
          icon: 'Document',
          requireAuth: true,
          permissions: ['article:list']
        }
      },
      {
        id: 42,
        title: '分类管理',
        path: '/article/category',
        icon: 'Collection',
        meta: {
          title: '分类管理',
          icon: 'Collection',
          requireAuth: true,
          permissions: ['article:category:list']
        }
      },
      {
        id: 43,
        title: '标签管理',
        path: '/article/tag',
        icon: 'PriceTag',
        meta: {
          title: '标签管理',
          icon: 'PriceTag',
          requireAuth: true,
          permissions: ['article:tag:list']
        }
      }
    ],
    meta: {
      title: '文章管理',
      icon: 'EditPen',
      requireAuth: true
    }
  }
])

onMounted(() => {
  // 这里可以从后端获取用户权限菜单
  // userStore.setMenuList(menuList.value)
})
</script>

<style lang="scss" scoped>
.sidebar-menu {
  height: 100%;
  
  .menu {
    border: none;
    height: 100%;
    
    :deep(.el-menu-item) {
      height: 48px;
      line-height: 48px;
      margin: 4px 12px;
      border-radius: var(--radius-md);
      transition: all 0.3s ease;
      position: relative;
      overflow: hidden;
      
      &:hover {
        background: var(--bg-secondary);
        color: var(--primary-color);
        transform: translateX(4px);
      }
      
      &.is-active {
        background: var(--gradient-tech);
        color: white;
        
        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 50%;
          width: 4px;
          height: 20px;
          background: var(--primary-light);
          border-radius: 2px;
          transform: translateY(-50%);
        }
      }
      
      .el-icon {
        margin-right: 8px;
        font-size: 18px;
      }
    }
    
    :deep(.el-sub-menu) {
      .el-sub-menu__title {
        height: 48px;
        line-height: 48px;
        margin: 4px 12px;
        border-radius: var(--radius-md);
        transition: all 0.3s ease;
        
        &:hover {
          background: var(--bg-secondary);
          color: var(--primary-color);
          transform: translateX(4px);
        }
        
        .el-icon {
          margin-right: 8px;
          font-size: 18px;
        }
        
        .el-sub-menu__icon-arrow {
          margin-top: -3px;
        }
      }
      
      .el-menu {
        background: var(--bg-secondary);
        
        .el-menu-item {
          margin: 2px 8px;
          margin-left: 40px;
          
          &::before {
            content: '';
            position: absolute;
            left: -20px;
            top: 50%;
            width: 8px;
            height: 1px;
            background: var(--border-color);
            transform: translateY(-50%);
          }
        }
      }
    }
    
    // 折叠状态样式
    &.el-menu--collapse {
      :deep(.el-menu-item),
      :deep(.el-sub-menu .el-sub-menu__title) {
        padding: 0 20px;
        margin: 4px 8px;
        
        .el-icon {
          margin-right: 0;
        }
      }
    }
  }
}
</style> 