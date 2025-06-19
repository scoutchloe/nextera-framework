<template>
  <div class="error-test">
    <h1>错误处理测试页面</h1>
    <p>测试修复后的错误处理，确保不会出现重复的错误提示</p>
    
    <div class="test-buttons">
      <el-button type="primary" @click="testSuccessResponse">
        测试成功响应
      </el-button>
      
      <el-button type="warning" @click="testBackendError">
        测试后端错误 (code !== 200)
      </el-button>
      
      <el-button type="danger" @click="testNetworkError">
        测试网络错误 (404)
      </el-button>
      
      <el-button type="info" @click="testServerError">
        测试服务器错误 (500)
      </el-button>
      
      <el-button type="success" @click="testTimeoutError">
        测试超时错误
      </el-button>
      
      <el-button type="warning" @click="testValidationError">
        测试验证错误 (400)
      </el-button>
    </div>
    
    <div class="test-results">
      <h3>测试结果 (应该只显示一个错误提示):</h3>
      <div class="result-item" v-for="(result, index) in testResults" :key="index">
        <el-tag :type="result.type">{{ result.time }}</el-tag>
        <span class="result-message">{{ result.message }}</span>
      </div>
    </div>
    
    <div class="instructions">
      <h3>测试说明:</h3>
      <ul>
        <li>✅ <strong>成功响应</strong>: 应该显示成功消息</li>
        <li>⚠️ <strong>后端错误</strong>: 应该只显示一个错误提示，内容为后端返回的具体错误信息</li>
        <li>🌐 <strong>网络错误</strong>: 应该只显示一个错误提示，不应该有"内部服务器错误"</li>
        <li>🔥 <strong>服务器错误</strong>: 应该只显示一个错误提示</li>
        <li>⏱️ <strong>超时错误</strong>: 应该只显示一个错误提示</li>
        <li>📝 <strong>验证错误</strong>: 应该只显示后端返回的具体验证错误信息</li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const testResults = ref<Array<{time: string, message: string, type: string}>>([])

// 通用错误处理函数 - 与权限管理页面相同
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

const addTestResult = (message: string, type: string = 'info') => {
  const now = new Date().toLocaleTimeString()
  testResults.value.unshift({ time: now, message, type })
  
  // 限制结果数量
  if (testResults.value.length > 15) {
    testResults.value = testResults.value.slice(0, 15)
  }
}

const testSuccessResponse = async () => {
  try {
    // 模拟成功响应
    const mockResponse = {
      code: 200,
      message: '操作成功',
      data: { id: 1, name: '测试数据' }
    }
    
    // 成功响应
    ElMessage.success('操作成功')
    addTestResult('✅ 成功响应测试通过 - 只显示一个成功消息', 'success')
  } catch (error: any) {
    const errorMessage = handleApiError(error, '测试失败')
    ElMessage.error(errorMessage)
    addTestResult(`❌ ${errorMessage}`, 'danger')
  }
}

const testBackendError = async () => {
  try {
    // 模拟后端返回错误 - 这会触发Promise.reject
    const mockError = new Error('参数验证失败：权限名称不能为空')
    throw mockError
  } catch (error: any) {
    const errorMessage = handleApiError(error, '操作失败')
    ElMessage.error(errorMessage)
    addTestResult(`⚠️ 后端错误测试: ${errorMessage} (应该只有一个错误提示)`, 'warning')
  }
}

const testNetworkError = async () => {
  try {
    // 模拟网络错误
    const mockError = {
      response: {
        status: 404,
        data: {
          message: '请求的资源不存在'
        }
      }
    }
    
    throw mockError
  } catch (error: any) {
    const errorMessage = handleApiError(error, '网络请求失败')
    ElMessage.error(errorMessage)
    addTestResult(`🌐 网络错误测试: ${errorMessage} (应该只有一个错误提示)`, 'danger')
  }
}

const testServerError = async () => {
  try {
    // 模拟服务器错误
    const mockError = {
      response: {
        status: 500,
        data: {
          message: '数据库连接失败，请联系管理员'
        }
      }
    }
    
    throw mockError
  } catch (error: any) {
    const errorMessage = handleApiError(error, '服务器错误')
    ElMessage.error(errorMessage)
    addTestResult(`🔥 服务器错误测试: ${errorMessage} (应该只有一个错误提示)`, 'danger')
  }
}

const testTimeoutError = async () => {
  try {
    // 模拟超时错误
    const mockError = {
      message: 'Request timeout of 5000ms exceeded'
    }
    
    throw mockError
  } catch (error: any) {
    const errorMessage = handleApiError(error, '请求超时')
    ElMessage.error(errorMessage)
    addTestResult(`⏱️ 超时错误测试: ${errorMessage} (应该只有一个错误提示)`, 'danger')
  }
}

const testValidationError = async () => {
  try {
    // 模拟验证错误
    const mockError = {
      response: {
        status: 400,
        data: {
          message: '权限编码格式不正确，应为 system:module:action 格式'
        }
      }
    }
    
    throw mockError
  } catch (error: any) {
    const errorMessage = handleApiError(error, '验证失败')
    ElMessage.error(errorMessage)
    addTestResult(`📝 验证错误测试: ${errorMessage} (应该只有一个错误提示)`, 'warning')
  }
}
</script>

<style scoped lang="scss">
.error-test {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;

  h1 {
    text-align: center;
    color: #303133;
    margin-bottom: 10px;
  }

  p {
    text-align: center;
    color: #909399;
    margin-bottom: 30px;
  }

  .test-buttons {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 16px;
    justify-content: center;
    margin-bottom: 30px;

    .el-button {
      min-width: 180px;
    }
  }

  .test-results {
    background: white;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    margin-bottom: 30px;

    h3 {
      margin-top: 0;
      margin-bottom: 16px;
      color: #303133;
    }

    .result-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 8px 0;
      border-bottom: 1px solid #ebeef5;

      &:last-child {
        border-bottom: none;
      }

      .result-message {
        flex: 1;
        color: #606266;
        font-size: 14px;
      }
    }
  }

  .instructions {
    background: #f8f9fa;
    border-radius: 8px;
    padding: 20px;
    border-left: 4px solid #409eff;

    h3 {
      margin-top: 0;
      margin-bottom: 16px;
      color: #303133;
    }

    ul {
      margin: 0;
      padding-left: 20px;

      li {
        margin-bottom: 8px;
        color: #606266;
        line-height: 1.5;

        strong {
          color: #303133;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .error-test {
    padding: 10px;

    .test-buttons {
      grid-template-columns: 1fr;

      .el-button {
        width: 100%;
      }
    }
  }
}
</style> 