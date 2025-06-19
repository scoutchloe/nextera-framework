<template>
  <div class="quick-test">
    <h2>权限编码查看和修复工具</h2>
    
    <div class="button-group">
      <el-button @click="fetchAllPermissions" type="success">获取所有权限编码</el-button>
      <el-button @click="checkCurrentUserPermissions" type="info">获取当前用户权限</el-button>
      <el-button @click="checkContentAdmin" type="primary">检查ContentAdmin权限</el-button>
      <el-button @click="fixPermissions" type="warning">修复权限</el-button>
    </div>
    
    <!-- 所有权限编码 -->
    <div v-if="allPermissions" class="result-section">
      <h3>系统中所有权限编码</h3>
      <el-card>
        <div class="permissions-grid">
          <div v-for="permission in flatPermissions" :key="permission.permissionCode" class="permission-item">
            <span class="permission-code">{{ permission.permissionCode }}</span>
            <span class="permission-name">{{ permission.permissionName }}</span>
            <span class="permission-type" :class="permission.permissionType">{{ permission.permissionType }}</span>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 当前用户权限 -->
    <div v-if="currentUserPermissions" class="result-section">
      <h3>当前用户权限</h3>
      <el-card>
        <div class="user-permissions">
          <div v-for="permission in currentUserPermissions" :key="permission.permissionCode">
            {{ permission.permissionCode }} - {{ permission.permissionName }}
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- ContentAdmin权限检查 -->
    <div v-if="contentAdminResult" class="result-section">
      <h3>ContentAdmin权限状态</h3>
      
      <el-card title="用户信息">
        <p>用户ID: {{ contentAdminResult.userId }}</p>
        <p>用户名: {{ contentAdminResult.username }}</p>
        <p>角色: {{ contentAdminResult.roles?.join(', ') || '无' }}</p>
      </el-card>
      
      <el-card title="关键权限检查" style="margin-top: 10px;">
        <div v-for="(hasPermission, permission) in contentAdminResult.keyPermissions" :key="permission">
          <span :style="{ color: hasPermission ? 'green' : 'red' }">
            {{ hasPermission ? '✓' : '✗' }}
          </span>
          {{ permission }}
        </div>
      </el-card>
      
      <el-card title="建议的前端菜单权限配置" style="margin-top: 10px;">
        <pre>{{ suggestedMenuConfig }}</pre>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const allPermissions = ref<any>(null)
const currentUserPermissions = ref<any>(null)
const contentAdminResult = ref<any>(null)

// 扁平化权限列表
const flatPermissions = computed(() => {
  if (!allPermissions.value) return []
  
  const flatten = (permissions: any[]): any[] => {
    let result: any[] = []
    permissions.forEach(permission => {
      result.push(permission)
      if (permission.children && permission.children.length > 0) {
        result = result.concat(flatten(permission.children))
      }
    })
    return result
  }
  
  return flatten(allPermissions.value)
})

// 建议的菜单配置
const suggestedMenuConfig = computed(() => {
  if (!contentAdminResult.value?.permissions) return ''
  
  const permissions = contentAdminResult.value.permissions
  
  // 根据实际权限生成菜单配置
  const config = {
    dashboard: permissions.includes('dashboard:view') ? ['dashboard:view'] : [],
    system: permissions.filter(p => p.startsWith('system:')),
    user: permissions.filter(p => p.startsWith('user:')),
    article: permissions.filter(p => p.startsWith('article:')),
    content: permissions.filter(p => p.startsWith('content:')),
    order: permissions.filter(p => p.startsWith('order:'))
  }
  
  return JSON.stringify(config, null, 2)
})

const fetchAllPermissions = async () => {
  try {
    const response = await axios.get('/api/system/permission/tree')
    if (response.data?.code === 200) {
      allPermissions.value = response.data.data
      ElMessage.success('获取所有权限成功')
    } else {
      ElMessage.error(`获取失败: ${response.data?.message}`)
    }
  } catch (error: any) {
    console.error('获取权限失败:', error)
    ElMessage.error(`获取失败: ${error.response?.data?.message || error.message}`)
  }
}

const checkCurrentUserPermissions = async () => {
  try {
    const response = await axios.get('/api/auth/permissions')
    if (response.data?.code === 200) {
      currentUserPermissions.value = response.data.data
      ElMessage.success('获取当前用户权限成功')
    } else {
      ElMessage.error(`获取失败: ${response.data?.message}`)
    }
  } catch (error: any) {
    console.error('获取当前用户权限失败:', error)
    ElMessage.error(`获取失败: ${error.response?.data?.message || error.message}`)
  }
}

const checkContentAdmin = async () => {
  try {
    const response = await axios.get('/api/debug/check-content-admin')
    if (response.data?.code === 200) {
      contentAdminResult.value = response.data.data
      ElMessage.success('检查ContentAdmin权限完成')
    } else {
      ElMessage.error(`检查失败: ${response.data?.message}`)
    }
  } catch (error: any) {
    console.error('检查ContentAdmin权限失败:', error)
    ElMessage.error(`检查失败: ${error.response?.data?.message || error.message}`)
  }
}

const fixPermissions = async () => {
  try {
    const response = await axios.post('/api/debug/fix-content-admin')
    if (response.data?.code === 200) {
      ElMessage.success('权限修复成功，请重新检查')
      // 自动重新检查权限
      setTimeout(() => {
        checkContentAdmin()
      }, 1000)
    } else {
      ElMessage.error(`修复失败: ${response.data?.message}`)
    }
  } catch (error: any) {
    console.error('修复权限失败:', error)
    ElMessage.error(`修复失败: ${error.response?.data?.message || error.message}`)
  }
}
</script>

<style scoped>
.quick-test {
  padding: 20px;
}

.button-group {
  margin-bottom: 20px;
}

.button-group .el-button {
  margin-right: 10px;
}

.result-section {
  margin-top: 20px;
}

.el-card {
  margin-bottom: 10px;
}

.permissions-grid {
  display: grid;
  gap: 10px;
  max-height: 400px;
  overflow-y: auto;
}

.permission-item {
  display: flex;
  align-items: center;
  padding: 8px;
  border: 1px solid #eee;
  border-radius: 4px;
  gap: 10px;
}

.permission-code {
  font-family: monospace;
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  min-width: 200px;
}

.permission-name {
  flex: 1;
}

.permission-type {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  color: white;
}

.permission-type.menu {
  background: #409eff;
}

.permission-type.button {
  background: #67c23a;
}

.user-permissions {
  max-height: 300px;
  overflow-y: auto;
}

.user-permissions div {
  padding: 4px 0;
  border-bottom: 1px solid #f0f0f0;
}

pre {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
  overflow-x: auto;
}
</style>
</rewritten_file>