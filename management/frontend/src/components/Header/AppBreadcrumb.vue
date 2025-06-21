<template>
  <div class="app-breadcrumb">
    <el-breadcrumb separator="/">
      <transition-group name="breadcrumb">
        <el-breadcrumb-item
          v-for="(item, index) in breadcrumbList"
          :key="item.path || item.title"
          :to="item.path && index < breadcrumbList.length - 1 ? { path: item.path } : undefined"
          :class="{ 'is-active': index === breadcrumbList.length - 1 }"
        >
          <el-icon v-if="item.icon && index === 0">
            <component :is="item.icon" />
          </el-icon>
          <span>{{ item.title }}</span>
        </el-breadcrumb-item>
      </transition-group>
    </el-breadcrumb>
  </div>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useI18n } from '@/composables/useI18n'
import type { BreadcrumbItem } from '@/types'

const route = useRoute()
const appStore = useAppStore()
const { t } = useI18n()

// 路由标题翻译映射
const routeTitleMap: Record<string, string> = {
  '首页': 'common.home',
  '仪表盘': 'menu.dashboard',
  '系统管理': 'menu.system',
  '管理员管理': 'menu.admin',
  '角色管理': 'menu.role',
  '权限管理': 'menu.permission',
  '操作日志': 'menu.log',
  '用户管理': 'menu.user',
  '用户列表': 'menu.userList',
  '用户分析': 'menu.analysis',
  '文章管理': 'menu.article',
  '文章列表': 'menu.articleList',
  '文章分类': 'menu.articleCategory',
  '文章标签': 'menu.articleTag',
  '订单管理': 'menu.order',
  '订单列表': 'menu.orderList'
}

// 计算面包屑列表
const breadcrumbList = computed<BreadcrumbItem[]>(() => {
  // 获取路由匹配的所有路径
  const matched = route.matched.filter(item => item.meta && item.meta.title)
  
  // 添加首页
  const breadcrumbs: BreadcrumbItem[] = [
    {
      title: t('common.home'),
      path: '/dashboard',
      icon: 'HomeFilled'
    }
  ]
  
  // 添加匹配的路由
  matched.forEach((match, index) => {
    if (match.meta?.title && match.path !== '/dashboard') {
      const originalTitle = match.meta.title as string
      const translationKey = routeTitleMap[originalTitle]
      const title = translationKey ? t(translationKey) : originalTitle
      
      breadcrumbs.push({
        title,
        path: index === matched.length - 1 ? undefined : match.path,
        icon: match.meta.icon as string
      })
    }
  })
  
  return breadcrumbs
})

// 监听路由变化，更新面包屑
watch(
  () => route.path,
  () => {
    // 可以在这里进行额外的面包屑处理
  },
  { immediate: true }
)
</script>

<style lang="scss" scoped>
.app-breadcrumb {
  display: flex;
  align-items: center;
  
  :deep(.el-breadcrumb) {
    font-size: 14px;
    line-height: 1;
    
    .el-breadcrumb__item {
      .el-breadcrumb__inner {
        display: inline-flex;
        align-items: center;
        color: var(--text-secondary);
        font-weight: 400;
        transition: color 0.3s ease;
        
        .el-icon {
          margin-right: 4px;
          font-size: 16px;
        }
        
        &:hover {
          color: var(--primary-color);
        }
        
        &.is-link {
          color: var(--text-primary);
          cursor: pointer;
          
          &:hover {
            color: var(--primary-color);
          }
        }
      }
      
      &:last-child .el-breadcrumb__inner {
        color: var(--text-primary);
        font-weight: 500;
        cursor: default;
        
        &:hover {
          color: var(--text-primary);
        }
      }
      
      .el-breadcrumb__separator {
        color: var(--text-tertiary);
        margin: 0 8px;
      }
    }
  }
}

// 面包屑动画
.breadcrumb-enter-active,
.breadcrumb-leave-active {
  transition: all 0.3s ease;
}

.breadcrumb-enter-from,
.breadcrumb-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

.breadcrumb-move {
  transition: transform 0.3s ease;
}
</style> 