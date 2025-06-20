<template>
  <div class="permission-test">
    <el-card header="权限测试页面">
      <el-button @click="testUserPermissions" type="primary">测试当前用户权限</el-button>
      <el-button @click="fixContentAdminPermissions" type="warning">修复内容管理员权限</el-button>
      
      <div v-if="testResult" class="test-result">
        <h3>测试结果：</h3>
        <div class="user-info">
          <h4>用户信息：</h4>
          <p>用户名: {{ testResult.username }}</p>
          <p>是否管理员: {{ testResult.isAdmin }}</p>
          <p>角色: {{ testResult.roles?.join(', ') || '无' }}</p>
        </div>
        
        <div class="permissions-info">
          <h4>用户权限 ({{ testResult.permissions?.length || 0 }}个)：</h4>
          <ul v-if="testResult.permissions?.length">
            <li v-for="perm in testResult.permissions" :key="perm">{{ perm }}</li>
          </ul>
          <p v-else>无权限</p>
        </div>
        
        <div class="menu-info">
          <h4>应该显示的菜单：</h4>
          <ul>
            <li v-for="menu in expectedMenus" :key="menu.title">
              {{ menu.title }} - 权限: {{ menu.permissions?.join(', ') || '无' }}
              <span :style="{ color: menu.hasPermission ? 'green' : 'red' }">
                ({{ menu.hasPermission ? '✓' : '✗' }})
              </span>
            </li>
          </ul>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import axios from 'axios'

const userStore = useUserStore()
const testResult = ref<any>(null)

// 预期的菜单配置
const expectedMenus = ref([
  { title: '仪表盘', permissions: ['dashboard:view'], hasPermission: false },
  { title: '系统管理', permissions: ['system:view'], hasPermission: false },
  { title: '用户管理', permissions: ['user:view'], hasPermission: false },
  { title: '文章管理', permissions: ['content:view'], hasPermission: false }
])

const testUserPermissions = async () => {
  try {
    // 获取用户权限信息
    const userInfo = userStore.userInfo
    const permissions = userStore.permissions || []
    const roles = userStore.roles || []
    
    testResult.value = {
      username: userInfo?.username || '未知',
      isAdmin: userStore.isAdmin,
      roles: roles,
      permissions: permissions
    }
    
    // 检查每个菜单的权限
    expectedMenus.value.forEach(menu => {
      if (menu.permissions && menu.permissions.length > 0) {
        menu.hasPermission = menu.permissions.some(perm => 
          userStore.hasPermission(perm)
        )
      } else {
        menu.hasPermission = true
      }
    })
    
    ElMessage.success('权限测试完成')
  } catch (error) {
    console.error('权限测试失败:', error)
    ElMessage.error('权限测试失败')
  }
}

const fixContentAdminPermissions = async () => {
  try {
    const response = await axios.post('/api/debug/fix-content-admin')
    if (response.data?.code === 200) {
      ElMessage.success('内容管理员权限修复成功，请重新登录测试')
    } else {
      ElMessage.error(`修复失败: ${response.data?.message || '未知错误'}`)
    }
  } catch (error: any) {
    console.error('修复权限失败:', error)
    ElMessage.error(`修复失败: ${error.response?.data?.message || error.message}`)
  }
}
</script>

<style scoped>
.permission-test {
  padding: 20px;
}

.test-result {
  margin-top: 20px;
  padding: 15px;
  background: #f5f5f5;
  border-radius: 5px;
}

.user-info, .permissions-info, .menu-info {
  margin-bottom: 20px;
}

.permissions-info ul, .menu-info ul {
  max-height: 200px;
  overflow-y: auto;
}

.permissions-info li, .menu-info li {
  padding: 2px 0;
}
</style> 