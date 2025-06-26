<template>
  <div class="order-statistics-wrapper">
    <h2 class="section-title">📊 订单统计数据</h2>
    
    <div class="stats-container">
      <!-- 概览统计 -->
      <div class="overview-section">
        <div class="stat-card today">
          <div class="stat-icon">📅</div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.todayOrderCount }}</div>
            <div class="stat-label">今日订单</div>
          </div>
        </div>

        <div class="stat-card month">
          <div class="stat-icon">📆</div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.monthOrderCount }}</div>
            <div class="stat-label">本月订单</div>
          </div>
        </div>

        <div class="stat-card total">
          <div class="stat-icon">📈</div>
          <div class="stat-content">
            <div class="stat-number">{{ stats.totalOrderCount }}</div>
            <div class="stat-label">总订单数</div>
          </div>
        </div>
      </div>

      <!-- 商品排行榜 -->
      <div class="ranking-section">
        <div class="section-header">
          <h3>🏆 热销商品排行</h3>
          <el-button @click="refreshData" size="small" :loading="loading" type="primary">
            刷新
          </el-button>
        </div>
        
        <div class="ranking-list">
          <div v-for="(item, index) in products" :key="item.id" class="ranking-item">
            <div class="rank-badge">{{ index + 1 }}</div>
            <div class="product-name">{{ item.name }}</div>
            <div class="product-sales">{{ item.sales }}销量</div>
          </div>
        </div>
      </div>

      <!-- 调试信息 -->
      <div class="debug-section">
        <p><strong>组件状态:</strong> {{ loading ? '加载中...' : '已加载' }}</p>
        <p><strong>加载时间:</strong> {{ loadTime }}</p>
        <p><strong>数据更新:</strong> {{ updateTime }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'

const loading = ref(false)
const loadTime = ref('')
const updateTime = ref('')

const stats = reactive({
  todayOrderCount: 0,
  monthOrderCount: 0,
  totalOrderCount: 0
})

const products = ref([
  { id: 1, name: '测试商品1', sales: 150 },
  { id: 2, name: '测试商品2', sales: 120 },
  { id: 3, name: '测试商品3', sales: 100 },
  { id: 4, name: '测试商品4', sales: 80 },
  { id: 5, name: '测试商品5', sales: 60 }
])

const refreshData = async () => {
  loading.value = true
  updateTime.value = new Date().toLocaleString()
  
  // 模拟加载数据
  setTimeout(() => {
    stats.todayOrderCount = 128
    stats.monthOrderCount = 3420
    stats.totalOrderCount = 45678
    loading.value = false
    console.log('订单统计数据已刷新')
  }, 1000)
}

onMounted(() => {
  loadTime.value = new Date().toLocaleString()
  console.log('OrderStatistics 组件已挂载')
  refreshData()
})
</script>

<style scoped>
.order-statistics-wrapper {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 24px;
  margin: 20px 0;
  color: white;
  min-height: 400px;
}

.section-title {
  text-align: center;
  font-size: 24px;
  margin-bottom: 24px;
  font-weight: bold;
}

.overview-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #333;
}

.stat-icon {
  font-size: 24px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #667eea;
  border-radius: 50%;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #2c3e50;
}

.stat-label {
  font-size: 14px;
  color: #7f8c8d;
}

.ranking-section {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 24px;
  color: #333;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.ranking-item:last-child {
  border-bottom: none;
}

.rank-badge {
  width: 24px;
  height: 24px;
  background: #667eea;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 12px;
  margin-right: 12px;
}

.product-name {
  flex: 1;
  font-weight: 500;
}

.product-sales {
  color: #666;
  font-size: 14px;
}

.debug-section {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 16px;
  font-size: 14px;
}

.debug-section p {
  margin: 4px 0;
}
</style> 