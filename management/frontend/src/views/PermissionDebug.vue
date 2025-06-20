<template>
  <div class="permission-debug">
    <el-card header="权限调试信息">
      <div class="debug-section">
        <h3>用户信息</h3>
        <pre>{{ JSON.stringify(userStore.userInfo, null, 2) }}</pre>
      </div>
      
      <div class="debug-section">
        <h3>权限列表 ({{ userStore.permissions.length }})</h3>
        <el-tag v-for="permission in userStore.permissions" :key="permission" style="margin: 2px;">
          {{ permission }}
        </el-tag>
        <div v-if="userStore.permissions.length === 0" style="color: #999;">
          无权限数据
        </div>
      </div>
      
      <div class="debug-section">
        <h3>权限树结构</h3>
        <pre>{{ JSON.stringify(userStore.permissionTree, null, 2) }}</pre>
      </div>
      
      <div class="debug-section">
        <h3>菜单列表 ({{ userStore.menuList.length }})</h3>
        <pre>{{ JSON.stringify(userStore.menuList, null, 2) }}</pre>
      </div>
      
      <div class="debug-section">
        <h3>localStorage数据</h3>
        <div>
          <strong>token:</strong> {{ localStorageData.token }}
        </div>
        <div>
          <strong>permissions:</strong> {{ localStorageData.permissions }}
        </div>
        <div>
          <strong>menuList:</strong> {{ localStorageData.menuList }}
        </div>
      </div>
      
      <div class="debug-section">
        <h3>权限检查测试</h3>
        <div>
          <el-tag :type="userStore.hasPermission('dashboard:view') ? 'success' : 'danger'">
            dashboard:view: {{ userStore.hasPermission('dashboard:view') ? '有权限' : '无权限' }}
          </el-tag>
        </div>
        <div>
          <el-tag :type="userStore.hasPermission('article:view') ? 'success' : 'danger'">
            article:view: {{ userStore.hasPermission('article:view') ? '有权限' : '无权限' }}
          </el-tag>
        </div>
        <div>
          <el-tag :type="userStore.hasPermission('article:list') ? 'success' : 'danger'">
            article:list: {{ userStore.hasPermission('article:list') ? '有权限' : '无权限' }}
          </el-tag>
        </div>
      </div>
      
      <div class="debug-section">
        <h3>操作</h3>
        <el-button @click="reloadPermissions" type="primary">重新加载权限</el-button>
        <el-button @click="clearCache" type="warning">清除缓存</el-button>
        <el-button @click="refreshData" type="success">刷新数据</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

// 计算属性用于安全访问localStorage
const localStorageData = computed(() => {
  if (typeof window === 'undefined') {
    return {
      token: '不存在',
      permissions: '无',
      menuList: '无'
    }
  }
  
  return {
    token: window.localStorage.getItem('token') ? '已存在' : '不存在',
    permissions: window.localStorage.getItem('permissions') || '无',
    menuList: window.localStorage.getItem('menuList') || '无'
  }
})

const reloadPermissions = async () => {
  try {
    await userStore.reloadPermissionsAndMenu()
    ElMessage.success('权限重新加载成功')
  } catch (error: any) {
    ElMessage.error('权限重新加载失败: ' + (error?.message || '未知错误'))
  }
}

const clearCache = () => {
  if (typeof window !== 'undefined') {
    window.localStorage.removeItem('permissions')
    window.localStorage.removeItem('menuList')
    window.localStorage.removeItem('permissionTree')
  }
  userStore.permissions = []
  userStore.menuList = []
  userStore.permissionTree = []
  ElMessage.success('缓存已清除')
}

const refreshData = () => {
  if (typeof window !== 'undefined') {
    window.location.reload()
  }
}
</script>

<style scoped>
.permission-debug {
  padding: 20px;
}

.debug-section {
  margin-bottom: 20px;
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
}

.debug-section h3 {
  margin-top: 0;
  color: #409eff;
}

pre {
  background: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
  max-height: 300px;
  overflow-y: auto;
}
</style> 