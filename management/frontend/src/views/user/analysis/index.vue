<template>
  <div class="user-analysis">
    <div class="page-header">
      <div class="header-title">
        <h2>用户分析</h2>
        <p>用户数据统计与分析</p>
      </div>
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
          <div class="stat-number">{{ stats.activeUsers }}</div>
          <div class="stat-label">活跃用户</div>
        </div>
        <div class="stat-icon">
          <el-icon :size="32" color="#67c23a">
            <UserFilled />
          </el-icon>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-content">
          <div class="stat-number">{{ stats.newUsers }}</div>
          <div class="stat-label">新增用户</div>
        </div>
        <div class="stat-icon">
          <el-icon :size="32" color="#e6a23c">
            <Plus />
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
          <h3>用户增长趋势</h3>
          <el-select v-model="timeRange" size="small">
            <el-option label="最近7天" value="7d" />
            <el-option label="最近30天" value="30d" />
            <el-option label="最近90天" value="90d" />
          </el-select>
        </div>
        <div class="chart-content">
          <div class="chart-placeholder">
            <el-icon :size="48" color="#ddd">
              <TrendCharts />
            </el-icon>
            <p>用户增长趋势图表</p>
          </div>
        </div>
      </div>

      <div class="chart-card">
        <div class="card-header">
          <h3>用户地域分布</h3>
        </div>
        <div class="chart-content">
          <div class="chart-placeholder">
            <el-icon :size="48" color="#ddd">
              <Location />
            </el-icon>
            <p>用户地域分布图表</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 用户行为分析 -->
    <div class="behavior-analysis">
      <div class="analysis-card">
        <div class="card-header">
          <h3>用户行为分析</h3>
        </div>
        <div class="behavior-list">
          <div class="behavior-item">
            <div class="behavior-label">平均会话时长</div>
            <div class="behavior-value">15分32秒</div>
          </div>
          <div class="behavior-item">
            <div class="behavior-label">页面浏览量</div>
            <div class="behavior-value">8.5页/会话</div>
          </div>
          <div class="behavior-item">
            <div class="behavior-label">跳出率</div>
            <div class="behavior-value">32.4%</div>
          </div>
          <div class="behavior-item">
            <div class="behavior-label">转化率</div>
            <div class="behavior-value">4.2%</div>
          </div>
        </div>
      </div>

      <div class="analysis-card">
        <div class="card-header">
          <h3>热门页面</h3>
        </div>
        <div class="page-list">
          <div class="page-item" v-for="page in hotPages" :key="page.id">
            <div class="page-info">
              <div class="page-title">{{ page.title }}</div>
              <div class="page-url">{{ page.url }}</div>
            </div>
            <div class="page-stats">
              <div class="page-views">{{ page.views }}次访问</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { 
  User, 
  UserFilled, 
  Plus, 
  Connection, 
  TrendCharts, 
  Location 
} from '@element-plus/icons-vue'

// 统计数据
const stats = reactive({
  totalUsers: 12568,
  activeUsers: 8234,
  newUsers: 156,
  onlineUsers: 342
})

// 时间范围
const timeRange = ref('7d')

// 热门页面数据
const hotPages = ref([
  {
    id: 1,
    title: '首页',
    url: '/',
    views: 15432
  },
  {
    id: 2,
    title: '产品页面',
    url: '/products',
    views: 8765
  },
  {
    id: 3,
    title: '关于我们',
    url: '/about',
    views: 5432
  },
  {
    id: 4,
    title: '联系我们',
    url: '/contact',
    views: 3210
  },
  {
    id: 5,
    title: '帮助中心',
    url: '/help',
    views: 2345
  }
])
</script>

<style lang="scss" scoped>
.user-analysis {
  .page-header {
    margin-bottom: 24px;
    
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
          font-size: 28px;
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
        height: 300px;
        
        .chart-placeholder {
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          height: 100%;
          color: var(--text-tertiary);
          
          p {
            margin: 12px 0 0 0;
            font-size: 14px;
          }
        }
      }
    }
  }

  .behavior-analysis {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
    
    .analysis-card {
      background: var(--bg-primary);
      border: 1px solid var(--border-color);
      border-radius: var(--radius-lg);
      overflow: hidden;
      
      .card-header {
        padding: 20px 24px;
        border-bottom: 1px solid var(--border-color);
        
        h3 {
          margin: 0;
          font-size: 18px;
          font-weight: 600;
          color: var(--text-primary);
        }
      }
      
      .behavior-list {
        padding: 24px;
        
        .behavior-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 16px 0;
          border-bottom: 1px solid var(--border-light);
          
          &:last-child {
            border-bottom: none;
          }
          
          .behavior-label {
            color: var(--text-secondary);
            font-size: 14px;
          }
          
          .behavior-value {
            color: var(--text-primary);
            font-size: 16px;
            font-weight: 600;
          }
        }
      }
      
      .page-list {
        padding: 24px;
        max-height: 300px;
        overflow-y: auto;
        
        .page-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          padding: 12px 0;
          border-bottom: 1px solid var(--border-light);
          
          &:last-child {
            border-bottom: none;
          }
          
          .page-info {
            flex: 1;
            
            .page-title {
              color: var(--text-primary);
              font-size: 14px;
              font-weight: 500;
              margin-bottom: 4px;
            }
            
            .page-url {
              color: var(--text-tertiary);
              font-size: 12px;
            }
          }
          
          .page-stats {
            .page-views {
              color: var(--text-secondary);
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
  .user-analysis {
    .charts-grid {
      grid-template-columns: 1fr;
    }
    
    .behavior-analysis {
      grid-template-columns: 1fr;
    }
  }
}

@media (max-width: 768px) {
  .user-analysis {
    .stats-grid {
      grid-template-columns: 1fr;
    }
  }
}
</style> 