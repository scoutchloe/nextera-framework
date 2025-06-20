<template>
  <div class="avatar-test">
    <h2>头像上传下载测试</h2>
    
    <div class="test-section">
      <h3>1. 头像上传测试</h3>
      <el-upload
        class="avatar-uploader"
        action="#"
        :show-file-list="false"
        :before-upload="beforeAvatarUpload"
        :http-request="uploadAvatar"
      >
        <img v-if="avatarUrl" :src="avatarUrl" class="avatar" />
        <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
      </el-upload>
      
      <div v-if="uploadResult" class="upload-result">
        <h4>上传结果：</h4>
        <pre>{{ JSON.stringify(uploadResult, null, 2) }}</pre>
      </div>
    </div>

    <div class="test-section" v-if="avatarUrl">
      <h3>2. 头像显示测试</h3>
      <div class="avatar-display">
        <img :src="avatarUrl" alt="头像" style="width: 100px; height: 100px; object-fit: cover; border-radius: 50%;" />
        <p>当前头像URL: {{ avatarUrl }}</p>
      </div>
    </div>

    <div class="test-section" v-if="avatarUrl">
      <h3>3. 头像下载测试</h3>
      <el-button type="primary" @click="downloadCurrentAvatar">
        下载当前头像
      </el-button>
    </div>

    <div class="test-section">
      <h3>4. API调用日志</h3>
      <div class="log-container">
        <div v-for="(log, index) in logs" :key="index" class="log-item">
          <span class="log-time">{{ log.time }}</span>
          <span class="log-type" :class="log.type">{{ log.type }}</span>
          <span class="log-message">{{ log.message }}</span>
        </div>
      </div>
      <el-button @click="clearLogs" size="small">清空日志</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { UploadProps, UploadRequestOptions } from 'element-plus'
import { fileApi } from '@/api/system'

// 响应式数据
const avatarUrl = ref('')
const uploadResult = ref<any>(null)
const logs = ref<any[]>([])

// 添加日志
const addLog = (type: 'info' | 'success' | 'error', message: string) => {
  logs.value.unshift({
    time: new Date().toLocaleTimeString(),
    type,
    message
  })
}

// 清空日志
const clearLogs = () => {
  logs.value = []
}

// 头像上传前验证
const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile) => {
  addLog('info', `开始上传文件: ${rawFile.name}, 大小: ${(rawFile.size / 1024 / 1024).toFixed(2)}MB`)
  
  if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png' && rawFile.type !== 'image/gif' && rawFile.type !== 'image/webp') {
    const errorMsg = '头像图片只能是 JPG/PNG/GIF/WEBP 格式!'
    ElMessage.error(errorMsg)
    addLog('error', errorMsg)
    return false
  } else if (rawFile.size / 1024 / 1024 > 10) {
    const errorMsg = '头像图片大小不能超过 10MB!'
    ElMessage.error(errorMsg)
    addLog('error', errorMsg)
    return false
  }
  
  addLog('info', '文件验证通过，准备上传')
  return true
}

// 头像上传
const uploadAvatar = async (options: UploadRequestOptions) => {
  try {
    addLog('info', `调用上传API: /file/avatar/upload`)
    const response = await fileApi.uploadAvatar(options.file)
    
    addLog('info', `API响应: ${JSON.stringify(response)}`)
    
    if (response.code === 200 && response.data) {
      // 使用返回的文件URL更新头像
      avatarUrl.value = `/api${response.data.fileUrl}`
      uploadResult.value = response.data
      
      const successMsg = '头像上传成功'
      ElMessage.success(successMsg)
      addLog('success', `${successMsg}, URL: ${avatarUrl.value}`)
      
      options.onSuccess?.(response.data)
    } else {
      const errorMsg = response.message || '头像上传失败'
      ElMessage.error(errorMsg)
      addLog('error', errorMsg)
      options.onError?.({
        status: 400,
        method: 'POST',
        url: '/file/avatar/upload',
        message: errorMsg
      } as any)
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    const errorMsg = '头像上传失败，请重试'
    ElMessage.error(errorMsg)
    addLog('error', `${errorMsg}: ${error}`)
    options.onError?.({
      status: 500,
      method: 'POST',
      url: '/file/avatar/upload',
      message: '网络错误'
    } as any)
  }
}

// 下载当前头像
const downloadCurrentAvatar = () => {
  if (!avatarUrl.value) {
    ElMessage.warning('暂无头像可下载')
    return
  }
  
  addLog('info', `开始下载头像: ${avatarUrl.value}`)
  
  try {
    // 创建临时下载链接
    const link = document.createElement('a')
    link.href = avatarUrl.value
    link.download = 'avatar.jpg'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    addLog('success', '头像下载启动成功')
    ElMessage.success('头像下载已启动')
  } catch (error) {
    addLog('error', `头像下载失败: ${error}`)
    ElMessage.error('头像下载失败')
  }
}
</script>

<style lang="scss" scoped>
.avatar-test {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;

  h2 {
    color: #333;
    border-bottom: 2px solid #409eff;
    padding-bottom: 10px;
  }

  .test-section {
    margin: 30px 0;
    padding: 20px;
    border: 1px solid #ebeef5;
    border-radius: 8px;

    h3 {
      margin-top: 0;
      color: #409eff;
    }
  }

  .avatar-uploader {
    :deep(.el-upload) {
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: .3s;

      &:hover {
        border-color: #409eff;
      }
    }

    .avatar-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 100px;
      height: 100px;
      text-align: center;
      line-height: 100px;
    }

    .avatar {
      width: 100px;
      height: 100px;
      display: block;
      object-fit: cover;
    }
  }

  .upload-result {
    margin-top: 20px;
    
    pre {
      background: #f5f7fa;
      padding: 15px;
      border-radius: 4px;
      font-size: 12px;
      overflow-x: auto;
    }
  }

  .avatar-display {
    display: flex;
    align-items: center;
    gap: 20px;

    p {
      margin: 0;
      color: #666;
      word-break: break-all;
    }
  }

  .log-container {
    max-height: 300px;
    overflow-y: auto;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    padding: 10px;
    margin-bottom: 10px;

    .log-item {
      display: flex;
      gap: 10px;
      padding: 5px 0;
      border-bottom: 1px solid #f0f0f0;
      font-size: 14px;

      &:last-child {
        border-bottom: none;
      }

      .log-time {
        color: #666;
        font-size: 12px;
        white-space: nowrap;
      }

      .log-type {
        font-weight: bold;
        min-width: 60px;
        
        &.info {
          color: #409eff;
        }
        
        &.success {
          color: #67c23a;
        }
        
        &.error {
          color: #f56c6c;
        }
      }

      .log-message {
        flex: 1;
        word-break: break-all;
      }
    }
  }
}
</style> 