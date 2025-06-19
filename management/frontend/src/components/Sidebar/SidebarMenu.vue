<template>
  <div class="sidebar-menu">
    <el-menu
      :default-active="activeMenu"
      :default-openeds="defaultOpeneds"
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
  } else if (currentPath.startsWith('/order')) {
    openeds.push('/order')
  }
  
  return openeds
})

// 使用用户store中动态生成的菜单列表
const menuList = computed(() => {
  console.log('SidebarMenu: 当前菜单列表:', userStore.menuList)
  return userStore.menuList
})

onMounted(() => {
  // 调试：打印菜单相关信息
  console.log('=== 侧边栏菜单调试信息 ===')
  console.log('用户信息:', userStore.userInfo)
  console.log('用户权限:', userStore.permissions)
  console.log('用户角色:', userStore.roles)
  console.log('是否管理员:', userStore.isAdmin)
  console.log('权限树数据:', userStore.permissionTree)
  console.log('动态生成的菜单:', userStore.menuList)
  console.log('当前路由:', route.path)
  
  // 如果没有菜单数据，尝试重新加载
  if (userStore.menuList.length === 0 && userStore.isLoggedIn) {
    console.log('菜单为空，尝试重新加载权限和菜单')
    userStore.reloadPermissionsAndMenu().catch(error => {
      console.error('重新加载菜单失败:', error)
    })
  }
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
          transform: translateY(-50%);
          border-radius: 0 2px 2px 0;
        }
      }
    }
    
    :deep(.el-sub-menu) {
      margin: 4px 12px;
      border-radius: var(--radius-md);
      overflow: hidden;
      
      .el-sub-menu__title {
        height: 48px;
        line-height: 48px;
        transition: all 0.3s ease;
        
        &:hover {
          background: var(--bg-secondary);
          color: var(--primary-color);
          transform: translateX(4px);
        }
      }
      
      &.is-active {
        .el-sub-menu__title {
          background: var(--gradient-tech);
          color: white;
        }
      }
    }
    
    :deep(.el-menu-item-group) {
      .el-menu-item-group__title {
        padding: 12px 20px;
        font-size: 12px;
        color: var(--text-secondary);
        font-weight: 500;
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }
    }
  }
}

// 折叠状态样式
.menu.el-menu--collapse {
  :deep(.el-menu-item),
  :deep(.el-sub-menu) {
    margin: 4px 8px;
  }
}

// 暗色主题适配
@media (prefers-color-scheme: dark) {
  .sidebar-menu {
    .menu {
      :deep(.el-menu-item) {
        &:hover {
          background: rgba(255, 255, 255, 0.1);
        }
        
        &.is-active {
          background: var(--primary-color);
        }
      }
      
      :deep(.el-sub-menu) {
        .el-sub-menu__title {
          &:hover {
            background: rgba(255, 255, 255, 0.1);
          }
        }
        
        &.is-active {
          .el-sub-menu__title {
            background: var(--primary-color);
          }
        }
      }
    }
  }
}
</style> 