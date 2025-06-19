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
        <el-table-column prop="id" label="角色ID" width="100" />
        <el-table-column prop="roleName" label="角色名称" min-width="150" />
        <el-table-column prop="roleCode" label="角色编码" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="200">
          <template #default="{ row }">
            <div class="description-content">
              <div class="description-text">{{ row.description }}</div>
              <div class="permission-count" v-if="row.permissionCount !== undefined">
                <el-tag size="small" type="info">
                  {{ row.permissionCount || 0 }}个权限
                </el-tag>
              </div>
            </div>
          </template>
        </el-table-column>
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
      width="900px"
      :before-close="handlePermissionDialogClose"
    >
      <div class="permission-header">
        <div class="role-info">
          <span class="role-name">{{ currentRoleName }}</span>
          <span class="permission-summary">
            已选择 <el-tag type="primary" size="small">{{ selectedPermissionCount }}</el-tag> 个权限
          </span>
        </div>
        <div class="permission-actions">
          <el-button size="small" @click="expandAllNodes">展开全部</el-button>
          <el-button size="small" @click="collapseAllNodes">收起全部</el-button>
          <el-button size="small" @click="checkAllNodes">全选</el-button>
          <el-button size="small" @click="uncheckAllNodes">取消全选</el-button>
        </div>
      </div>
      
      <div class="permission-tree">
        <el-tree
          ref="permissionTreeRef"
          :data="permissionTreeData"
          show-checkbox
          node-key="id"
          check-strictly
          :default-expanded-keys="defaultExpandedKeys"
          :default-checked-keys="checkedPermissions"
          :props="treeProps"
          @check="handlePermissionCheck"
        >
          <template #default="{ data }">
            <div class="tree-node-content">
              <div class="node-info">
                <span class="node-label">{{ data.permissionName || data.label }}</span>
                <el-tag 
                  v-if="data.permissionType" 
                  :type="getPermissionTypeColor(data.permissionType)" 
                  size="small"
                  class="permission-type-tag"
                >
                  {{ getPermissionTypeText(data.permissionType) }}
                </el-tag>
              </div>
              <div class="node-meta" v-if="data.permissionCode">
                <span class="permission-code">{{ data.permissionCode }}</span>
                <span class="permission-path" v-if="data.path">{{ data.path }}</span>
              </div>
            </div>
          </template>
        </el-tree>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handlePermissionDialogClose">取消</el-button>
          <el-button type="primary" @click="handlePermissionSubmit" :loading="permissionLoading">
            保存配置
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
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
const permissionTreeData = ref<any[]>([])
const checkedPermissions = ref<any[]>([])
const defaultExpandedKeys = ref<any[]>([])
const currentRoleId = ref<number | null>(null)
const currentRoleName = ref('')
const selectedPermissionCount = ref(0)
const treeProps = {
  children: 'children',
  label: 'permissionName'
}

// 树组件就绪检测函数
const waitForTreeReady = (methodName: string, maxRetries = 20, delay = 100): Promise<boolean> => {
  return new Promise((resolve) => {
    let retries = 0
    
    const checkReady = () => {
      console.log(`[${retries + 1}/${maxRetries}] 检查树组件方法: ${methodName}`)
      
      try {
        const hasRef = !!permissionTreeRef.value
        const hasMethod = hasRef && methodName in permissionTreeRef.value
        const isFunction = hasMethod && typeof permissionTreeRef.value[methodName] === 'function'
        const hasData = permissionTreeData.value && permissionTreeData.value.length > 0
        const isTreeMounted = hasRef && permissionTreeRef.value.$el
        
        // 输出树组件的所有可用方法（只在第一次检查时）
        if (hasRef && retries === 0) {
          const availableMethods = Object.getOwnPropertyNames(permissionTreeRef.value)
            .filter(prop => typeof permissionTreeRef.value[prop] === 'function')
          console.log('树组件可用方法:', availableMethods)
          
          // 特别检查展开相关的方法
          const expandMethods = availableMethods.filter(method => 
            method.toLowerCase().includes('expand') || 
            method.toLowerCase().includes('node') ||
            method.toLowerCase().includes('key')
          )
          console.log('节点操作相关方法:', expandMethods)
        }
        
        console.log('检查详情:', {
          hasRef,
          hasMethod,
          isFunction,
          hasData,
          isTreeMounted,
          refType: hasRef ? typeof permissionTreeRef.value : 'undefined'
        })
        
        if (hasRef && isFunction && hasData && isTreeMounted) {
          console.log(`✓ 树组件方法 ${methodName} 检查通过`)
          resolve(true)
          return
        }
      } catch (error) {
        console.warn('检查过程中出现错误:', error)
      }
      
      if (retries < maxRetries) {
        retries++
        setTimeout(checkReady, delay)
      } else {
        console.warn(`✗ 树组件方法 ${methodName} 检查超时`)
        resolve(false)
      }
    }
    
    // 立即开始第一次检查
    checkReady()
  })
}

// 计算权限数量
const calculatePermissionCount = (permissions: any[]): number => {
  if (!permissions || !Array.isArray(permissions)) return 0
  
  let count = 0
  permissions.forEach(permission => {
    count++
    if (permission.children && permission.children.length > 0) {
      count += calculatePermissionCount(permission.children)
    }
  })
  return count
}

// 获取权限类型颜色
const getPermissionTypeColor = (type: string) => {
  switch (type) {
    case 'menu':
      return 'primary'
    case 'button':
      return 'success'
    case 'api':
      return 'warning'
    default:
      return 'info'
  }
}

// 获取权限类型文本
const getPermissionTypeText = (type: string) => {
  switch (type) {
    case 'menu':
      return '菜单'
    case 'button':
      return '按钮'
    case 'api':
      return '接口'
    default:
      return '其他'
  }
}

// 使用实际可用的方法来操作树
const expandAllNodes = async () => {
  if (!permissionTreeData.value.length) {
    ElMessage.warning('暂无权限数据')
    return
  }
  
  console.log('开始展开所有节点操作')
  
  try {
    if (permissionTreeRef.value) {
      const treeInstance = permissionTreeRef.value
      console.log('使用可用方法展开所有节点')
      
      // 获取所有节点
      const allKeys = getAllNodeKeys(permissionTreeData.value)
      console.log('所有节点键值:', allKeys)
      
      // 逐个展开节点
      let expandedCount = 0
      allKeys.forEach(key => {
        try {
          // 获取节点对象
          const node = treeInstance.getNode ? treeInstance.getNode(key) : null
          if (node) {
            // 展开节点
            if (node.expand) {
              node.expand()
              expandedCount++
            } else if (treeInstance.handleNodeExpand) {
              treeInstance.handleNodeExpand(node.data, node, node)
              expandedCount++
            }
          }
        } catch (nodeError) {
          console.warn(`展开节点 ${key} 失败:`, nodeError)
        }
      })
      
      if (expandedCount > 0) {
        ElMessage.success(`已展开 ${expandedCount} 个节点`)
      } else {
        // 使用强制方法
        console.log('标准方法无效，使用强制展开')
        forceExpandTree()
      }
    } else {
      ElMessage.error('权限树组件不可用')
    }
  } catch (error) {
    console.error('展开操作失败:', error)
    // 使用强制方法作为最后备选
    console.log('使用强制展开方法')
    forceExpandTree()
  }
}

const collapseAllNodes = async () => {
  console.log('开始收起所有节点操作')
  
  try {
    if (permissionTreeRef.value) {
      const treeInstance = permissionTreeRef.value
      console.log('使用可用方法收起所有节点')
      
      // 获取所有节点
      const allKeys = getAllNodeKeys(permissionTreeData.value)
      console.log('所有节点键值:', allKeys)
      
      // 逐个收起节点
      let collapsedCount = 0
      allKeys.forEach(key => {
        try {
          // 获取节点对象
          const node = treeInstance.getNode ? treeInstance.getNode(key) : null
          if (node) {
            // 收起节点
            if (node.collapse) {
              node.collapse()
              collapsedCount++
            } else if (node.expanded !== undefined) {
              node.expanded = false
              collapsedCount++
            }
          }
        } catch (nodeError) {
          console.warn(`收起节点 ${key} 失败:`, nodeError)
        }
      })
      
      if (collapsedCount > 0) {
        ElMessage.success(`已收起 ${collapsedCount} 个节点`)
      } else {
        // 使用强制方法
        console.log('标准方法无效，使用强制收起')
        forceCollapseTree()
      }
    } else {
      ElMessage.error('权限树组件不可用')
    }
  } catch (error) {
    console.error('收起操作失败:', error)
    // 使用强制方法作为最后备选
    console.log('使用强制收起方法')
    forceCollapseTree()
  }
}

const checkAllNodes = async () => {
  if (!permissionTreeData.value.length) {
    ElMessage.warning('暂无权限数据')
    return
  }
  
  const isReady = await waitForTreeReady('setCheckedKeys')
  if (isReady) {
    try {
      const allKeys = getAllNodeKeys(permissionTreeData.value)
      console.log('选中所有节点:', allKeys)
      permissionTreeRef.value.setCheckedKeys(allKeys)
      selectedPermissionCount.value = allKeys.length
      ElMessage.success(`已选择所有权限 (${allKeys.length}个)`)
    } catch (error) {
      console.warn('选中所有节点失败:', error)
      ElMessage.error('全选失败，请稍后重试')
    }
  } else {
    ElMessage.error('权限树组件未就绪，请稍后重试')
  }
}

const uncheckAllNodes = async () => {
  const isReady = await waitForTreeReady('setCheckedKeys')
  if (isReady) {
    try {
      console.log('取消选中所有节点')
      permissionTreeRef.value.setCheckedKeys([])
      selectedPermissionCount.value = 0
      ElMessage.success('已取消所有选择')
    } catch (error) {
      console.warn('取消选中所有节点失败:', error)
      ElMessage.error('取消选择失败，请稍后重试')
    }
  } else {
    ElMessage.error('权限树组件未就绪，请稍后重试')
  }
}

const getAllNodeKeys = (nodes: any[]): any[] => {
  let keys: any[] = []
  if (!nodes || !Array.isArray(nodes)) {
    return keys
  }
  
  nodes.forEach(node => {
    if (node.id !== undefined && node.id !== null) {
      keys.push(node.id)
    }
    if (node.children && Array.isArray(node.children) && node.children.length > 0) {
      keys = keys.concat(getAllNodeKeys(node.children))
    }
  })
  return keys
}

const handlePermissionCheck = () => {
  // 使用可选链操作符，更安全地获取选中的节点
  try {
    const checkedKeys = permissionTreeRef.value?.getCheckedKeys?.() || []
    selectedPermissionCount.value = checkedKeys.length
  } catch (error) {
    console.warn('获取选中节点失败:', error)
    selectedPermissionCount.value = 0
  }
}

const handlePermissionDialogClose = () => {
  permissionDialogVisible.value = false
  currentRoleId.value = null
  currentRoleName.value = ''
  selectedPermissionCount.value = 0
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
    
    if (response.code === 200) {
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
      
      // 为每个角色计算权限数量
      for (const role of dataList) {
        if (role.permissions && Array.isArray(role.permissions)) {
          role.permissionCount = calculatePermissionCount(role.permissions)
        } else {
          // 如果没有权限数据，尝试从后端获取
          try {
            const permResponse = await roleApi.getRolePermissions(role.id)
            if (permResponse.code === 200 && permResponse.data) {
              role.permissionCount = Array.isArray(permResponse.data) ? permResponse.data.length : 0
            } else {
              role.permissionCount = 0
            }
          } catch (error) {
            console.warn('获取角色权限数量失败:', error)
            role.permissionCount = 0
          }
        }
      }
      
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
  currentRoleName.value = row.roleName
  selectedPermissionCount.value = 0
  
  try {
    // 加载权限树数据
    console.log('加载权限树数据')
    let treeData = []
    
    try {
      const treeResponse = await permissionApi.getPermissionTree()
      console.log('权限树API响应:', treeResponse)
      
      if (treeResponse.code === 200 && treeResponse.data) {
        treeData = treeResponse.data
      } else {
        console.warn('权限树API返回异常，使用模拟数据')
        treeData = getDefaultPermissionTree()
      }
    } catch (apiError) {
      console.warn('权限树API调用失败，使用模拟数据:', apiError)
      treeData = getDefaultPermissionTree()
    }
    
    permissionTreeData.value = treeData
    console.log('权限树数据:', permissionTreeData.value)
    
    // 加载角色已有权限
    console.log('加载角色权限:', row.id)
    let permissionIds: any[] = []
    
    try {
      const permResponse = await roleApi.getRolePermissions(row.id)
      console.log('角色权限API响应:', permResponse)
      
      if (permResponse.code === 200 && permResponse.data) {
        const permissions = permResponse.data || []
        permissionIds = Array.isArray(permissions) ? permissions.map((p: any) => p.id).filter(id => id !== undefined) : []
      } else {
        console.warn('角色权限API返回异常，使用默认权限')
        permissionIds = getDefaultRolePermissions(row.roleName)
      }
    } catch (apiError) {
      console.warn('角色权限API调用失败，使用默认权限:', apiError)
      permissionIds = getDefaultRolePermissions(row.roleName)
    }
    
    checkedPermissions.value = permissionIds
    selectedPermissionCount.value = permissionIds.length
    
    // 设置默认展开节点
    if (permissionTreeData.value.length > 0) {
      const topLevelKeys = permissionTreeData.value.map((item: any) => item.id).filter(id => id !== undefined)
      defaultExpandedKeys.value = topLevelKeys
      console.log('设置默认展开节点:', topLevelKeys)
    }
    
    // 先显示对话框
    permissionDialogVisible.value = true
    
    // 等待多个 tick 确保对话框和树组件完全渲染
    await nextTick()
    await nextTick()
    
    // 使用 setTimeout 确保树组件完全初始化
    setTimeout(() => {
      if (permissionTreeRef.value && 
          typeof permissionTreeRef.value.setExpandedKeys === 'function' &&
          typeof permissionTreeRef.value.setCheckedKeys === 'function' &&
          typeof permissionTreeRef.value.getCheckedKeys === 'function') {
        try {
          // 设置展开状态
          if (defaultExpandedKeys.value.length > 0) {
            console.log('设置展开状态:', defaultExpandedKeys.value)
            permissionTreeRef.value.setExpandedKeys(defaultExpandedKeys.value)
          }
          
          // 设置选中状态
          if (permissionIds.length > 0) {
            console.log('设置选中权限:', permissionIds)
            permissionTreeRef.value.setCheckedKeys(permissionIds)
          }
          
          console.log('权限树组件已就绪')
        } catch (error) {
          console.warn('设置树状态失败:', error)
        }
      } else {
        console.warn('权限树组件未就绪或方法不存在')
      }
    }, 100)
    
    ElMessage.success('权限数据加载成功')
    
  } catch (error) {
    console.error('加载权限数据失败:', error)
    ElMessage.error('加载权限数据失败，请检查网络连接')
    
    // 即使出错也要显示基本的权限树
    permissionTreeData.value = getDefaultPermissionTree()
    checkedPermissions.value = []
    selectedPermissionCount.value = 0
    permissionDialogVisible.value = true
  }
}

// 获取默认权限树数据
const getDefaultPermissionTree = () => {
  return [
    {
      id: 1,
      permissionName: '系统管理',
      permissionCode: 'SYSTEM',
      permissionType: 'menu',
      path: '/system',
      children: [
        {
          id: 11,
          permissionName: '用户管理',
          permissionCode: 'SYSTEM_USER',
          permissionType: 'menu',
          path: '/system/user'
        },
        {
          id: 12,
          permissionName: '角色管理',
          permissionCode: 'SYSTEM_ROLE',
          permissionType: 'menu',
          path: '/system/role'
        },
        {
          id: 13,
          permissionName: '权限管理',
          permissionCode: 'SYSTEM_PERMISSION',
          permissionType: 'menu',
          path: '/system/permission'
        },
        {
          id: 14,
          permissionName: '操作日志',
          permissionCode: 'SYSTEM_LOG',
          permissionType: 'menu',
          path: '/system/log'
        }
      ]
    },
    {
      id: 2,
      permissionName: '内容管理',
      permissionCode: 'CONTENT',
      permissionType: 'menu',
      path: '/content',
      children: [
        {
          id: 21,
          permissionName: '文章管理',
          permissionCode: 'CONTENT_ARTICLE',
          permissionType: 'menu',
          path: '/content/article'
        },
        {
          id: 22,
          permissionName: '分类管理',
          permissionCode: 'CONTENT_CATEGORY',
          permissionType: 'menu',
          path: '/content/category'
        },
        {
          id: 23,
          permissionName: '标签管理',
          permissionCode: 'CONTENT_TAG',
          permissionType: 'menu',
          path: '/content/tag'
        }
      ]
    },
    {
      id: 3,
      permissionName: '用户中心',
      permissionCode: 'USER_CENTER',
      permissionType: 'menu',
      path: '/user',
      children: [
        {
          id: 31,
          permissionName: '用户列表',
          permissionCode: 'USER_LIST',
          permissionType: 'menu',
          path: '/user/list'
        },
        {
          id: 32,
          permissionName: '用户分析',
          permissionCode: 'USER_ANALYSIS',
          permissionType: 'menu',
          path: '/user/analysis'
        }
      ]
    }
  ]
}

// 根据角色名称获取默认权限
const getDefaultRolePermissions = (roleName: string) => {
  switch (roleName) {
    case '超级管理员':
      return [1, 11, 12, 13, 14, 2, 21, 22, 23, 3, 31, 32] // 所有权限
    case '系统管理员':
      return [1, 11, 12, 13, 14] // 系统管理权限
    case '内容管理员':
      return [2, 21, 22, 23] // 内容管理权限
    case '普通用户':
      return [3, 31] // 基础用户权限
    default:
      return []
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
  
  if (!permissionTreeRef.value || 
      typeof permissionTreeRef.value.getCheckedNodes !== 'function' || 
      typeof permissionTreeRef.value.getCheckedKeys !== 'function') {
    ElMessage.error('权限树组件未就绪，请稍后重试')
    return
  }
  
  let checkedNodes, checkedKeys
  try {
    checkedNodes = permissionTreeRef.value.getCheckedNodes()
    checkedKeys = permissionTreeRef.value.getCheckedKeys()
  } catch (error) {
    console.error('获取权限树数据失败:', error)
    ElMessage.error('获取权限数据失败，请稍后重试')
    return
  }
  
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

// 强制树操作方法（保证成功的备选方案）
const forceCollapseTree = () => {
  console.log('强制收起树 - 重置默认展开状态')
  try {
    // 清空默认展开的键值
    defaultExpandedKeys.value = []
    
    // 保存当前选中状态
    const currentChecked = permissionTreeRef.value?.getCheckedKeys?.() || []
    console.log('保存的选中状态:', currentChecked)
    
    // 重新渲染树以应用新的展开状态
    const currentData = [...permissionTreeData.value]
    permissionTreeData.value = []
    
    nextTick(() => {
      permissionTreeData.value = currentData
      
      // 恢复选中状态
      setTimeout(() => {
        if (currentChecked.length > 0 && permissionTreeRef.value?.setCheckedKeys) {
          try {
            permissionTreeRef.value.setCheckedKeys(currentChecked)
            selectedPermissionCount.value = currentChecked.length
          } catch (e) {
            console.warn('恢复选中状态失败:', e)
          }
        }
      }, 200)
    })
    
    ElMessage.success('已收起所有节点')
  } catch (error) {
    console.error('强制收起失败:', error)
    ElMessage.error('收起操作失败')
  }
}

const forceExpandTree = () => {
  console.log('强制展开树 - 设置默认展开状态')
  try {
    // 获取所有节点键值并设置为默认展开
    const allKeys = getAllNodeKeys(permissionTreeData.value)
    console.log('设置展开的节点数:', allKeys.length)
    defaultExpandedKeys.value = allKeys
    
    // 保存当前选中状态
    const currentChecked = permissionTreeRef.value?.getCheckedKeys?.() || []
    console.log('保存的选中状态:', currentChecked)
    
    // 重新渲染树以应用新的展开状态
    const currentData = [...permissionTreeData.value]
    permissionTreeData.value = []
    
    nextTick(() => {
      permissionTreeData.value = currentData
      
      // 恢复选中状态
      setTimeout(() => {
        if (currentChecked.length > 0 && permissionTreeRef.value?.setCheckedKeys) {
          try {
            permissionTreeRef.value.setCheckedKeys(currentChecked)
            selectedPermissionCount.value = currentChecked.length
          } catch (e) {
            console.warn('恢复选中状态失败:', e)
          }
        }
      }, 200)
    })
    
    ElMessage.success(`已展开所有节点 (${allKeys.length}个)`)
  } catch (error) {
    console.error('强制展开失败:', error)
    ElMessage.error('展开操作失败')
  }
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
    max-height: 450px;
    overflow-y: auto;
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    padding: 20px;
    background: #fafafa;
  }
  
  .permission-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 16px;
    background: #f5f7fa;
    border-radius: var(--radius-md);
    border: 1px solid #e4e7ed;
    
    .role-info {
      display: flex;
      align-items: center;
      gap: 12px;
      flex: 1;
      
      .role-name {
        font-weight: 600;
        color: #303133;
        font-size: 16px;
      }
      
      .permission-summary {
        color: #606266;
        font-size: 14px;
      }
    }
    
    .permission-actions {
      display: flex;
      gap: 8px;
      flex-shrink: 0;
    }
  }
  
  .tree-node-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 4px 0;
    min-height: 40px;
    
    .node-info {
      display: flex;
      align-items: center;
      gap: 8px;
      flex-wrap: wrap;
      
      .node-label {
        font-weight: 500;
        color: #303133;
        font-size: 14px;
        line-height: 1.4;
      }
      
      .permission-type-tag {
        font-size: 12px;
        flex-shrink: 0;
      }
    }
    
    .node-meta {
      display: flex;
      align-items: center;
      gap: 12px;
      font-size: 12px;
      color: #909399;
      flex-wrap: wrap;
      margin-top: 2px;
      
      .permission-code {
        background: #e4e7ed;
        color: #606266;
        padding: 2px 6px;
        border-radius: 4px;
        font-family: 'Consolas', 'Monaco', monospace;
        font-size: 11px;
        flex-shrink: 0;
      }
      
      .permission-path {
        font-style: italic;
        color: #909399;
        font-size: 11px;
        word-break: break-all;
      }
    }
  }
  
  .description-content {
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .description-text {
      color: #303133;
      line-height: 1.4;
      word-break: break-word;
    }
    
    .permission-count {
      display: flex;
      align-items: center;
    }
  }
}

// 对话框样式调整
:deep(.el-dialog) {
  .el-dialog__header {
    padding: 20px 20px 10px 20px;
    border-bottom: 1px solid #e4e7ed;
    
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
    border-top: 1px solid #e4e7ed;
  }
}

// 树组件样式优化
:deep(.el-tree) {
  .el-tree-node {
    .el-tree-node__content {
      height: auto;
      min-height: 32px;
      padding: 8px 0;
      
      &:hover {
        background-color: #f0f9ff;
      }
    }
    
    .el-tree-node__expand-icon {
      padding: 6px;
      font-size: 12px;
    }
    
    .el-checkbox {
      margin-right: 8px;
      
      .el-checkbox__inner {
        width: 16px;
        height: 16px;
        
        &::after {
          width: 4px;
          height: 8px;
          left: 5px;
          top: 1px;
        }
      }
    }
  }
  
  .el-tree-node__children {
    .el-tree-node__content {
      padding-left: 24px;
    }
  }
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}
</style>