<template>
  <div class="permission-tree-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <h2>权限管理 - 树形结构</h2>
        <p>基于树形结构的权限配置和菜单访问控制</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          新增权限
        </el-button>
        <el-button type="success" :icon="Refresh" @click="handleRefresh">
          刷新权限树
        </el-button>
        <el-button type="info" :icon="Expand" @click="handleExpandAll" v-if="!isAllExpanded">
          展开全部
        </el-button>
        <el-button type="info" :icon="Fold" @click="handleCollapseAll" v-else>
          收起全部
        </el-button>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="权限名称">
          <el-input
            v-model="searchForm.permissionName"
            placeholder="请输入权限名称"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          />
        </el-form-item>
        <el-form-item label="权限类型">
          <el-select v-model="searchForm.permissionType" placeholder="请选择权限类型" clearable @change="handleSearch">
            <el-option label="菜单权限" value="menu" />
            <el-option label="按钮权限" value="button" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable @change="handleSearch">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
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

    <!-- 权限树表格 -->
    <div class="table-container">
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="filteredTreeData"
        style="width: 100%"
        row-key="id"
        :default-expand-all="isAllExpanded"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        @selection-change="handleSelectionChange"
        :header-cell-style="{ background: '#f5f7fa', color: '#303133' }"
        class="permission-tree-table"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="permissionName" label="权限名称" min-width="250">
          <template #default="{ row }">
            <div class="permission-name">
              <el-icon v-if="row.icon" class="permission-icon">
                <component :is="row.icon" />
              </el-icon>
              <span>{{ row.permissionName }}</span>
              <el-tag v-if="row.children && row.children.length > 0" type="info" size="small" class="children-count">
                {{ row.children.length }}个子权限
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="permissionCode" label="权限编码" min-width="180" show-overflow-tooltip />
        <el-table-column prop="permissionType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag 
              :type="row.permissionType === 'menu' ? 'primary' : 'success'"
              size="small"
            >
              {{ row.permissionType === 'menu' ? '菜单' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="menuPath" label="路径/URL" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.menuPath" class="path-text">{{ row.menuPath }}</span>
            <span v-else class="text-gray">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="componentPath" label="组件路径" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.componentPath" class="component-text">{{ row.componentPath }}</span>
            <span v-else class="text-gray">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.sortOrder }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
              :disabled="loading"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button
                type="primary"
                link
                :icon="Plus"
                @click="handleAddChild(row)"
                v-if="row.permissionType === 'menu'"
                size="small"
              >
                添加子权限
              </el-button>
              <el-button
                type="warning"
                link
                :icon="Edit"
                @click="handleEdit(row)"
                size="small"
              >
                编辑
              </el-button>
              <el-button
                type="danger"
                link
                :icon="Delete"
                @click="handleDelete(row)"
                size="small"
              >
                删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 权限编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="900px"
      :before-close="handleDialogClose"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="permission-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="权限名称" prop="permissionName">
              <el-input v-model="formData.permissionName" placeholder="请输入权限名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权限编码" prop="permissionCode">
              <el-input v-model="formData.permissionCode" placeholder="请输入权限编码，如：system:user:add" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="权限类型" prop="permissionType">
              <el-select v-model="formData.permissionType" @change="handleTypeChange" style="width: 100%">
                <el-option label="菜单权限" value="menu" />
                <el-option label="按钮权限" value="button" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="父级权限" prop="parentId">
              <el-tree-select
                v-model="formData.parentId"
                :data="parentPermissionOptions"
                :props="treeSelectProps"
                placeholder="请选择父级权限"
                check-strictly
                :render-after-expand="false"
                style="width: 100%"
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20" v-if="formData.permissionType === 'menu'">
          <el-col :span="12">
            <el-form-item label="菜单路径" prop="menuPath">
              <el-input v-model="formData.menuPath" placeholder="请输入菜单路径，如：/system/user" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="组件路径" prop="componentPath">
              <el-input v-model="formData.componentPath" placeholder="请输入组件路径，如：system/user/index" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="图标" prop="icon">
              <el-input v-model="formData.icon" placeholder="请输入图标名称，如：User" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="formData.sortOrder" :min="0" :max="999" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :label="1">启用</el-radio>
                <el-radio :label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="权限描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入权限描述"
            maxlength="200"
            show-word-limit
          />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Edit, Delete, Expand, Fold } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { permissionApi } from '@/api/system'

// 响应式数据
const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('')
const selectedRows = ref<any[]>([])
const isAllExpanded = ref(true)
const tableRef = ref()

// 权限树数据
const permissionTreeData = ref<any[]>([])

// 搜索表单
const searchForm = reactive({
  permissionName: '',
  permissionType: '',
  status: ''
})

// 表单数据
const formData = reactive({
  id: null,
  permissionName: '',
  permissionCode: '',
  permissionType: 'menu',
  parentId: null,
  menuPath: '',
  componentPath: '',
  icon: '',
  sortOrder: 0,
  description: '',
  status: 1
})

// 表单验证规则
const formRules = {
  permissionName: [
    { required: true, message: '请输入权限名称', trigger: 'blur' }
  ],
  permissionCode: [
    { required: true, message: '请输入权限编码', trigger: 'blur' }
  ],
  permissionType: [
    { required: true, message: '请选择权限类型', trigger: 'change' }
  ],
  menuPath: [
    { required: true, message: '请输入菜单路径', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序', trigger: 'blur' }
  ]
}

const formRef = ref<FormInstance>()

// 过滤后的树数据
const filteredTreeData = computed(() => {
  if (!searchForm.permissionName && !searchForm.permissionType && searchForm.status === '') {
    return permissionTreeData.value
  }
  
  // 递归过滤函数
  const filterTree = (data: any[]): any[] => {
    return data.filter(item => {
      // 检查当前节点是否匹配
      let matches = true
      
      if (searchForm.permissionName) {
        matches = matches && item.permissionName.includes(searchForm.permissionName)
      }
      
      if (searchForm.permissionType) {
        matches = matches && item.permissionType === searchForm.permissionType
      }
      
      if (searchForm.status !== '') {
        matches = matches && item.status === searchForm.status
      }
      
      // 递归检查子节点
      if (item.children && item.children.length > 0) {
        const filteredChildren = filterTree(item.children)
        if (filteredChildren.length > 0) {
          return {
            ...item,
            children: filteredChildren
          }
        }
      }
      
      return matches
    }).map(item => ({
      ...item,
      children: item.children ? filterTree(item.children) : []
    }))
  }
  
  return filterTree(permissionTreeData.value)
})

// 父权限选项
const parentPermissionOptions = computed(() => {
  const buildParentOptions = (data: any[], level = 0): any[] => {
    return data.reduce((acc, item) => {
      if (item.permissionType === 'menu' && item.id !== formData.id) {
        acc.push({
          value: item.id,
          label: `${'　'.repeat(level)}${item.permissionName}`,
          children: item.children ? buildParentOptions(item.children, level + 1) : []
        })
      }
      return acc
    }, [] as any[])
  }
  
  return [
    { value: null, label: '顶级权限' },
    ...buildParentOptions(permissionTreeData.value)
  ]
})

const treeSelectProps = {
  value: 'value',
  label: 'label',
  children: 'children'
}

// 方法实现
const handleSearch = () => {
  console.log('搜索权限树:', searchForm)
}

const handleReset = () => {
  Object.assign(searchForm, {
    permissionName: '',
    permissionType: '',
    status: ''
  })
}

const handleRefresh = () => {
  loadPermissionTree()
}

const handleExpandAll = () => {
  isAllExpanded.value = true
  if (tableRef.value) {
    expandAllNodes(permissionTreeData.value)
  }
}

const handleCollapseAll = () => {
  isAllExpanded.value = false
  if (tableRef.value) {
    collapseAllNodes(permissionTreeData.value)
  }
}

const expandAllNodes = (data: any[]) => {
  data.forEach(item => {
    if (tableRef.value) {
      tableRef.value.toggleRowExpansion(item, true)
    }
    if (item.children && item.children.length > 0) {
      expandAllNodes(item.children)
    }
  })
}

const collapseAllNodes = (data: any[]) => {
  data.forEach(item => {
    if (tableRef.value) {
      tableRef.value.toggleRowExpansion(item, false)
    }
    if (item.children && item.children.length > 0) {
      collapseAllNodes(item.children)
    }
  })
}

const loadPermissionTree = async () => {
  loading.value = true
  
  try {
    console.log('加载权限树数据')
    const response = await permissionApi.getPermissionTree()
    console.log('权限树响应:', response)
    
    if (response.code === 200) {
      permissionTreeData.value = response.data || []
      console.log('权限树数据加载成功:', permissionTreeData.value)
      ElMessage.success(`权限树加载成功，共${countTreeNodes(permissionTreeData.value)}个权限`)
    } else {
      ElMessage.error(response.message || '获取权限树失败')
      permissionTreeData.value = getMockPermissionTree()
    }
  } catch (error) {
    console.error('获取权限树失败:', error)
    ElMessage.error('获取权限树失败，请检查网络连接')
    permissionTreeData.value = getMockPermissionTree()
  } finally {
    loading.value = false
  }
}

const countTreeNodes = (data: any[]): number => {
  let count = 0
  data.forEach(item => {
    count += 1
    if (item.children && item.children.length > 0) {
      count += countTreeNodes(item.children)
    }
  })
  return count
}

const getMockPermissionTree = () => {
  return [
    {
      id: 1,
      permissionName: '系统管理',
      permissionCode: 'system',
      permissionType: 'menu',
      parentId: null,
      menuPath: '/system',
      componentPath: 'system/index',
      icon: 'Setting',
      sortOrder: 1,
      status: 1,
      description: '系统管理模块',
      children: [
        {
          id: 11,
          permissionName: '用户管理',
          permissionCode: 'system:user',
          permissionType: 'menu',
          parentId: 1,
          menuPath: '/system/user',
          componentPath: 'system/user/index',
          icon: 'User',
          sortOrder: 1,
          status: 1,
          description: '用户管理页面',
          children: [
            {
              id: 111,
              permissionName: '新增用户',
              permissionCode: 'system:user:add',
              permissionType: 'button',
              parentId: 11,
              menuPath: '',
              componentPath: '',
              icon: '',
              sortOrder: 1,
              status: 1,
              description: '新增用户按钮'
            }
          ]
        }
      ]
    }
  ]
}

const handleAdd = () => {
  dialogTitle.value = '新增权限'
  isEdit.value = false
  Object.assign(formData, {
    id: null,
    permissionName: '',
    permissionCode: '',
    permissionType: 'menu',
    parentId: null,
    menuPath: '',
    componentPath: '',
    icon: '',
    sortOrder: 0,
    description: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleAddChild = (row: any) => {
  dialogTitle.value = `新增子权限 - ${row.permissionName}`
  isEdit.value = false
  Object.assign(formData, {
    id: null,
    permissionName: '',
    permissionCode: '',
    permissionType: 'button',
    parentId: row.id,
    menuPath: '',
    componentPath: '',
    icon: '',
    sortOrder: 0,
    description: '',
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑权限'
  isEdit.value = true
  Object.assign(formData, { 
    ...row, 
    parentId: row.parentId || null 
  })
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  if (row.children && row.children.length > 0) {
    ElMessage.warning('该权限包含子权限，请先删除子权限')
    return
  }
  
  ElMessageBox.confirm(
    `确定要删除权限 "${row.permissionName}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const response = await permissionApi.deletePermission(row.id)
      
      if (response.code === 200) {
        ElMessage.success('删除成功')
        loadPermissionTree()
      } else {
        ElMessage.error(response.message || '删除失败')
      }
    } catch (error) {
      console.error('删除权限失败:', error)
      ElMessage.error('删除失败，请检查网络连接')
    }
  })
}

const handleStatusChange = async (row: any) => {
  try {
    const response = await permissionApi.updatePermission(row.id, {
      id: row.id,
      status: row.status
    })
    
    if (response.code === 200) {
      ElMessage.success(row.status ? '启用成功' : '禁用成功')
    } else {
      row.status = row.status === 1 ? 0 : 1
      ElMessage.error(response.message || '状态修改失败')
    }
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
    console.error('修改权限状态失败:', error)
    ElMessage.error('状态修改失败，请检查网络连接')
  }
}

const handleTypeChange = (type: string) => {
  if (type === 'button') {
    formData.menuPath = ''
    formData.componentPath = ''
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
        id: formData.id,
        permissionName: formData.permissionName,
        permissionCode: formData.permissionCode,
        permissionType: formData.permissionType,
        parentId: formData.parentId,
        menuPath: formData.menuPath,
        componentPath: formData.componentPath,
        icon: formData.icon,
        sortOrder: formData.sortOrder,
        description: formData.description,
        status: formData.status
      }
      
      let response
      if (isEdit.value && formData.id) {
        response = await permissionApi.updatePermission(formData.id, submitData)
      } else {
        response = await permissionApi.createPermission(submitData)
      }
      
      if (response.code === 200) {
        ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
        dialogVisible.value = false
        loadPermissionTree()
      } else {
        ElMessage.error(response.message || '操作失败')
      }
    } catch (error) {
      console.error('权限操作失败:', error)
      ElMessage.error('操作失败，请检查网络连接')
    } finally {
      submitLoading.value = false
    }
  }
}

const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection
}

onMounted(() => {
  loadPermissionTree()
})
</script>

<style lang="scss" scoped>
.permission-tree-management {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
  
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;
    padding: 20px;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    
    .header-title {
      h2 {
        margin: 0 0 8px 0;
        color: #303133;
        font-size: 24px;
        font-weight: 600;
      }
      
      p {
        margin: 0;
        color: #909399;
        font-size: 14px;
      }
    }
    
    .header-actions {
      display: flex;
      gap: 12px;
    }
  }

  .search-bar {
    background: white;
    padding: 20px;
    border-radius: 8px;
    margin-bottom: 16px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    
    .search-form {
      margin: 0;
      
      .el-form-item {
        margin-bottom: 0;
      }
    }
  }

  .table-container {
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    overflow: hidden;
    
    .permission-tree-table {
      .permission-name {
        display: flex;
        align-items: center;
        
        .permission-icon {
          margin-right: 8px;
          color: #409eff;
        }
        
        .children-count {
          margin-left: 8px;
        }
      }
      
      .path-text {
        color: #409eff;
        font-family: 'Courier New', monospace;
      }
      
      .component-text {
        color: #67c23a;
        font-family: 'Courier New', monospace;
      }
      
      .text-gray {
        color: #c0c4cc;
        font-style: italic;
      }
      
      .action-buttons {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
      }
    }
  }
  
  .permission-form {
    .el-form-item {
      margin-bottom: 20px;
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
  }
}

// 全局样式覆盖
:deep(.el-table) {
  .el-table__row {
    &:hover {
      background-color: #f5f7fa !important;
    }
  }
  
  .el-table__header {
    .el-table__cell {
      background-color: #fafafa !important;
      color: #606266 !important;
      font-weight: 600;
      border-bottom: 1px solid #ebeef5;
    }
  }
  
  .el-table__expand-icon {
    color: #409eff;
  }
}

// 权限树特定样式
:deep(.permission-tree-table) {
  .el-table__row {
    &[aria-level="1"] {
      background-color: #f8faff;
      font-weight: 500;
      
      .el-table__cell {
        border-bottom: 2px solid #e6ebf5;
      }
    }
    
    &[aria-level="2"] {
      background-color: #fbfcff;
      
      .permission-name {
        padding-left: 10px;
      }
    }
    
    &[aria-level="3"] {
      .permission-name {
        padding-left: 20px;
        color: #666;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .permission-tree-management {
    padding: 10px;
    
    .page-header {
      flex-direction: column;
      gap: 16px;
      
      .header-actions {
        flex-wrap: wrap;
      }
    }
    
    .search-bar {
      .search-form {
        .el-form-item {
          margin-bottom: 16px;
        }
      }
    }
    
    .table-container {
      .permission-tree-table {
        .action-buttons {
          flex-direction: column;
          gap: 4px;
        }
      }
    }
  }
}
</style> 