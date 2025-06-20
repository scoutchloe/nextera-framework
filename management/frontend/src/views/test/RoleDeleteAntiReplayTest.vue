<template>
  <div class="role-delete-test">
    <h2>角色删除防重放测试</h2>
    <p>测试角色删除接口的防重放攻击保护功能</p>
    
    <el-card header="测试说明" style="margin-bottom: 20px;">
      <p><strong>防重放机制：</strong>时间窗口5分钟，启用Nonce验证</p>
      <p><strong>请求头部：</strong>X-Timestamp, X-Nonce</p>
    </el-card>

    <el-card header="角色列表" style="margin-bottom: 20px;">
      <el-button @click="loadRoles" :loading="loading" type="primary" style="margin-bottom: 16px;">
        刷新角色列表
      </el-button>
      <el-table :data="roles" style="width: 100%;">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" />
        <el-table-column prop="roleCode" label="角色编码" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="selectRole(row)">
              选择测试
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card header="测试操作" style="margin-bottom: 20px;" v-if="selectedRole">
      <p>选中角色: {{ selectedRole.roleName }} (ID: {{ selectedRole.id }})</p>
      <el-row :gutter="16">
        <el-col :span="6">
          <el-button type="success" @click="testNormalDelete" block>
            正常删除测试
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="warning" @click="testReplayAttack" :disabled="!lastHeaders" block>
            重放攻击测试
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="danger" @click="testExpiredRequest" block>
            过期请求测试
          </el-button>
        </el-col>
        <el-col :span="6">
          <el-button type="info" @click="testWithoutHeaders" block>
            无头部测试
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card header="测试结果">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="正常删除" name="normal">
          <pre>{{ results.normal || '暂无结果' }}</pre>
        </el-tab-pane>
        <el-tab-pane label="重放攻击" name="replay">
          <pre>{{ results.replay || '暂无结果' }}</pre>
        </el-tab-pane>
        <el-tab-pane label="过期请求" name="expired">
          <pre>{{ results.expired || '暂无结果' }}</pre>
        </el-tab-pane>
        <el-tab-pane label="无头部" name="noHeaders">
          <pre>{{ results.noHeaders || '暂无结果' }}</pre>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { roleApi } from '@/api/system'
import { generateAntiReplayHeaders } from '@/utils/antiReplay'
import axios from 'axios'

const roles = ref<any[]>([])
const selectedRole = ref<any>(null)
const loading = ref(false)
const activeTab = ref('normal')
const lastHeaders = ref<any>(null)

const results = reactive({
  normal: '',
  replay: '',
  expired: '',
  noHeaders: ''
})

const loadRoles = async () => {
  loading.value = true
  try {
    const response = await roleApi.getRoleList({ current: 1, size: 10 })
    if (response.code === 200) {
      roles.value = response.data.records || []
    }
  } catch (error) {
    ElMessage.error('加载角色列表失败')
  }
  loading.value = false
}

const selectRole = (role: any) => {
  selectedRole.value = role
  // 清空结果
  Object.keys(results).forEach(key => {
    results[key as keyof typeof results] = ''
  })
  lastHeaders.value = null
}

const testNormalDelete = async () => {
  if (!selectedRole.value) return
  
  try {
    const headers = generateAntiReplayHeaders('DELETE', `/system/role/${selectedRole.value.id}`, '')
    lastHeaders.value = headers
    
    const response = await axios.delete(`/api/system/role/${selectedRole.value.id}`, { headers })
    results.normal = JSON.stringify(response.data, null, 2)
    ElMessage.success('正常删除测试完成')
    await loadRoles()
  } catch (error: any) {
    results.normal = JSON.stringify(error.response?.data || error.message, null, 2)
    ElMessage.error('正常删除测试失败')
  }
}

const testReplayAttack = async () => {
  if (!selectedRole.value || !lastHeaders.value) return
  
  try {
    // 使用相同的头部再次发送请求
    const response = await axios.delete(`/api/system/role/${selectedRole.value.id}`, { 
      headers: lastHeaders.value 
    })
    results.replay = JSON.stringify(response.data, null, 2)
    ElMessage.warning('重放攻击成功（不应该发生）')
  } catch (error: any) {
    results.replay = JSON.stringify(error.response?.data || error.message, null, 2)
    ElMessage.success('重放攻击被成功阻止')
  }
}

const testExpiredRequest = async () => {
  if (!selectedRole.value) return
  
  try {
    // 生成过期的时间戳（10分钟前）
    const expiredHeaders = {
      'X-Timestamp': (Date.now() - 10 * 60 * 1000).toString(),
      'X-Nonce': Math.random().toString(36).substring(2, 15)
    }
    
    const response = await axios.delete(`/api/system/role/${selectedRole.value.id}`, { 
      headers: expiredHeaders 
    })
    results.expired = JSON.stringify(response.data, null, 2)
    ElMessage.warning('过期请求成功（不应该发生）')
  } catch (error: any) {
    results.expired = JSON.stringify(error.response?.data || error.message, null, 2)
    ElMessage.success('过期请求被成功阻止')
  }
}

const testWithoutHeaders = async () => {
  if (!selectedRole.value) return
  
  try {
    const response = await axios.delete(`/api/system/role/${selectedRole.value.id}`)
    results.noHeaders = JSON.stringify(response.data, null, 2)
    ElMessage.warning('无头部请求成功（不应该发生）')
  } catch (error: any) {
    results.noHeaders = JSON.stringify(error.response?.data || error.message, null, 2)
    ElMessage.success('无头部请求被成功阻止')
  }
}

onMounted(() => {
  loadRoles()
})
</script>

<style scoped>
pre {
  background: #f5f5f5;
  padding: 16px;
  border-radius: 4px;
  overflow-x: auto;
  white-space: pre-wrap;
}
</style> 