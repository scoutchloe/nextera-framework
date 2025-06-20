import type { RouteRecordRaw } from 'vue-router'

// 测试相关路由
export const testRoutes: RouteRecordRaw[] = [
  {
    path: '/test',
    name: 'Test',
    meta: {
      title: '测试功能',
      icon: 'test',
      requireAuth: true
    },
    children: [
      {
        path: '/test/anti-replay',
        name: 'AntiReplayTest',
        component: () => import('@/views/test/AntiReplayTest.vue'),
        meta: {
          title: '防重放攻击测试',
          icon: 'security',
          requireAuth: true
        }
      },
      {
        path: '/test/role-signature',
        name: 'RoleSignatureTest',
        component: () => import('@/views/test/RoleSignatureTest.vue'),
        meta: {
          title: '角色签名测试',
          icon: 'key',
          requireAuth: true
        }
      },
      {
        path: '/test/signature-debug',
        name: 'SignatureDebugTest',
        component: () => import('@/views/test/SignatureDebugTest.vue'),
        meta: {
          title: '签名调试',
          icon: 'tools',
          requireAuth: true
        }
      },
      {
        path: '/test/hybrid-encryption',
        name: 'HybridEncryptionTest',
        component: () => import('@/views/test/HybridEncryptionTest.vue'),
        meta: {
          title: '混合加密测试',
          icon: 'encryption',
          requireAuth: true
        }
      },
      {
        path: '/test/role-delete-anti-replay',
        name: 'RoleDeleteAntiReplayTest',
        component: () => import('@/views/test/RoleDeleteAntiReplayTest.vue'),
        meta: {
          title: '角色删除防重放测试',
          icon: 'delete',
          requireAuth: true
        }
      }
    ]
  }
] 