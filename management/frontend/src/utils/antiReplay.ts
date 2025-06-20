import { v4 as uuidv4 } from 'uuid';
import CryptoJS from 'crypto-js';

/**
 * 防重放攻击工具类
 */

// 用户序列号存储
const userSequenceMap = new Map<string, number>();

/**
 * 防重放请求头配置
 */
export interface AntiReplayHeaders {
  'X-Timestamp': string;
  'X-Nonce': string;
  'X-Sequence'?: string;
  'X-Signature'?: string;
  [key: string]: string | undefined; // 添加索引签名以兼容axios
}

/**
 * 防重放配置选项
 */
export interface AntiReplayOptions {
  enableSequence?: boolean;
  enableSignature?: boolean;
  userId?: string;
  secretKey?: string;
}

/**
 * 生成随机nonce
 */
export function generateNonce(): string {
  return uuidv4().replace(/-/g, '');
}

/**
 * 获取当前时间戳
 */
export function getCurrentTimestamp(): string {
  return Date.now().toString();
}

/**
 * 获取用户下一个序列号
 */
export function getNextUserSequence(userId: string): number {
  const currentSequence = userSequenceMap.get(userId) || 0;
  const nextSequence = currentSequence + 1;
  userSequenceMap.set(userId, nextSequence);
  return nextSequence;
}

/**
 * 生成请求签名
 */
export function generateSignature(data: any, timestamp: string, nonce: string, secretKey: string): string {
  const sortedData = JSON.stringify(data, Object.keys(data).sort());
  const signString = `${sortedData}${timestamp}${nonce}${secretKey}`;
  return CryptoJS.SHA256(signString).toString();
}

/**
 * 生成防重放请求头
 */
export function generateAntiReplayHeaders(
  method: string,
  url: string,
  body: any = '',
  options: AntiReplayOptions = {}
): AntiReplayHeaders {
  const timestamp = getCurrentTimestamp();
  const nonce = generateNonce();
  
  const headers: AntiReplayHeaders = {
    'X-Timestamp': timestamp,
    'X-Nonce': nonce
  };
  
  // 添加序列号（如果启用）
  if (options.enableSequence && options.userId) {
    const sequence = getNextUserSequence(options.userId);
    headers['X-Sequence'] = sequence.toString();
  }
  
  // 添加签名（如果启用）
  if (options.enableSignature && options.secretKey && body) {
    const signature = generateSignature(body, timestamp, nonce, options.secretKey);
    headers['X-Signature'] = signature;
  }
  
  return headers;
}

/**
 * 为axios请求添加防重放头部
 */
export function addAntiReplayHeaders(
  config: any,
  options: AntiReplayOptions = {}
): void {
  const method = config.method || 'GET';
  const url = config.url || '';
  const body = config.data || '';
  
  const antiReplayHeaders = generateAntiReplayHeaders(method, url, body, options);
  
  // 合并到现有headers中
  config.headers = {
    ...config.headers,
    ...antiReplayHeaders
  };
}

/**
 * 重置用户序列号（用于测试或用户重新登录）
 */
export function resetUserSequence(userId: string): void {
  userSequenceMap.delete(userId);
}

/**
 * 获取用户当前序列号
 */
export function getCurrentUserSequence(userId: string): number {
  return userSequenceMap.get(userId) || 0;
}

/**
 * 防重放攻击工具类（兼容旧版本）
 */
export class AntiReplayUtil {
  private secretKey: string = 'nextera-anti-replay-secret-key-2024';
  
  /**
   * 设置密钥
   */
  setSecretKey(key: string): void {
    this.secretKey = key;
  }
  
  /**
   * 生成防重放头部（简化版本）
   */
  async generateHeaders(
    method: string,
    url: string,
    body: any = '',
    userId?: string,
    enableSequence: boolean = false,
    enableSignature: boolean = false
  ): Promise<AntiReplayHeaders> {
    return generateAntiReplayHeaders(method, url, body, {
      enableSequence,
      enableSignature,
      userId,
      secretKey: this.secretKey
    });
  }
}

// 创建默认实例
export const antiReplayUtil = new AntiReplayUtil();

// 高阶函数，为axios请求添加防重放头部
export function withAntiReplay(requestConfig: any, antiReplayConfig: AntiReplayOptions = {}) {
  const headers = generateAntiReplayHeaders(requestConfig.method || 'GET', requestConfig.url || '', requestConfig.data || '', antiReplayConfig);
  
  return {
    ...requestConfig,
    headers: {
      ...requestConfig.headers,
      ...headers
    }
  };
}

// 用户序列号管理
const sequenceMap = new Map<string, number>();

function getUserSequence(userId: string): number {
  const currentSequence = sequenceMap.get(userId) || 0;
  const nextSequence = currentSequence + 1;
  sequenceMap.set(userId, nextSequence);
  return nextSequence;
} 