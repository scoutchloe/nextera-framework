<template>
  <div class="header-actions">
    <!-- 搜索 -->
    <div class="action-item search">
      <el-input
        v-model="searchText"
        placeholder="搜索菜单..."
        prefix-icon="Search"
        clearable
        style="width: 200px"
        @input="handleSearch"
      />
    </div>
    
    <!-- 全屏切换 -->
    <div class="action-item">
      <el-tooltip :content="isFullscreen ? '退出全屏' : '全屏'" placement="bottom">
        <el-button
          circle
          :icon="isFullscreen ? 'Aim' : 'FullScreen'"
          @click="toggleFullscreen"
        />
      </el-tooltip>
    </div>
    
    <!-- 主题切换 -->
    <div class="action-item">
      <el-tooltip content="切换主题" placement="bottom">
        <el-button
          circle
          :icon="appStore.isDark ? 'Sunny' : 'Moon'"
          @click="appStore.toggleTheme"
        />
      </el-tooltip>
    </div>
    
    <!-- 消息通知 -->
    <div class="action-item">
      <el-badge :value="messageCount" :max="99" class="message-badge">
        <el-button circle icon="Bell" @click="showMessageDrawer = true" />
      </el-badge>
    </div>
    
    <!-- 用户信息 -->
    <div class="action-item user-info">
      <el-dropdown @command="handleUserCommand" trigger="click">
        <div class="user-dropdown">
          <el-avatar
            :src="userStore.userAvatar"
            :size="32"
            class="user-avatar"
          >
            <el-icon><UserFilled /></el-icon>
          </el-avatar>
          <span class="username">{{ userStore.userName }}</span>
          <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
        </div>
        
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
              个人中心
            </el-dropdown-item>
            <el-dropdown-item command="settings">
              <el-icon><Setting /></el-icon>
              系统设置
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    
    <!-- 消息抽屉 -->
    <el-drawer
      v-model="showMessageDrawer"
      title="消息通知"
      direction="rtl"
      size="400px"
    >
      <div class="message-list">
        <div v-if="messageList.length === 0" class="empty-message">
          <el-empty description="暂无消息" />
        </div>
        <div v-else>
          <div
            v-for="message in messageList"
            :key="message.id"
            class="message-item"
            :class="{ 'is-read': message.isRead }"
          >
            <div class="message-avatar">
              <el-avatar :size="40">
                <el-icon><Bell /></el-icon>
              </el-avatar>
            </div>
            <div class="message-content">
              <h4>{{ message.title }}</h4>
              <p>{{ message.content }}</p>
              <span class="message-time">{{ message.time }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'

interface Message {
  id: number
  title: string
  content: string
  time: string
  isRead: boolean
}

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

// 响应式数据
const searchText = ref('')
const isFullscreen = ref(false)
const showMessageDrawer = ref(false)

// 模拟消息数据
const messageList = ref<Message[]>([
  {
    id: 1,
    title: '系统通知',
    content: '您有新的权限配置需要审核',
    time: '2024-06-18 10:30',
    isRead: false
  },
  {
    id: 2,
    title: '用户反馈',
    content: '用户张三提交了新的反馈意见',
    time: '2024-06-18 09:15',
    isRead: true
  }
])

// 计算未读消息数量
const messageCount = computed(() => {
  return messageList.value.filter(msg => !msg.isRead).length
})

// 搜索处理
const handleSearch = (value: string) => {
  console.log('搜索:', value)
  // 这里可以实现搜索逻辑
}

// 全屏切换
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    document.exitFullscreen()
    isFullscreen.value = false
  }
}

// 监听全屏状态变化
const handleFullscreenChange = () => {
  isFullscreen.value = !!document.fullscreenElement
}

// 用户下拉菜单命令处理
const handleUserCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    userStore.logout()
    ElMessage.success('退出登录成功')
    router.push('/login')
  } catch (error) {
    // 用户取消
  }
}

onMounted(() => {
  document.addEventListener('fullscreenchange', handleFullscreenChange)
})

onUnmounted(() => {
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
})
</script>

<style lang="scss" scoped>
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  
  .action-item {
    display: flex;
    align-items: center;
    
    &.search {
      margin-right: 8px;
      
      :deep(.el-input) {
        .el-input__wrapper {
          background: var(--bg-secondary);
          border: 1px solid var(--border-color);
          border-radius: var(--radius-md);
          transition: all 0.3s ease;
          
          &:hover,
          &.is-focus {
            border-color: var(--primary-color);
            box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
          }
        }
      }
    }
    
    .el-button {
      &.is-circle {
        width: 36px;
        height: 36px;
        border: 1px solid var(--border-color);
        background: var(--bg-primary);
        color: var(--text-secondary);
        transition: all 0.3s ease;
        
        &:hover {
          color: var(--primary-color);
          border-color: var(--primary-color);
          background: var(--bg-secondary);
        }
      }
    }
  }
  
  .message-badge {
    :deep(.el-badge__content) {
      background: var(--error-color);
      border: 2px solid var(--bg-primary);
    }
  }
  
  .user-info {
    .user-dropdown {
      display: flex;
      align-items: center;
      padding: 6px 12px;
      border-radius: var(--radius-md);
      cursor: pointer;
      transition: all 0.3s ease;
      
      &:hover {
        background: var(--bg-secondary);
      }
      
      .user-avatar {
        margin-right: 8px;
        border: 2px solid var(--border-color);
        transition: all 0.3s ease;
        
        &:hover {
          border-color: var(--primary-color);
        }
      }
      
      .username {
        font-size: 14px;
        font-weight: 500;
        color: var(--text-primary);
        margin-right: 4px;
      }
      
      .dropdown-arrow {
        font-size: 12px;
        color: var(--text-tertiary);
        transition: transform 0.3s ease;
      }
      
      &:hover .dropdown-arrow {
        transform: rotate(180deg);
      }
    }
  }
}

// 消息列表样式
.message-list {
  .empty-message {
    padding: 40px 20px;
    text-align: center;
  }
  
  .message-item {
    display: flex;
    padding: 16px;
    border-bottom: 1px solid var(--border-color);
    transition: all 0.3s ease;
    cursor: pointer;
    
    &:hover {
      background: var(--bg-secondary);
    }
    
    &.is-read {
      opacity: 0.6;
    }
    
    .message-avatar {
      margin-right: 12px;
      flex-shrink: 0;
    }
    
    .message-content {
      flex: 1;
      min-width: 0;
      
      h4 {
        margin: 0 0 8px 0;
        font-size: 14px;
        font-weight: 500;
        color: var(--text-primary);
        
        .message-item.is-read & {
          color: var(--text-secondary);
        }
      }
      
      p {
        margin: 0 0 8px 0;
        font-size: 13px;
        color: var(--text-secondary);
        line-height: 1.4;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }
      
      .message-time {
        font-size: 12px;
        color: var(--text-tertiary);
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .header-actions {
    gap: 8px;
    
    .action-item.search {
      display: none;
    }
    
    .user-info .user-dropdown .username {
      display: none;
    }
  }
}
</style> 