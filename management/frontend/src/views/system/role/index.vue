<template>
  <div class="role-management" style="width: 100%; height: 100%; min-height: 500px; background: white; padding: 20px;">
    <div class="page-header">
      <div class="header-title">
        <h2>角色管理</h2>
        <p>管理系统角色信息，分配权限</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          新增角色
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="角色名称">
          <el-input
            v-model="searchForm.roleName"
            placeholder="请输入角色名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="启用" value="1" />
            <el-option label="禁用" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">
            搜索
          </el-button>
          <el-button :icon="Refresh" @click="handleReset">
            重置
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
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="roleId" label="角色ID" width="100" />
        <el-table-column prop="roleName" label="角色名称" min-width="150" />
        <el-table-column prop="roleCode" label="角色编码" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              :icon="Edit"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              type="primary"
              link
              :icon="Key"
              @click="handlePermission(row)"
            >
              权限
            </el-button>
            <el-button
              type="danger"
              link
              :icon="Delete"
              @click="handleDelete(row)"
            >
              删除
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

    <!-- 角色编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="formData.roleCode" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 权限配置对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      title="权限配置"
      width="800px"
    >
      <div class="permission-tree">
        <el-tree
          ref="permissionTreeRef"
          :data="permissionTreeData"
          show-checkbox
          node-key="id"
          :default-expanded-keys="[1]"
          :default-checked-keys="checkedPermissions"
          :props="treeProps"
        />
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="permissionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handlePermissionSubmit" :loading="permissionLoading">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, Key } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { roleApi, permissionApi } from '@/api/system'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const permissionLoading = ref(false)
const dialogVisible = ref(false)
const permissionDialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const permissionTreeRef = ref()

// 搜索表单
const searchForm = reactive({
  roleName: '',
  status: ''
})

// 分页数据
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 表格数据
const tableData = ref([
  {
    id: 1,
    roleId: 1,
    roleName: '超级管理员',
    roleCode: 'SUPER_ADMIN',
    description: '系统超级管理员，拥有所有权限',
    status: 1,
    createTime: '2024-01-01 10:00:00'
  },
  {
    id: 2,
    roleId: 2,
    roleName: '系统管理员',
    roleCode: 'SYSTEM_ADMIN',
    description: '系统管理员，负责系统配置和用户管理',
    status: 1,
    createTime: '2024-01-02 10:00:00'
  },
  {
    id: 3,
    roleId: 3,
    roleName: '内容管理员',
    roleCode: 'CONTENT_ADMIN',
    description: '内容管理员，负责文章和内容管理',
    status: 1,
    createTime: '2024-01-03 10:00:00'
  },
  {
    id: 4,
    roleId: 4,
    roleName: '普通用户',
    roleCode: 'USER',
    description: '普通用户角色',
    status: 0,
    createTime: '2024-01-04 10:00:00'
  }
])

const selectedRows = ref<any[]>([])

// 表单数据
const formData = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  description: '',
  status: 1
})

// 表单验证规则
const formRules = {
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 20, message: '角色名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '角色编码只能包含大写字母和下划线', trigger: 'blur' }
  ]
}

// 权限树数据
const permissionTreeData = ref([
  {
    id: 1,
    label: '系统管理',
    children: [
      { id: 11, label: '用户管理' },
      { id: 12, label: '角色管理' },
      { id: 13, label: '权限管理' },
      { id: 14, label: '操作日志' }
    ]
  },
  {
    id: 2,
    label: '内容管理',
    children: [
      { id: 21, label: '文章管理' },
      { id: 22, label: '分类管理' },
      { id: 23, label: '标签管理' }
    ]
  },
  {
    id: 3,
    label: '用户管理',
    children: [
      { id: 31, label: '用户列表' },
      { id: 32, label: '用户分析' }
    ]
  }
])

const checkedPermissions = ref([11, 12, 21, 22])
const currentRoleId = ref(null)

const treeProps = {
  children: 'children',
  label: 'label'
}

// 方法
const handleSearch = () => {
  pagination.current = 1
  loadTableData()
}

const handleReset = () => {
  Object.assign(searchForm, {
    roleName: '',
    status: ''
  })
  handleSearch()
}

const loadTableData = async () => {
  loading.value = true
  
  try {
    const params = {
      page: pagination.current,
      pageSize: pagination.pageSize,
      roleName: searchForm.roleName || undefined,
      status: searchForm.status !== '' ? searchForm.status : undefined
    }
    
    console.log('加载角色列表，参数:', params)
    const response = await roleApi.getRoleList(params)
    console.log('角色列表响应:', response)
    console.log('响应数据结构:', response.data)
    
    if (response.code === 200 || response.success) {
      // 处理不同的响应数据结构
      let dataList = []
      let total = 0
      
      if (response.data) {
        // 检查是否是分页结构
        if (response.data.records && Array.isArray(response.data.records)) {
          dataList = response.data.records
          total = response.data.total || response.data.records.length
        } else if (response.data.list && Array.isArray(response.data.list)) {
          dataList = response.data.list
          total = response.data.total || response.data.list.length
        } else if (Array.isArray(response.data)) {
          dataList = response.data
          total = response.data.length
        } else {
          console.warn('未识别的数据结构:', response.data)
          dataList = []
          total = 0
        }
      }
      
      console.log('处理后的角色列表:', dataList)
      console.log('角色总数:', total)
      
      tableData.value = dataList
      pagination.total = total
      
      if (dataList.length > 0) {
        ElMessage.success(`角色列表加载成功，共${total}条数据`)
      } else {
        ElMessage.info('暂无角色数据')
      }
    } else {
      ElMessage.error(response.message || '获取角色列表失败')
      tableData.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error('获取角色列表失败，请检查网络连接')
    // 使用模拟数据作为fallback
    tableData.value = [
      {
        id: 1,
        roleId: 1,
        roleName: '超级管理员',
        roleCode: 'SUPER_ADMIN',
        description: '系统超级管理员，拥有所有权限',
        status: 1,
        createTime: '2024-01-01 10:00:00'
      }
    ]
    pagination.total = 1
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增角色'
  isEdit.value = false
  Object.assign(formData, {
    id: null,
    roleName: '',
    roleCode: '',
    description: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑角色'
  isEdit.value = true
  Object.assign(formData, { ...row })
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除角色 "${row.roleName}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      console.log('删除角色:', row.id)
      const response = await roleApi.deleteRole(row.id)
      console.log('删除角色响应:', response)
      
      if (response.code === 200) {
        ElMessage.success('删除成功')
        loadTableData()
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    } catch (error) {
      console.error('删除角色失败:', error)
      ElMessage.error('删除失败，请检查网络连接')
    }
  }).catch(() => {
    // 取消删除
  })
}

const handlePermission = async (row: any) => {
  currentRoleId.value = row.id
  permissionDialogVisible.value = true
  
  try {
    // 加载权限树数据
    console.log('加载权限树数据')
    const treeResponse = await permissionApi.getPermissionTree()
    if (treeResponse.code === 200) {
      permissionTreeData.value = treeResponse.data || []
    }
    
    // 加载角色已有权限
    console.log('加载角色权限:', row.id)
    const permResponse = await roleApi.getRolePermissions(row.id)
    if (permResponse.code === 200) {
      checkedPermissions.value = permResponse.data.map((p: any) => p.id) || []
    }
  } catch (error) {
    console.error('加载权限数据失败:', error)
    ElMessage.error('加载权限数据失败')
  }
}

const handleDialogClose = () => {
  dialogVisible.value = false
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate()
  if (valid) {
    submitLoading.value = true
    try {
      const submitData = {
        roleName: formData.roleName,
        roleCode: formData.roleCode,
        description: formData.description,
        status: formData.status
      }
      
      console.log('提交角色数据:', submitData)
      
      let response
      if (isEdit.value && formData.id) {
        response = await roleApi.updateRole(formData.id, submitData)
      } else {
        response = await roleApi.createRole(submitData)
      }
      
      console.log('角色操作响应:', response)
      
      if (response.code === 200) {
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
        dialogVisible.value = false
        loadTableData()
      } else {
        ElMessage.error(response.message || '操作失败')
      }
    } catch (error) {
      console.error('角色操作失败:', error)
      ElMessage.error('操作失败，请检查网络连接')
    } finally {
      submitLoading.value = false
    }
  }
}

const handlePermissionSubmit = async () => {
  if (!currentRoleId.value) return
  
  const checkedNodes = permissionTreeRef.value.getCheckedNodes()
  const checkedKeys = permissionTreeRef.value.getCheckedKeys()
  
  permissionLoading.value = true
  try {
    console.log('提交权限配置:', currentRoleId.value, checkedKeys)
    const response = await roleApi.assignRolePermissions(currentRoleId.value, checkedKeys)
    console.log('权限配置响应:', response)
    
    if (response.code === 200) {
      ElMessage.success('权限配置成功')
      permissionDialogVisible.value = false
      loadTableData()
    } else {
      ElMessage.error(response.message || '权限配置失败')
    }
  } catch (error) {
    console.error('权限配置失败:', error)
    ElMessage.error('权限配置失败，请检查网络连接')
  } finally {
    permissionLoading.value = false
  }
}

const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  loadTableData()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  loadTableData()
}

// 组件挂载时加载数据
onMounted(() => {
  loadTableData()
})
</script>

<style lang="scss" scoped>
.role-management {
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

  .permission-tree {
    max-height: 400px;
    overflow-y: auto;
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    padding: 16px;
  }
}

// 对话框样式调整
:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}
</style> 