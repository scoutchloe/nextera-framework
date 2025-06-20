import JSEncrypt from 'jsencrypt'
import CryptoJS from 'crypto-js'
import request from '@/api/request'

interface HybridEncryptionData {
  encryptedKey: string
  encryptedData: string
}

/**
 * 混合加密工具类
 * 结合RSA和AES加密，提供更安全的数据传输
 */
class HybridEncryption {
  private rsaPublicKey: string | null = null
  private jsencrypt: JSEncrypt | null = null

  /**
   * 获取RSA公钥
   */
  private async getRSAPublicKey(): Promise<string> {
    if (this.rsaPublicKey) {
      return this.rsaPublicKey
    }

    try {
      const response = await request.get('/rsa/public-key')
      const rawPublicKey = response.data.publicKey
      
      // 将Base64公钥转换为PEM格式
      this.rsaPublicKey = this.formatPublicKeyToPEM(rawPublicKey)
      
      // 初始化RSA加密器
      this.jsencrypt = new JSEncrypt()
      this.jsencrypt.setPublicKey(this.rsaPublicKey!)
      
      console.log('RSA公钥获取成功')
      return this.rsaPublicKey!
    } catch (error) {
      console.error('获取RSA公钥失败:', error)
      throw new Error('获取RSA公钥失败')
    }
  }

  /**
   * 将Base64公钥转换为PEM格式
   */
  private formatPublicKeyToPEM(base64Key: string): string {
    const pemHeader = '-----BEGIN PUBLIC KEY-----'
    const pemFooter = '-----END PUBLIC KEY-----'
    
    // 每64个字符换行
    const formattedKey = base64Key.match(/.{1,64}/g)?.join('\n') || base64Key
    
    return `${pemHeader}\n${formattedKey}\n${pemFooter}`
  }

  /**
   * 生成随机AES密钥
   */
  private generateAESKey(): string {
    // 生成256位（32字节）的随机密钥
    const key = CryptoJS.lib.WordArray.random(256/8).toString()
    console.log('生成的AES密钥:', key)
    console.log('AES密钥长度:', key.length)
    return key
  }

  /**
   * AES加密（使用CBC模式兼容后端）
   */
  private aesEncrypt(data: string, key: string): string {
    try {
      // 将十六进制密钥转换为WordArray
      const keyBytes = CryptoJS.enc.Hex.parse(key)
      
      // 生成随机IV（16字节用于CBC模式）
      const iv = CryptoJS.lib.WordArray.random(16)
      
      // AES-CBC加密
      const encrypted = CryptoJS.AES.encrypt(data, keyBytes, {
        iv: iv,
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7
      })
      
      // 将IV和加密数据合并并转换为Base64
      const combined = iv.concat(encrypted.ciphertext)
      return CryptoJS.enc.Base64.stringify(combined)
    } catch (error) {
      console.error('AES加密失败:', error)
      throw new Error('AES加密失败')
    }
  }

  /**
   * RSA加密AES密钥
   */
  private rsaEncrypt(aesKey: string): string {
    if (!this.jsencrypt) {
      throw new Error('RSA加密器未初始化')
    }

    try {
      console.log('原始AES密钥:', aesKey)
      console.log('原始AES密钥长度:', aesKey.length)
      
      // 将十六进制密钥转换为Base64格式
      const keyBytes = CryptoJS.enc.Hex.parse(aesKey)
      const keyBase64 = CryptoJS.enc.Base64.stringify(keyBytes)
      
      console.log('转换为Base64的AES密钥:', keyBase64)
      console.log('Base64 AES密钥长度:', keyBase64.length)
      
      // 检查密钥长度是否合理（RSA 2048位最多可以加密245字节）
      if (keyBase64.length > 245) {
        throw new Error(`AES密钥太长，无法使用RSA加密。长度: ${keyBase64.length}`)
      }
      
      const encrypted = this.jsencrypt.encrypt(keyBase64)
      if (!encrypted) {
        console.error('JSEncrypt返回空值')
        console.error('公钥信息:', this.rsaPublicKey?.substring(0, 100) + '...')
        throw new Error('RSA加密AES密钥失败：加密器返回空值')
      }
      
      console.log('RSA加密成功，密文长度:', encrypted.length)
      return encrypted
    } catch (error) {
      console.error('RSA加密过程中出错:', error)
      console.error('当前公钥:', this.rsaPublicKey)
      throw new Error(`RSA加密AES密钥失败: ${error instanceof Error ? error.message : '未知错误'}`)
    }
  }

  /**
   * 混合加密
   * @param data 要加密的数据（对象或字符串）
   * @returns 混合加密后的数据
   */
  async hybridEncrypt(data: any): Promise<HybridEncryptionData> {
    try {
      // 1. 获取RSA公钥
      await this.getRSAPublicKey()

      // 2. 将数据转换为JSON字符串
      const jsonData = typeof data === 'string' ? data : JSON.stringify(data)
      console.log('准备加密的数据:', jsonData)

      // 3. 生成随机AES密钥
      const aesKey = this.generateAESKey()
      console.log('生成AES密钥成功')

      // 4. 使用AES加密业务数据
      const encryptedData = this.aesEncrypt(jsonData, aesKey)
      console.log('AES加密业务数据成功')

      // 5. 使用RSA加密AES密钥
      const encryptedKey = this.rsaEncrypt(aesKey)
      console.log('RSA加密AES密钥成功')

      return {
        encryptedKey,
        encryptedData
      }
    } catch (error) {
      console.error('混合加密失败:', error)
      throw error
    }
  }

  /**
   * 清除缓存的公钥（用于密钥轮换）
   */
  clearCache(): void {
    this.rsaPublicKey = null
    this.jsencrypt = null
    console.log('混合加密缓存已清除')
  }
}

// 创建单例实例
const hybridEncryption = new HybridEncryption()

export default hybridEncryption
export type { HybridEncryptionData } 