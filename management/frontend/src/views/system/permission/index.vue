<template>
  <div class="permission-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-title">
        <h2>权限管理</h2>
        <p>管理系统权限配置和菜单访问控制</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" :icon="Plus" @click="handleAdd">
          新增一级菜单
        </el-button>
        <el-button type="success" :icon="Refresh" @click="handleRefresh">
          刷新权限树
        </el-button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 左侧：一级菜单树 -->
      <div class="left-panel">
        <div class="panel-header">
          <h3>一级菜单</h3>
          <el-button link :icon="Plus" @click="handleAdd" size="small">
            新增
          </el-button>
        </div>
        
        <div class="menu-tree-container">
          <el-tree
            ref="menuTreeRef"
            v-loading="treeLoading"
            :data="firstLevelMenus"
            :props="treeProps"
            node-key="id"
            :highlight-current="true"
            :expand-on-click-node="false"
            @node-click="handleMenuClick"
            class="menu-tree"
          >
            <template #default="{ data }">
              <div class="tree-node">
                <div class="node-content">
                  <el-icon v-if="data.icon" class="node-icon">
                    <component :is="getIconComponent(data.icon)" />
                  </el-icon>
                  <span class="node-label">{{ data.permissionName }}</span>
                  <el-tag v-if="data.children && data.children.length > 0" type="info" size="small" class="child-count">
                    {{ data.children.length }}
                  </el-tag>
                </div>
                <div class="node-actions" @click.stop>
                  <el-button link :icon="Plus" @click="handleAddSubMenu(data)" size="small" title="添加子菜单" />
                  <el-button link :icon="Edit" @click="handleEdit(data)" size="small" title="编辑" />
                  <el-button link :icon="Delete" @click="handleDelete(data)" size="small" title="删除" />
                </div>
              </div>
            </template>
          </el-tree>
        </div>
      </div>

      <!-- 右侧：二级权限列表 -->
      <div class="right-panel">
        <div class="panel-header">
          <h3>
            <span v-if="selectedMenu">{{ selectedMenu.permissionName }} - 子权限</span>
            <span v-else>请选择左侧菜单</span>
          </h3>
          <div class="header-actions" v-if="selectedMenu">
            <el-button type="primary" :icon="Plus" @click="handleAddChildPermission" size="small">
              新增子权限
            </el-button>
            <el-button type="info" :icon="Refresh" @click="loadChildPermissions" size="small">
              刷新
            </el-button>
          </div>
        </div>

        <!-- 搜索栏 -->
        <div class="search-bar" v-if="selectedMenu">
          <el-form :inline="true" :model="searchForm" class="search-form">
            <el-form-item label="权限名称">
              <el-input
                v-model="searchForm.permissionName"
                placeholder="请输入权限名称"
                clearable
                @keyup.enter="handleSearch"
                @clear="handleSearch"
                style="width: 200px"
              />
            </el-form-item>
            <el-form-item label="权限类型">
              <el-select v-model="searchForm.permissionType" placeholder="请选择类型" clearable @change="handleSearch" style="width: 120px">
                <el-option label="菜单" value="menu" />
                <el-option label="按钮" value="button" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="searchForm.status" placeholder="请选择状态" clearable @change="handleSearch" style="width: 100px">
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="handleSearch" size="small">
                搜索
              </el-button>
              <el-button :icon="Refresh" @click="handleReset" size="small">
                重置
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 权限列表表格 -->
        <div class="table-container" v-if="selectedMenu">
          <el-table
            ref="tableRef"
            v-loading="tableLoading"
            :data="filteredChildPermissions"
            style="width: 100%"
            row-key="id"
            @selection-change="handleSelectionChange"
            :header-cell-style="{ background: '#f5f7fa', color: '#303133' }"
            empty-text="暂无子权限"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="permissionName" label="权限名称" min-width="180">
              <template #default="{ row }">
                <div class="permission-name">
                  <el-icon v-if="row.icon" class="permission-icon">
                    <component :is="getIconComponent(row.icon)" />
                  </el-icon>
                  <span>{{ row.permissionName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="permissionCode" label="权限编码" min-width="200" show-overflow-tooltip />
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
            <el-table-column prop="sortOrder" label="排序" width="80" align="center">
              <template #default="{ row }">
                <el-tag type="info" size="small">{{ row.sortOrder || 0 }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-switch
                  v-model="row.status"
                  :active-value="1"
                  :inactive-value="0"
                  @change="handleStatusChange(row)"
                  :disabled="tableLoading"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <div class="action-buttons">
                  <el-button
                    type="primary"
                    link
                    :icon="Plus"
                    @click="handleAddChildPermission(row)"
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

        <!-- 空状态 -->
        <div class="empty-state" v-else>
          <el-empty description="请选择左侧菜单查看对应的权限信息">
            <el-button type="primary" @click="handleAdd">新增一级菜单</el-button>
          </el-empty>
        </div>
      </div>
    </div>

    <!-- 权限编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :before-close="handleDialogClose"
      :close-on-click-modal="false"
      class="permission-dialog"
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
              <el-input v-model="formData.icon" placeholder="请输入图标名称，如：User">
                <template #prepend>
                  <el-icon v-if="formData.icon">
                    <component :is="getIconComponent(formData.icon)" />
                  </el-icon>
                  <el-icon v-else><Menu /></el-icon>
                </template>
              </el-input>
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
import { Plus, Search, Refresh, Edit, Delete, Menu, User, Setting, Avatar } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { permissionApi } from '../../../api/system'

// 响应式数据
const loading = ref(false)
const treeLoading = ref(false)
const tableLoading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const dialogTitle = ref('')
const selectedRows = ref<any[]>([])
const menuTreeRef = ref()
const tableRef = ref()

// 权限树数据
const permissionTreeData = ref<any[]>([])
const selectedMenu = ref<any>(null)
const childPermissions = ref<any[]>([])

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

// 树形组件配置
const treeProps = {
  children: 'children',
  label: 'permissionName'
}

// 计算属性
// 一级菜单列表
const firstLevelMenus = computed(() => {
  return permissionTreeData.value.filter(item => !item.parentId || item.parentId === null)
})

// 过滤后的子权限列表
const filteredChildPermissions = computed(() => {
  if (!selectedMenu.value || !childPermissions.value.length) {
    return []
  }
  
  let filtered = childPermissions.value
  
  if (searchForm.permissionName) {
    filtered = filtered.filter(item => 
      item.permissionName.includes(searchForm.permissionName)
    )
  }
  
  if (searchForm.permissionType) {
    filtered = filtered.filter(item => 
      item.permissionType === searchForm.permissionType
    )
  }
  
  if (searchForm.status !== '') {
    filtered = filtered.filter(item => 
      item.status === searchForm.status
    )
  }
  
  return filtered
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

// 图标组件映射
const iconComponents: Record<string, any> = {
  User,
  Setting,
  Avatar,
  Menu,
  Plus,
  Edit,
  Delete
}

// 获取图标组件
const getIconComponent = (iconName: string) => {
  return iconComponents[iconName] || Menu
}

// 通用错误处理函数
const handleApiError = (error: any, defaultMessage: string): string => {
  console.error('API错误:', error)
  
  if (error.response) {
    // 后端返回的错误响应
    const responseData = error.response.data
    if (responseData && responseData.message) {
      return responseData.message
    } else if (responseData && typeof responseData === 'string') {
      // 有些后端直接返回字符串错误信息
      return responseData
    } else if (error.response.status === 500) {
      return '服务器内部错误，请稍后重试'
    } else if (error.response.status === 404) {
      return '请求的资源不存在'
    } else if (error.response.status === 403) {
      return '没有权限访问该资源'
    } else if (error.response.status === 401) {
      return '登录已过期，请重新登录'
    } else if (error.response.status) {
      return `请求失败，状态码: ${error.response.status}`
    }
  } else if (error.message && !error.message.includes('Network Error')) {
    // 网络错误或其他错误，但排除通用的Network Error
    return error.message
  }
  
  return defaultMessage
}

// 方法实现
const handleSearch = () => {
  console.log('搜索子权限:', searchForm)
  // 通过计算属性自动过滤，无需额外操作
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
  if (selectedMenu.value) {
    loadChildPermissions()
  }
}

// 加载权限树数据
const loadPermissionTree = async () => {
  treeLoading.value = true
  
  try {
    console.log('加载权限树数据')
    const response = await permissionApi.getPermissionTree()
    console.log('权限树响应:', response)
    
    // 成功响应
    permissionTreeData.value = response.data || []
    console.log('权限树数据加载成功:', permissionTreeData.value)
    ElMessage.success(`权限树加载成功，共${countTreeNodes(permissionTreeData.value)}个权限`)
    
    // 如果没有选中的菜单且有数据，默认选中第一个
    if (!selectedMenu.value && firstLevelMenus.value.length > 0) {
      handleMenuClick(firstLevelMenus.value[0])
    }
  } catch (error: any) {
    const errorMessage = handleApiError(error, '获取权限树失败，请检查网络连接')
    ElMessage.error(errorMessage)
    permissionTreeData.value = getMockPermissionTree()
  } finally {
    treeLoading.value = false
  }
}

// 计算树节点总数
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

// 模拟权限树数据
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
          description: '用户管理页面'
        },
        {
          id: 12,
          permissionName: '角色管理',
          permissionCode: 'system:role',
          permissionType: 'menu',
          parentId: 1,
          menuPath: '/system/role',
          componentPath: 'system/role/index',
          icon: 'Avatar',
          sortOrder: 2,
          status: 1,
          description: '角色管理页面'
        }
      ]
    },
    {
      id: 2,
      permissionName: '内容管理',
      permissionCode: 'content',
      permissionType: 'menu',
      parentId: null,
      menuPath: '/content',
      componentPath: 'content/index',
      icon: 'Menu',
      sortOrder: 2,
      status: 1,
      description: '内容管理模块',
      children: []
    }
  ]
}

// 处理菜单点击
const handleMenuClick = (menu: any) => {
  selectedMenu.value = menu
  console.log('选中菜单:', menu)
  
  // 高亮当前选中的菜单
  if (menuTreeRef.value) {
    menuTreeRef.value.setCurrentKey(menu.id)
  }
  
  // 加载子权限
  loadChildPermissions()
}

// 加载子权限
const loadChildPermissions = async () => {
  if (!selectedMenu.value) return
  
  tableLoading.value = true
  
  try {
    // 从权限树中获取子权限
    const children = selectedMenu.value.children || []
    childPermissions.value = children
    console.log('子权限数据:', children)
    
    if (children.length > 0) {
      ElMessage.success(`加载成功，共${children.length}个子权限`)
    }
  } catch (error: any) {
    const errorMessage = handleApiError(error, '加载子权限失败')
    ElMessage.error(errorMessage)
  } finally {
    tableLoading.value = false
  }
}

// 新增一级菜单
const handleAdd = () => {
  dialogTitle.value = '新增一级菜单'
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

// 新增子菜单
const handleAddSubMenu = (menu: any) => {
  dialogTitle.value = `新增子菜单 - ${menu.permissionName}`
  isEdit.value = false
  Object.assign(formData, {
    id: null,
    permissionName: '',
    permissionCode: '',
    permissionType: 'menu',
    parentId: menu.id,
    menuPath: '',
    componentPath: '',
    icon: '',
    sortOrder: 0,
    description: '',
    status: 1
  })
  dialogVisible.value = true
}

// 新增子权限
const handleAddChildPermission = (parent?: any) => {
  const parentMenu = parent || selectedMenu.value
  dialogTitle.value = `新增子权限 - ${parentMenu.permissionName}`
  isEdit.value = false
  Object.assign(formData, {
    id: null,
    permissionName: '',
    permissionCode: '',
    permissionType: 'button',
    parentId: parentMenu.id,
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
  // 检查是否有子权限
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
      console.log('删除权限:', row.id)
      const response = await permissionApi.deletePermission(row.id)
      console.log('删除权限响应:', response)
      
      // 成功响应
      ElMessage.success('删除成功')
      loadPermissionTree()
      if (selectedMenu.value) {
        loadChildPermissions()
      }
        } catch (error: any) {
      const errorMessage = handleApiError(error, '删除失败，请检查网络连接')
      ElMessage.error(errorMessage)
    }
  }).catch(() => {
    // 取消删除
  })
}

const handleStatusChange = async (row: any) => {
  try {
    console.log('修改权限状态:', row.id, row.status)
    const response = await permissionApi.updatePermission(row.id, {
      id: row.id,
      status: row.status
    })
    
    // 成功响应
    ElMessage.success(row.status ? '启用成功' : '禁用成功')
  } catch (error: any) {
    // 恢复原状态
    row.status = row.status === 1 ? 0 : 1
    const errorMessage = handleApiError(error, '状态修改失败，请检查网络连接')
    ElMessage.error(errorMessage)
  }
}

const handleTypeChange = (type: string) => {
  // 根据类型重置相关字段
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
      
      console.log('提交权限数据:', submitData)
      
      let response
      if (isEdit.value && formData.id) {
        response = await permissionApi.updatePermission(formData.id, submitData)
      } else {
        response = await permissionApi.createPermission(submitData)
      }
      
      console.log('权限操作响应:', response)
      
      // 成功响应
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadPermissionTree()
      if (selectedMenu.value) {
        loadChildPermissions()
      }
    } catch (error: any) {
      const errorMessage = handleApiError(error, '操作失败，请检查网络连接')
      ElMessage.error(errorMessage)
    } finally {
      submitLoading.value = false
    }
  }
}

const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection
}

// 组件挂载时加载数据
onMounted(() => {
  loadPermissionTree()
})
</script>

<style scoped lang="scss">
.permission-management {
  width: 100%;
  height: 100%;
  min-height: 500px;
  background: #f5f7fa;
  padding: 20px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    background: white;
    padding: 20px;
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

  .main-content {
    display: flex;
    gap: 20px;
    height: calc(100vh - 200px);
    min-height: 600px;
  }

  .left-panel {
    flex: 0 0 320px;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    padding: 20px;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    
    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      padding-bottom: 16px;
      border-bottom: 1px solid #ebeef5;
      
      h3 {
        margin: 0;
        color: #303133;
        font-size: 18px;
        font-weight: 600;
      }
    }
    
    .menu-tree-container {
      flex: 1;
      overflow-y: auto;
      
      .menu-tree {
        .tree-node {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 8px 0;
          border-radius: 4px;
          transition: all 0.3s ease;
          
          &:hover {
            background-color: #f5f7fa;
            
            .node-actions {
              opacity: 1;
            }
          }
          
          .node-content {
            display: flex;
            align-items: center;
            flex: 1;
            
            .node-icon {
              margin-right: 8px;
              color: #409eff;
              font-size: 16px;
            }
            
            .node-label {
              color: #303133;
              font-weight: 500;
              font-size: 14px;
              margin-right: 8px;
            }
            
            .child-count {
              margin-left: auto;
            }
          }
          
          .node-actions {
            display: flex;
            gap: 4px;
            opacity: 0;
            transition: opacity 0.3s ease;
            
            .el-button {
              padding: 4px;
              min-height: auto;
              
              &:hover {
                background-color: #e6f7ff;
              }
            }
          }
        }
      }
    }
  }

  .right-panel {
    flex: 1;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    padding: 20px;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    
    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      padding-bottom: 16px;
      border-bottom: 1px solid #ebeef5;
      
      h3 {
        margin: 0;
        color: #303133;
        font-size: 18px;
        font-weight: 600;
      }
      
      .header-actions {
        display: flex;
        gap: 12px;
      }
    }

    .search-bar {
      margin-bottom: 20px;
      
      .search-form {
        margin: 0;
        
        .el-form-item {
          margin-bottom: 0;
          margin-right: 16px;
        }
      }
    }

    .table-container {
      flex: 1;
      overflow: hidden;
      
      .el-table {
        height: 100%;
        border: none;
        
        .permission-name {
          display: flex;
          align-items: center;
          
          .permission-icon {
            margin-right: 8px;
            color: #409eff;
            font-size: 16px;
          }
        }
        
        .text-gray {
          color: #c0c4cc;
          font-style: italic;
        }
        
        .path-text {
          font-family: monospace;
          background: #f5f7fa;
          padding: 2px 6px;
          border-radius: 3px;
          font-size: 12px;
        }
        
        .action-buttons {
          display: flex;
          gap: 8px;
          flex-wrap: wrap;
        }
      }
    }

    .empty-state {
      flex: 1;
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 300px;
    }
  }
}

.permission-dialog {
  .el-dialog__header {
    padding: 20px 20px 10px 20px;
    border-bottom: 1px solid #ebeef5;
    
    .el-dialog__title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }
  }
  
  .el-dialog__body {
    padding: 20px;
  }
  
  .el-dialog__footer {
    padding: 10px 20px 20px 20px;
    border-top: 1px solid #ebeef5;
  }
}

.permission-form {
  .el-form-item {
    margin-bottom: 20px;
    
    .el-form-item__label {
      font-weight: 500;
      color: #606266;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
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
}

:deep(.el-tree) {
  .el-tree-node__content {
    height: auto;
    padding: 8px 0;
    
    &:hover {
      background-color: transparent;
    }
  }
  
  .el-tree-node.is-current > .el-tree-node__content {
    background-color: #e6f7ff;
    color: #409eff;
    
    .tree-node .node-label {
      color: #409eff;
      font-weight: 600;
    }
  }
  
  .el-tree-node__expand-icon {
    color: #c0c4cc;
    
    &.is-leaf {
      color: transparent;
    }
  }
}

:deep(.el-tree-select) {
  .el-select__wrapper {
    &.is-focused {
      box-shadow: 0 0 0 1px #409eff inset;
    }
  }
}

:deep(.el-empty) {
  .el-empty__image {
    width: 120px;
  }
  
  .el-empty__description {
    margin-top: 16px;
    color: #909399;
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .permission-management {
    .main-content {
      flex-direction: column;
      height: auto;
      
      .left-panel {
        flex: none;
        max-height: 400px;
      }
      
      .right-panel {
        flex: none;
        min-height: 500px;
      }
    }
  }
}

@media (max-width: 768px) {
  .permission-management {
    padding: 10px;
    
    .page-header {
      flex-direction: column;
      gap: 16px;
      
      .header-actions {
        flex-wrap: wrap;
        justify-content: center;
      }
    }
    
    .main-content {
      gap: 10px;
      
      .left-panel,
      .right-panel {
        padding: 15px;
      }
    }
    
    .search-bar {
      .search-form {
        .el-form-item {
          margin-bottom: 16px;
          margin-right: 0;
          width: 100%;
        }
      }
    }
    
    .table-container {
      .el-table {
        .action-buttons {
          flex-direction: column;
          gap: 4px;
        }
      }
    }
  }
  
  .permission-dialog {
    :deep(.el-dialog) {
      width: 95% !important;
      margin: 5vh auto !important;
    }
  }
}

// 动画效果
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// 状态指示器
.status-indicator {
  &.active {
    color: #67c23a;
  }
  
  &.inactive {
    color: #f56c6c;
  }
}

// 权限类型标签样式
.permission-type-tag {
  &.menu {
    background-color: #e6f7ff;
    color: #1890ff;
    border-color: #91d5ff;
  }
  
  &.button {
    background-color: #f6ffed;
    color: #52c41a;
    border-color: #b7eb8f;
  }
}
</style> 