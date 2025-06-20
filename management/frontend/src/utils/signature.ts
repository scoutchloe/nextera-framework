import CryptoJS from 'crypto-js'

/**
 * 签名工具类
 * 用于生成API接口的签名
 */
export class SignatureUtil {
  // 签名密钥（需要与后端保持一致）
  private static readonly SECRET_KEY = 'nextera_role_management_secret_key_2025'

  /**
   * 生成签名
   * @param params 参数对象
   * @param timestamp 时间戳
   * @returns 签名字符串
   */
  static generateSignature(params: Record<string, any>, timestamp: number): string {
    try {
      // 创建排序后的参数Map
      const sortedParams: Record<string, any> = {}
      
      // 排序参数，包含空字符串但排除null和undefined
      Object.keys(params)
        .sort()
        .forEach(key => {
          if (params[key] !== null && params[key] !== undefined) {
            // 包含空字符串，与后端保持一致
            sortedParams[key] = params[key]
          }
        })
      
      // 添加时间戳
      sortedParams.timestamp = timestamp
      
      // 构建签名字符串
      const signString = Object.keys(sortedParams)
        .sort()
        .map(key => `${key}=${sortedParams[key]}`)
        .join('&') + `&key=${this.SECRET_KEY}`
      
      console.debug('前端生成签名字符串:', signString)
      
      // MD5加密
      const signature = CryptoJS.MD5(signString).toString().toUpperCase()
      console.debug('前端生成签名:', signature)
      
      return signature
    } catch (error) {
      console.error('生成签名失败:', error)
      throw new Error('生成签名失败')
    }
  }

  /**
   * 为角色数据生成签名头部
   * @param roleData 角色数据
   * @returns 包含签名头部信息的对象
   */
  static generateSignatureHeaders(roleData: any): { headers: Record<string, string>; data: any } {
    const timestamp = Date.now()
    
    // 提取需要参与签名的参数，与后端保持一致的处理方式
    const signParams: Record<string, any> = {}
    
    // 包含所有字段，即使是空字符串，与后端逻辑保持一致
    if (roleData.roleCode !== null && roleData.roleCode !== undefined) signParams.roleCode = roleData.roleCode
    if (roleData.roleName !== null && roleData.roleName !== undefined) signParams.roleName = roleData.roleName
    if (roleData.status !== null && roleData.status !== undefined) signParams.status = roleData.status
    // 包含空字符串的description字段
    if (roleData.description !== null && roleData.description !== undefined) signParams.description = roleData.description
    
    // 处理permissionIds
    if (roleData.permissionIds && roleData.permissionIds.length > 0) {
      signParams.permissionIds = roleData.permissionIds.sort().join(',')
    }
    
    console.debug('前端签名参数:', signParams)
    
    // 生成签名
    const sign = this.generateSignature(signParams, timestamp)
    
    return {
      headers: {
        'X-Signature': sign,
        'X-Timestamp': timestamp.toString()
      },
      data: roleData
    }
  }

  /**
   * 为角色数据生成签名（兼容旧版本）
   * @param roleData 角色数据
   * @returns 包含签名和时间戳的完整数据
   */
  static signRoleData(roleData: any): any {
    const result = this.generateSignatureHeaders(roleData)
    return {
      ...result.data,
      timestamp: parseInt(result.headers['X-Timestamp']),
      sign: result.headers['X-Signature']
    }
  }
} 