<template>
  <div class="layout-test">
    <h1>权限管理布局测试</h1>
    
    <!-- 模拟权限管理页面的主要布局 -->
    <div class="main-content">
      <!-- 左侧：一级菜单树 -->
      <div class="left-panel">
        <div class="panel-header">
          <h3>一级菜单</h3>
          <el-button link size="small">新增</el-button>
        </div>
        
        <div class="menu-list">
          <div class="menu-item active">
            <div class="menu-content">
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
              <el-tag type="info" size="small">2</el-tag>
            </div>
            <div class="menu-actions">
              <el-button link size="small">+</el-button>
              <el-button link size="small">编辑</el-button>
              <el-button link size="small">删除</el-button>
            </div>
          </div>
          
          <div class="menu-item">
            <div class="menu-content">
              <el-icon><Menu /></el-icon>
              <span>内容管理</span>
              <el-tag type="info" size="small">0</el-tag>
            </div>
            <div class="menu-actions">
              <el-button link size="small">+</el-button>
              <el-button link size="small">编辑</el-button>
              <el-button link size="small">删除</el-button>
            </div>
          </div>
          
          <div class="menu-item">
            <div class="menu-content">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
              <el-tag type="info" size="small">3</el-tag>
            </div>
            <div class="menu-actions">
              <el-button link size="small">+</el-button>
              <el-button link size="small">编辑</el-button>
              <el-button link size="small">删除</el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：二级权限列表 -->
      <div class="right-panel">
        <div class="panel-header">
          <h3>系统管理 - 子权限</h3>
          <div class="header-actions">
            <el-button type="primary" size="small">新增子权限</el-button>
            <el-button type="info" size="small">刷新</el-button>
          </div>
        </div>

        <!-- 搜索栏 -->
        <div class="search-bar">
          <el-form inline>
            <el-form-item label="权限名称">
              <el-input placeholder="请输入权限名称" style="width: 200px" />
            </el-form-item>
            <el-form-item label="权限类型">
              <el-select placeholder="请选择类型" style="width: 120px">
                <el-option label="菜单" value="menu" />
                <el-option label="按钮" value="button" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" size="small">搜索</el-button>
              <el-button size="small">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 权限列表 -->
        <div class="table-container">
          <el-table :data="tableData" style="width: 100%">
            <el-table-column type="selection" width="55" />
            <el-table-column prop="name" label="权限名称" min-width="180">
              <template #default="{ row }">
                <div class="permission-name">
                  <el-icon><component :is="row.icon" /></el-icon>
                  <span>{{ row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="code" label="权限编码" min-width="200" />
            <el-table-column prop="type" label="类型" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.type === 'menu' ? 'primary' : 'success'" size="small">
                  {{ row.type === 'menu' ? '菜单' : '按钮' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="path" label="路径/URL" min-width="180" />
            <el-table-column prop="sort" label="排序" width="80" align="center">
              <template #default="{ row }">
                <el-tag type="info" size="small">{{ row.sort }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-switch v-model="row.status" :active-value="1" :inactive-value="0" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default>
                <div class="action-buttons">
                  <el-button type="primary" link size="small">添加子权限</el-button>
                  <el-button type="warning" link size="small">编辑</el-button>
                  <el-button type="danger" link size="small">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Setting, Menu, User } from '@element-plus/icons-vue'

const tableData = ref([
  {
    id: 1,
    name: '用户管理',
    code: 'system:user',
    type: 'menu',
    path: '/system/user',
    sort: 1,
    status: 1,
    icon: 'User'
  },
  {
    id: 2,
    name: '角色管理',
    code: 'system:role',
    type: 'menu',
    path: '/system/role',
    sort: 2,
    status: 1,
    icon: 'Avatar'
  },
  {
    id: 3,
    name: '新增用户',
    code: 'system:user:add',
    type: 'button',
    path: '',
    sort: 1,
    status: 1,
    icon: 'Plus'
  },
  {
    id: 4,
    name: '编辑用户',
    code: 'system:user:edit',
    type: 'button',
    path: '',
    sort: 2,
    status: 1,
    icon: 'Edit'
  },
  {
    id: 5,
    name: '删除用户',
    code: 'system:user:delete',
    type: 'button',
    path: '',
    sort: 3,
    status: 0,
    icon: 'Delete'
  }
])
</script>

<style scoped lang="scss">
.layout-test {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;

  h1 {
    text-align: center;
    margin-bottom: 30px;
    color: #303133;
  }

  .main-content {
    display: flex;
    gap: 20px;
    height: calc(100vh - 120px);
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
    
    .menu-list {
      flex: 1;
      overflow-y: auto;
      
      .menu-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 8px;
        border-radius: 4px;
        margin-bottom: 8px;
        cursor: pointer;
        transition: all 0.3s ease;
        
        &:hover {
          background-color: #f5f7fa;
          
          .menu-actions {
            opacity: 1;
          }
        }
        
        &.active {
          background-color: #e6f7ff;
          color: #409eff;
          
          .menu-content span {
            color: #409eff;
            font-weight: 600;
          }
        }
        
        .menu-content {
          display: flex;
          align-items: center;
          flex: 1;
          
          .el-icon {
            margin-right: 8px;
            font-size: 16px;
          }
          
          span {
            margin-right: 8px;
            font-size: 14px;
          }
          
          .el-tag {
            margin-left: auto;
          }
        }
        
        .menu-actions {
          display: flex;
          gap: 4px;
          opacity: 0;
          transition: opacity 0.3s ease;
          
          .el-button {
            padding: 4px 8px;
            min-height: auto;
            
            &:hover {
              background-color: #e6f7ff;
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
      
      .el-form {
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
        
        .permission-name {
          display: flex;
          align-items: center;
          
          .el-icon {
            margin-right: 8px;
            color: #409eff;
            font-size: 16px;
          }
        }
        
        .action-buttons {
          display: flex;
          gap: 8px;
          flex-wrap: wrap;
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .layout-test {
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
  .layout-test {
    padding: 10px;
    
    .main-content {
      gap: 10px;
      
      .left-panel,
      .right-panel {
        padding: 15px;
      }
    }
  }
}
</style> 