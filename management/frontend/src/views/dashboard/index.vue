<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h2>仪表盘</h2>
      <p>欢迎回来，{{ userStore.userInfo?.adminInfo?.realName || '管理员' }}！</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.totalUsers }}</div>
          <div class="stat-label">总用户数</div>
        </div>
        <div class="stat-icon">
          <el-icon :size="32" color="#409eff">
            <User />
          </el-icon>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.totalArticles }}</div>
          <div class="stat-label">文章总数</div>
        </div>
        <div class="stat-icon">
          <el-icon :size="32" color="#67c23a">
            <Document />
          </el-icon>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.todayVisits }}</div>
          <div class="stat-label">今日访问</div>
        </div>
        <div class="stat-icon">
          <el-icon :size="32" color="#e6a23c">
            <View />
          </el-icon>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.onlineUsers }}</div>
          <div class="stat-label">在线用户</div>
        </div>
        <div class="stat-icon">
          <el-icon :size="32" color="#f56c6c">
            <Connection />
          </el-icon>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-grid">
      <div class="chart-card">
        <div class="card-header">
          <h3>访问趋势</h3>
          <el-select v-model="timeRange" size="small">
            <el-option label="最近7天" value="7d" />
            <el-option label="最近30天" value="30d" />
            <el-option label="最近90天" value="90d" />
          </el-select>
        </div>
        <div class="chart-content">
          <div id="visitChart" style="height: 300px;">
            <!-- 这里可以集成图表库如 ECharts -->
            <div class="chart-placeholder">
              <el-icon :size="48" color="#ddd">
                <TrendCharts />
              </el-icon>
              <p>访问趋势图表</p>
            </div>
          </div>
        </div>
      </div>

      <div class="chart-card">
        <div class="card-header">
          <h3>用户分布</h3>
        </div>
        <div class="chart-content">
          <div id="userChart" style="height: 300px;">
            <div class="chart-placeholder">
              <el-icon :size="48" color="#ddd">
                <PieChart />
              </el-icon>
              <p>用户分布图表</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <div class="action-card">
        <div class="action-header">
          <h3>快捷操作</h3>
        </div>
        <div class="action-buttons">
          <el-button type="primary" :icon="Plus" @click="$router.push('/system/admin')">
            新增管理员
          </el-button>
          <el-button type="success" :icon="EditPen" @click="$router.push('/article/list')">
            发布文章
          </el-button>
          <el-button type="warning" :icon="Setting" @click="$router.push('/system/role')">
            角色管理
          </el-button>
          <el-button type="info" :icon="Document" @click="$router.push('/system/log')">
            系统日志
          </el-button>
        </div>
      </div>

      <div class="action-card">
        <div class="action-header">
          <h3>最近活动</h3>
        </div>
        <div class="activity-list">
          <div 
            v-for="activity in recentActivities" 
            :key="activity.id" 
            class="activity-item"
          >
            <div class="activity-avatar">
              <el-avatar :size="32">{{ activity.user.charAt(0) }}</el-avatar>
            </div>
            <div class="activity-content">
              <div class="activity-text">
                <strong>{{ activity.user }}</strong> {{ activity.action }}
              </div>
              <div class="activity-time">{{ activity.time }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { 
  User, 
  Document, 
  View, 
  Connection, 
  TrendCharts, 
  PieChart, 
  Plus, 
  EditPen, 
  Setting 
} from '@element-plus/icons-vue'

const userStore = useUserStore()

// 统计数据
const stats = reactive({
  totalUsers: 1256,
  totalArticles: 342,
  todayVisits: 1024,
  onlineUsers: 28
})

// 时间范围选择
const timeRange = ref('7d')

// 最近活动
const recentActivities = ref([
  {
    id: 1,
    user: '张三',
    action: '登录了系统',
    time: '2分钟前'
  },
  {
    id: 2,
    user: '李四',
    action: '发布了新文章',
    time: '5分钟前'
  },
  {
    id: 3,
    user: '王五',
    action: '修改了用户权限',
    time: '10分钟前'
  },
  {
    id: 4,
    user: '赵六',
    action: '删除了一篇文章',
    time: '15分钟前'
  },
  {
    id: 5,
    user: '管理员',
    action: '更新了系统配置',
    time: '30分钟前'
  }
])

// 加载仪表盘数据
const loadDashboardData = () => {
  // 模拟加载数据
  console.log('Loading dashboard data...')
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style lang="scss" scoped>
.dashboard {
  .dashboard-header {
    margin-bottom: 24px;
    
    h2 {
      margin: 0 0 8px 0;
      color: var(--text-primary);
      font-size: 28px;
      font-weight: 600;
    }
    
    p {
      margin: 0;
      color: var(--text-secondary);
      font-size: 16px;
    }
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 20px;
    margin-bottom: 24px;
    
    .stat-card {
      background: var(--bg-primary);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-lg);
      padding: 24px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      transition: all 0.3s ease;
      
      &:hover {
        box-shadow: var(--shadow-md);
        transform: translateY(-2px);
      }
      
      .stat-content {
        .stat-number {
          font-size: 32px;
          font-weight: 700;
          color: var(--text-primary);
          margin-bottom: 4px;
        }
        
        .stat-label {
          font-size: 14px;
          color: var(--text-secondary);
        }
      }
      
      .stat-icon {
        opacity: 0.8;
      }
    }
  }

  .charts-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
    margin-bottom: 24px;
    
    .chart-card {
      background: var(--bg-primary);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-lg);
      overflow: hidden;
      
      .card-header {
        padding: 20px 24px;
        border-bottom: 1px solid var(--border-color);
        display: flex;
        align-items: center;
        justify-content: space-between;
        
        h3 {
          margin: 0;
          font-size: 18px;
          font-weight: 600;
          color: var(--text-primary);
        }
      }
      
      .chart-content {
        padding: 24px;
        
        .chart-placeholder {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          height: 300px;
          color: var(--text-tertiary);
          
          p {
            margin: 12px 0 0 0;
            font-size: 14px;
          }
        }
      }
    }
  }

  .quick-actions {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
    
    .action-card {
      background: var(--bg-primary);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-lg);
      overflow: hidden;
      
      .action-header {
        padding: 20px 24px;
        border-bottom: 1px solid var(--border-color);
        
        h3 {
          margin: 0;
          font-size: 18px;
          font-weight: 600;
          color: var(--text-primary);
        }
      }
      
      .action-buttons {
        padding: 24px;
        display: flex;
        flex-wrap: wrap;
        gap: 12px;
      }
      
      .activity-list {
        padding: 24px;
        max-height: 300px;
        overflow-y: auto;
        
        .activity-item {
          display: flex;
          align-items: center;
          padding: 12px 0;
          border-bottom: 1px solid var(--border-light);
          
          &:last-child {
            border-bottom: none;
          }
          
          .activity-avatar {
            margin-right: 12px;
          }
          
          .activity-content {
            flex: 1;
            
            .activity-text {
              color: var(--text-primary);
              font-size: 14px;
              margin-bottom: 4px;
            }
            
            .activity-time {
              color: var(--text-tertiary);
              font-size: 12px;
            }
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 1024px) {
  .dashboard {
    .charts-grid {
      grid-template-columns: 1fr;
    }
    
    .quick-actions {
      grid-template-columns: 1fr;
    }
  }
}

@media (max-width: 768px) {
  .dashboard {
    .stats-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style> 