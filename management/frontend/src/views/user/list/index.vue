<template>
  <div class="user-list">
    <div class="page-header">
      <div class="header-title">
        <h2>{{ t('system.user.list.title') }}</h2>
        <p>{{ t('system.user.list.subtitle') }}</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          {{ t('system.user.list.addUser') }}
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="t('system.user.list.usernameLabel')">
          <el-input
            v-model="searchForm.username"
            :placeholder="t('system.user.list.usernamePlaceholder')"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item :label="t('system.user.list.nicknameLabel')">
          <el-input
            v-model="searchForm.nickname"
            :placeholder="t('system.user.list.nicknamePlaceholder')"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item :label="t('system.user.list.emailLabel')">
          <el-input
            v-model="searchForm.email"
            :placeholder="t('system.user.list.emailPlaceholder')"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item :label="t('system.user.list.statusLabel')">
          <el-select v-model="searchForm.status" :placeholder="t('system.user.list.statusPlaceholder')" clearable>
            <el-option :label="t('system.user.list.normal')" value="1" />
            <el-option :label="t('system.user.list.disabled')" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">
            {{ t('common.search') }}
          </el-button>
          <el-button :icon="Refresh" @click="handleReset">
            {{ t('common.reset') }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
        row-key="id"
        stripe
      >
        <el-table-column prop="avatar" :label="t('system.user.list.avatar')" width="80">
          <template #default="{ row }">
            <el-avatar :src="row.avatar" :size="40">
              {{ row.username?.charAt(0) }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" :label="t('system.user.list.username')" width="120" />
        <el-table-column prop="nickname" :label="t('system.user.list.nickname')" width="120" />
        <el-table-column prop="email" :label="t('system.user.list.email')" min-width="180" />
        <el-table-column prop="phone" :label="t('system.user.list.phone')" width="120" />
        <el-table-column prop="gender" :label="t('system.user.list.gender')" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.gender === 1" type="primary">{{ t('system.user.list.male') }}</el-tag>
            <el-tag v-else-if="row.gender === 2" type="success">{{ t('system.user.list.female') }}</el-tag>
            <el-tag v-else type="info">{{ t('system.user.list.unknown') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="t('system.user.list.status')" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="success">{{ t('system.user.list.normal') }}</el-tag>
            <el-tag v-else type="danger">{{ t('system.user.list.disabled') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" :label="t('system.user.list.lastLoginTime')" width="180" />
        <el-table-column prop="createTime" :label="t('system.user.list.createTime')" width="180" />
        <el-table-column :label="t('system.user.list.operation')" width="240" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              :icon="Edit"
              @click="handleEdit(row)"
            >
              {{ t('system.user.list.edit') }}
            </el-button>
            <el-button
              :type="row.status === 0 ? 'warning' : 'success'"
              link
              @click="handleStatusChange(row)"
            >
              {{ row.status === 0 ? t('system.user.list.disable') : t('system.user.list.enable') }}
            </el-button>
            <el-button
              type="info"
              link
              @click="handleResetPassword(row)"
            >
              {{ t('system.user.list.resetPassword') }}
            </el-button>
            <el-button
              type="danger"
              link
              :icon="Delete"
              @click="handleDelete(row)"
            >
              {{ t('system.user.list.delete') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          :current-page="pagination.current"
          :page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { userApi } from '@/api/system'

const { t } = useI18n()

// 响应式数据
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  username: '',
  nickname: '',
  email: '',
  status: ''
})

// 分页数据
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 表格数据
const tableData = ref<any[]>([])

// 方法
const handleSearch = () => {
  pagination.current = 1
  loadTableData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    username: '',
    nickname: '',
    email: '',
    status: ''
  })
  handleSearch()
}

const loadTableData = async () => {
  try {
    loading.value = true
    const params = {
      current: pagination.current,
      size: pagination.pageSize,
      ...searchForm
    }
    
    const response = await userApi.getUserList(params)
    
    if (response.code === 200) {
      tableData.value = response.data.records
      pagination.total = response.data.total
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  ElMessage.info('新增用户功能开发中...')
}

const handleEdit = (row: any) => {
  ElMessage.info(`编辑用户 ${row.username} 功能开发中...`)
}

const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${row.username}" 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await userApi.deleteUser(row.id)
    
    ElMessage.success('删除成功')
    loadTableData()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error('删除用户失败')
    }
  }
}

const handleStatusChange = async (row: any) => {
  try {
    const newStatus = row.status === 0 ? 1 : 0
    
    await userApi.updateUserStatus(row.id, newStatus)
    
    row.status = newStatus
    row.statusName = newStatus === 0 ? '正常' : '禁用'
    ElMessage.success(newStatus === 0 ? '启用成功' : '禁用成功')
  } catch (error) {
    console.error('更新用户状态失败:', error)
    ElMessage.error('更新用户状态失败')
  }
}

const handleResetPassword = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要重置用户 "${row.username}" 的密码吗？重置后密码为：123456`,
      '重置密码确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await userApi.resetUserPassword(row.id, '123456')
    
    ElMessage.success('密码重置成功，新密码为：123456')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重置密码失败:', error)
      ElMessage.error('重置密码失败')
    }
  }
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  loadTableData()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadTableData()
}

onMounted(() => {
  loadTableData()
})
</script>

<style lang="scss" scoped>
.user-list {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--border-color);
    
    .header-title {
      h2 {
        margin: 0 0 8px 0;
        color: var(--text-primary);
        font-size: 24px;
        font-weight: 600;
      }
      
      p {
        margin: 0;
        color: var(--text-secondary);
        font-size: 14px;
      }
    }
  }

  .search-bar {
    background: var(--bg-primary);
    padding: 20px;
    border-radius: var(--radius-md);
    margin-bottom: 16px;
    border: 1px solid var(--border-color);
    
    .search-form {
      margin: 0;
    }
  }

  .table-container {
    background: var(--bg-primary);
    border-radius: var(--radius-md);
    border: 1px solid var(--border-color);
    overflow: hidden;
    
    .el-table {
      border: none;
    }
    
    .pagination-container {
      padding: 16px 20px;
      border-top: 1px solid var(--border-color);
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style> 