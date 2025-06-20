import { JSEncrypt } from 'jsencrypt'
import request from '@/api/request'

/**
 * RSA加密工具类
 */
export class RSAUtil {
  private static publicKey: string = ''

  /**
   * 设置公钥
   */
  static setPublicKey(publicKey: string) {
    this.publicKey = publicKey
  }

  /**
   * 获取公钥
   */
  static getPublicKey(): string {
    return this.publicKey
  }

  /**
   * RSA加密
   * @param plainText 明文
   * @param publicKey 公钥（可选，如果不传则使用已设置的公钥）
   * @returns 密文
   */
  static encrypt(plainText: string, publicKey?: string): string {
    try {
      const keyToUse = publicKey || this.publicKey
      if (!keyToUse) {
        throw new Error('RSA公钥未设置')
      }

      const encrypt = new JSEncrypt()
      
      // 格式化公钥（添加PEM格式的头尾）
      const formattedKey = this.formatPublicKey(keyToUse)
      encrypt.setPublicKey(formattedKey)
      
      const encrypted = encrypt.encrypt(plainText)
      if (!encrypted) {
        throw new Error('RSA加密失败')
      }
      
      return encrypted
    } catch (error) {
      console.error('RSA加密失败:', error)
      throw new Error('RSA加密失败')
    }
  }

  /**
   * 加密JSON对象
   * @param data 要加密的数据对象
   * @param publicKey 公钥（可选）
   * @returns 密文
   */
  static encryptObject(data: any, publicKey?: string): string {
    const jsonString = JSON.stringify(data)
    return this.encrypt(jsonString, publicKey)
  }

  /**
   * 格式化公钥为PEM格式
   * @param publicKey Base64编码的公钥
   * @returns PEM格式的公钥
   */
  private static formatPublicKey(publicKey: string): string {
    // 如果已经是PEM格式，直接返回
    if (publicKey.includes('-----BEGIN PUBLIC KEY-----')) {
      return publicKey
    }

    // 将Base64字符串转换为PEM格式
    const header = '-----BEGIN PUBLIC KEY-----'
    const footer = '-----END PUBLIC KEY-----'
    
    // 每64个字符换行
    const formattedKey = publicKey.match(/.{1,64}/g)?.join('\n') || publicKey
    
    return `${header}\n${formattedKey}\n${footer}`
  }

  /**
   * 从后端获取公钥并设置
   */
  static async fetchAndSetPublicKey(): Promise<void> {
    try {
      const result = await request.get('/rsa/public-key')
      
      if (result.code === 200) {
        this.setPublicKey(result.data)
        console.log('RSA公钥获取成功')
      } else {
        throw new Error(result.message || '获取公钥失败')
      }
    } catch (error) {
      console.error('获取RSA公钥失败:', error)
      throw error
    }
  }

  /**
   * 初始化RSA工具（获取公钥）
   */
  static async init(): Promise<void> {
    if (!this.publicKey) {
      await this.fetchAndSetPublicKey()
    }
  }
} 