import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types'

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // console.log('发送请求:', config.method?.toUpperCase(), config.url, config.data)
    
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    
    // 添加token
    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
      console.log('添加认证头部')
    }
    
    return config
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    console.log('收到响应:', response.status, response.config.url, response.data)
    
    // 如果是blob响应（文件下载），直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    
    const { data } = response
    
    // 处理业务逻辑错误
    if (data.code !== 200) {
      console.error('业务错误:', data.code, data.message)
      // 只处理需要全局处理的特殊错误码
      switch (data.code) {
        case 401:
          // 未授权，清除token并跳转登录
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          ElMessage.error('登录已过期，请重新登录')
          window.location.href = '/login'
          break
        // 其他错误码不在这里处理，让调用方处理
      }
      // 不要在这里显示错误消息，让调用方处理
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    
    console.log('请求成功，返回数据:', data)
    // 返回处理后的数据，但保持response结构
    return { ...response, data }
  },
  (error) => {
    console.error('Response error:', error)
    
    // 只处理需要全局处理的特殊HTTP错误
    if (error.response) {
      const { status } = error.response
      
      switch (status) {
        case 401:
          // 未授权，清除token并跳转登录
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          ElMessage.error('登录已过期，请重新登录')
          window.location.href = '/login'
          break
        // 其他HTTP错误不在这里处理，让调用方处理
      }
    }
    
    // 不要在这里显示错误消息，让调用方处理
    return Promise.reject(error)
  }
)

// 请求方法封装
export interface RequestOptions extends AxiosRequestConfig {
  loading?: boolean
  silent?: boolean
}

class Request {
  // GET请求
  get<T = any>(url: string, config?: RequestOptions): Promise<ApiResponse<T>> {
    return service.get(url, config).then(res => res.data)
  }
  
  // POST请求
  post<T = any>(url: string, data?: any, config?: RequestOptions): Promise<ApiResponse<T>> {
    return service.post(url, data, config).then(res => res.data)
  }
  
  // PUT请求
  put<T = any>(url: string, data?: any, config?: RequestOptions): Promise<ApiResponse<T>> {
    return service.put(url, data, config).then(res => res.data)
  }
  
  // DELETE请求
  delete<T = any>(url: string, config?: RequestOptions): Promise<ApiResponse<T>> {
    return service.delete(url, config).then(res => res.data)
  }
  
  // PATCH请求
  patch<T = any>(url: string, data?: any, config?: RequestOptions): Promise<ApiResponse<T>> {
    return service.patch(url, data, config).then(res => res.data)
  }
  
  // 上传文件
  upload<T = any>(url: string, formData: FormData, config?: RequestOptions): Promise<ApiResponse<T>> {
    return service.post(url, formData, {
      ...config,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    }).then(res => res.data)
  }
  
  // 下载文件
  download(url: string, filename?: string, config?: RequestOptions & { method?: string, data?: any }): Promise<void> {
    const method = config?.method?.toLowerCase() || 'get'
    const requestConfig = {
      ...config,
      responseType: 'blob' as const
    }
    
    let requestPromise: Promise<AxiosResponse>
    
    if (method === 'post') {
      requestPromise = service.post(url, config?.data, requestConfig)
    } else {
      requestPromise = service.get(url, requestConfig)
    }
    
    return requestPromise.then((response) => {
      const blob = new Blob([response.data])
      const downloadUrl = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = filename || 'download'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(downloadUrl)
    })
  }
}

export default new Request() 