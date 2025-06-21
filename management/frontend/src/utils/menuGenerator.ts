import type { MenuItem } from '@/types'
import i18n from '@/i18n'

// 权限与菜单的映射配置
interface PermissionMenuConfig {
  permissionCode: string
  menuItem: Omit<MenuItem, 'title'> & { titleKey: string }
}

// 菜单配置映射表
const MENU_CONFIG_MAP: PermissionMenuConfig[] = [
  {
    permissionCode: 'dashboard:view',
    menuItem: {
      id: 1,
      titleKey: 'menu.dashboard',
      path: '/dashboard',
      icon: 'DataBoard',
      meta: {
        title: '仪表盘',
        icon: 'DataBoard',
        requireAuth: true,
        permissions: ['dashboard:view']
      }
    }
  },
  {
    permissionCode: 'system:admin:view',
    menuItem: {
      id: 21,
      titleKey: 'menu.admin',
      path: '/system/admin',
      icon: 'UserFilled',
      meta: {
        title: '管理员管理',
        icon: 'UserFilled',
        requireAuth: true,
        permissions: ['system:admin:view']
      }
    }
  },
  {
    permissionCode: 'system:role:list',
    menuItem: {
      id: 22,
      titleKey: 'menu.role',
      path: '/system/role',
      icon: 'Avatar',
      meta: {
        title: '角色管理',
        icon: 'Avatar',
        requireAuth: true,
        permissions: ['system:role:list']
      }
    }
  },
  {
    permissionCode: 'system:permission:view',
    menuItem: {
      id: 23,
      titleKey: 'menu.permission',
      path: '/system/permission',
      icon: 'Lock',
      meta: {
        title: '权限管理',
        icon: 'Lock',
        requireAuth: true,
        permissions: ['system:permission:view']
      }
    }
  },
  {
    permissionCode: 'system:log:view',
    menuItem: {
      id: 24,
      titleKey: 'menu.log',
      path: '/system/log',
      icon: 'Document',
      meta: {
        title: '系统日志',
        icon: 'Document',
        requireAuth: true,
        permissions: ['system:log:view']
      }
    }
  },
  {
    permissionCode: 'article:view',
    menuItem: {
      id: 3,
      titleKey: 'menu.article',
      path: '/article',
      icon: 'Document',
      meta: {
        title: '文章管理',
        icon: 'Document',
        requireAuth: true,
        permissions: ['article:view']
      }
    }
  },
  {
    permissionCode: 'article:list',
    menuItem: {
      id: 31,
      titleKey: 'menu.articleList',
      path: '/article/list',
      icon: 'List',
      meta: {
        title: '文章列表',
        icon: 'List',
        requireAuth: true,
        permissions: ['article:list']
      }
    }
  },
  {
    permissionCode: 'article:category:list',
    menuItem: {
      id: 32,
      titleKey: 'menu.articleCategory',
      path: '/article/category',
      icon: 'Folder',
      meta: {
        title: '文章分类',
        icon: 'Folder',
        requireAuth: true,
        permissions: ['article:category:list']
      }
    }
  },
  {
    permissionCode: 'article:tag:list',
    menuItem: {
      id: 33,
      titleKey: 'menu.articleTag',
      path: '/article/tag',
      icon: 'PriceTag',
      meta: {
        title: '文章标签',
        icon: 'PriceTag',
        requireAuth: true,
        permissions: ['article:tag:list']
      }
    }
  },
  {
    permissionCode: 'user:view',
    menuItem: {
      id: 4,
      titleKey: 'menu.user',
      path: '/user',
      icon: 'User',
      meta: {
        title: '用户管理',
        icon: 'User',
        requireAuth: true,
        permissions: ['user:view']
      }
    }
  },
  {
    permissionCode: 'user:list:view',
    menuItem: {
      id: 41,
      titleKey: 'menu.userList',
      path: '/user/list',
      icon: 'UserFilled',
      meta: {
        title: '用户列表',
        icon: 'UserFilled',
        requireAuth: true,
        permissions: ['user:list:view']
      }
    }
  },
  {
    permissionCode: 'user:analysis:view',
    menuItem: {
      id: 42,
      titleKey: 'menu.analysis',
      path: '/user/analysis',
      icon: 'TrendCharts',
      meta: {
        title: '用户分析',
        icon: 'TrendCharts',
        requireAuth: true,
        permissions: ['user:analysis:view']
      }
    }
  },
  {
    permissionCode: 'order:view',
    menuItem: {
      id: 5,
      titleKey: 'menu.order',
      path: '/order',
      icon: 'ShoppingCart',
      meta: {
        title: '订单管理',
        icon: 'ShoppingCart',
        requireAuth: true,
        permissions: ['order:view']
      }
    }
  },
  {
    permissionCode: 'order:list:view',
    menuItem: {
      id: 51,
      titleKey: 'menu.orderList',
      path: '/order/list',
      icon: 'List',
      meta: {
        title: '订单列表',
        icon: 'List',
        requireAuth: true,
        permissions: ['order:list:view']
      }
    }
  },
  // {
  //   permissionCode: 'system:config:view',
  //   menuItem: {
  //     id: 6,
  //     title: '设置',
  //     path: '/settings',
  //     icon: 'Tools',
  //     meta: {
  //       title: '设置',
  //       icon: 'Tools',
  //       requireAuth: true,
  //       permissions: ['system:config:view']
  //     }
  //   }
  // }
]

// 菜单层级关系配置
const MENU_HIERARCHY: Record<string, string[]> = {
  'system:admin:view': ['system:admin:view'],
  'system:role:view': ['system:role:view'],
  'system:permission:view': ['system:permission:view'],
  'system:log:view': ['system:log:view'],
  'article:list': ['article:view', 'article:list'],
  'article:category:list': ['article:view', 'article:category:list'],
  'article:tag:list': ['article:view', 'article:tag:list'],
  'user:list:view': ['user:view', 'user:list:view'],
  'user:analysis:view': ['user:view', 'user:analysis:view'],
  'order:list:view': ['order:view', 'order:list:view']
}

// 转换配置项为菜单项，将titleKey转换为title
function configToMenuItem(config: PermissionMenuConfig): MenuItem {
  const item = JSON.parse(JSON.stringify(config.menuItem)) as any
  item.title = i18n.global.t(config.menuItem.titleKey)
  delete item.titleKey
  return item as MenuItem
}

// 从权限数据生成菜单
export function generateMenuFromPermissions(permissions: any[]): MenuItem[] {
  console.log('开始生成菜单，权限数据:', permissions)
  
  // 提取所有权限编码
  const permissionCodes = extractPermissionCodes(permissions)
  console.log('提取的权限编码:', permissionCodes)
  
  // 根据权限生成菜单项
  const menuItems: MenuItem[] = []
  const menuMap = new Map<number, MenuItem>()
  
  // 添加仪表盘（仅当用户有dashboard:view权限时）
  if (permissionCodes.includes('dashboard:view')) {
    const dashboardConfig = MENU_CONFIG_MAP.find(config => config.permissionCode === 'dashboard:view')
    if (dashboardConfig) {
      const dashboardMenu = configToMenuItem(dashboardConfig)
      menuItems.push(dashboardMenu)
      menuMap.set(dashboardMenu.id, dashboardMenu)
    }
  }
  
  // 生成其他菜单项
  for (const config of MENU_CONFIG_MAP) {
    if (config.permissionCode === 'dashboard:view') continue // 仪表盘已处理
    
    if (permissionCodes.includes(config.permissionCode)) {
      const menuItem = configToMenuItem(config)
      
      // 检查是否需要生成父菜单
      const hierarchy = MENU_HIERARCHY[config.permissionCode]
      if (hierarchy && hierarchy.length > 1) {
        // 需要生成父菜单
        const parentPermission = hierarchy[0]
        const parentConfig = MENU_CONFIG_MAP.find(c => c.permissionCode === parentPermission)
        
        if (parentConfig) {
          let parentMenu = menuMap.get(parentConfig.menuItem.id)
          if (!parentMenu) {
            parentMenu = configToMenuItem(parentConfig)
            if (parentMenu) {
              parentMenu.children = []
              menuItems.push(parentMenu)
              menuMap.set(parentMenu.id, parentMenu)
            }
            
            // 添加子菜单
            if (parentMenu && !parentMenu.children) {
              parentMenu.children = []
            }
            if (parentMenu && parentMenu.children) {
              parentMenu.children.push(menuItem)
            }
          } else {
            // 添加子菜单到现有父菜单
            if (!parentMenu.children) {
              parentMenu.children = []
            }
            if (parentMenu.children) {
              parentMenu.children.push(menuItem)
            }
          }
        }
      } else {
        // 顶级菜单
        menuItems.push(menuItem)
        menuMap.set(menuItem.id, menuItem)
      }
    }
  }
  
  // 处理系统管理父菜单
  const systemMenus = menuItems.filter(item => 
    item.path?.startsWith('/system/') && item.id !== 2
  )
  
  if (systemMenus.length > 0) {
    // 创建系统管理父菜单
    const systemParentMenu: MenuItem = {
      id: 2,
      title: i18n.global.t('menu.system'),
      path: '/system',
      icon: 'Setting',
      children: systemMenus,
      meta: {
        title: '系统管理',
        icon: 'Setting',
        requireAuth: true
      }
    }
    
    // 移除原来的系统菜单项，添加父菜单
    const filteredMenus = menuItems.filter(item => !item.path?.startsWith('/system/') || item.id === 2)
    filteredMenus.push(systemParentMenu)
    
    console.log('生成的菜单结构:', filteredMenus)
    return filteredMenus
  }
  
  console.log('生成的菜单结构:', menuItems)
  return menuItems
}

// 递归提取权限编码
function extractPermissionCodes(permissions: any[]): string[] {
  const codes: string[] = []
  
  function extract(perms: any[]) {
    for (const perm of perms) {
      if (perm.permissionCode) {
        codes.push(perm.permissionCode)
      }
      if (perm.children && Array.isArray(perm.children)) {
        extract(perm.children)
      }
    }
  }
  
  extract(permissions)
  return codes
}

// 检查用户是否有特定权限
export function hasPermission(userPermissions: string[], requiredPermission: string): boolean {
  return userPermissions.includes(requiredPermission)
}

// 检查用户是否有任一权限
export function hasAnyPermission(userPermissions: string[], requiredPermissions: string[]): boolean {
  return requiredPermissions.some(permission => userPermissions.includes(permission))
} 