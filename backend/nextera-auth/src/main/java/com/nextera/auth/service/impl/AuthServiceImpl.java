package com.nextera.auth.service.impl;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.google.code.kaptcha.Producer;
import com.nextera.auth.dto.LoginRequest;
import com.nextera.auth.dto.LoginResponse;
import com.nextera.auth.dto.RegisterRequest;
import com.nextera.auth.entity.User;
import com.nextera.auth.mapper.UserMapper;
import com.nextera.auth.service.AuthService;
import com.nextera.common.constant.CommonConstants;
import com.nextera.common.core.Result;
import com.nextera.common.core.ResultCode;
import com.nextera.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 认证服务实现类
 *
 * @author Nextera
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final Producer kaptchaProducer;

    @Override
    public Result<LoginResponse> loginInner(com.nextera.api.auth.dto.LoginRequest loginRequest) {
        try {
            // 查询用户
            User user = userMapper.selectByUsername(loginRequest.getUsername());
            if (user == null) {
                return Result.error(ResultCode.USER_NOT_FOUND);
            }
            
            // 检查用户状态
            if (!CommonConstants.UserStatus.NORMAL.equals(user.getStatus())) {
                return Result.error(ResultCode.USER_DISABLED);
            }
            
            // 验证密码
            if (!BCrypt.checkpw(loginRequest.getPassword(), user.getPassword())) {
                return Result.error(ResultCode.LOGIN_AUTH_FAILED);
            }
            
            // 生成Token
            String accessToken = jwtUtil.generateToken(user.getId(), user.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());
            
            // 存储Token到Redis
            String tokenKey = CommonConstants.CachePrefix.LOGIN_TOKEN + user.getId();
            String refreshKey = CommonConstants.CachePrefix.REFRESH_TOKEN + user.getId();
            
            redisTemplate.opsForValue().set(tokenKey, accessToken, Duration.ofHours(24));
            redisTemplate.opsForValue().set(refreshKey, refreshToken, Duration.ofDays(7));
            
            // 更新用户登录信息
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateById(user);
            
            // 构建响应
            LoginResponse response = new LoginResponse();
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setExpiresIn(24 * 60 * 60L); // 24小时
            
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setNickname(user.getNickname());
            userInfo.setEmail(user.getEmail());
            userInfo.setAvatar(user.getAvatar());
            // TODO: 设置角色和权限
            userInfo.setRoles(new String[]{"USER"});
            userInfo.setPermissions(new String[]{"user:read"});
            
            response.setUserInfo(userInfo);
            
            log.info("用户登录成功: {}", user.getUsername());
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("用户登录失败", e);
            return Result.error("登录失败");
        }
    }

    @Override
    public Result<LoginResponse> login(LoginRequest loginRequest) {
        try {
            // 验证验证码
            String captchaKey = CommonConstants.CachePrefix.CAPTCHA + loginRequest.getUuid();
            String storedCaptcha = redisTemplate.opsForValue().get(captchaKey);

            if (StrUtil.isBlank(storedCaptcha)) {
                return Result.error(ResultCode.CAPTCHA_EXPIRED);
            }

            if (!storedCaptcha.equalsIgnoreCase(loginRequest.getCaptcha())) {
                return Result.error(ResultCode.CAPTCHA_ERROR);
            }

            // 删除验证码
            redisTemplate.delete(captchaKey);

            // 查询用户
            User user = userMapper.selectByUsername(loginRequest.getUsername());
            if (user == null) {
                return Result.error(ResultCode.USER_NOT_FOUND);
            }

            // 检查用户状态
            if (!CommonConstants.UserStatus.NORMAL.equals(user.getStatus())) {
                return Result.error(ResultCode.USER_DISABLED);
            }

            // 验证密码
            if (!BCrypt.checkpw(loginRequest.getPassword(), user.getPassword())) {
                return Result.error(ResultCode.LOGIN_FAILED);
            }

            // 生成Token
            String accessToken = jwtUtil.generateToken(user.getId(), user.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

            // 存储Token到Redis
            String tokenKey = CommonConstants.CachePrefix.LOGIN_TOKEN + user.getId();
            String refreshKey = CommonConstants.CachePrefix.REFRESH_TOKEN + user.getId();

            redisTemplate.opsForValue().set(tokenKey, accessToken, Duration.ofHours(24));
            redisTemplate.opsForValue().set(refreshKey, refreshToken, Duration.ofDays(7));

            // 更新用户登录信息
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateById(user);

            // 构建响应
            LoginResponse response = new LoginResponse();
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setExpiresIn(24 * 60 * 60L); // 24小时

            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setNickname(user.getNickname());
            userInfo.setEmail(user.getEmail());
            userInfo.setAvatar(user.getAvatar());
            // TODO: 设置角色和权限
            userInfo.setRoles(new String[]{"USER"});
            userInfo.setPermissions(new String[]{"user:read"});

            response.setUserInfo(userInfo);

            log.info("用户登录成功: {}", user.getUsername());
            return Result.success(response);

        } catch (Exception e) {
            log.error("用户登录失败", e);
            return Result.error("登录失败");
        }
    }

    @Override
    public Result<Void> registerInner(com.nextera.api.auth.dto.RegisterRequest registerRequest) {
        try {

            // 验证密码一致性
            if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                return Result.error(ResultCode.PASSWORD_NOT_MATCH);
            }
            
            // 检查用户名是否存在
            if (userMapper.selectByUsername(registerRequest.getUsername()) != null) {
                return Result.error(ResultCode.USERNAME_EXISTS);
            }
            
            // 检查邮箱是否存在
            if (userMapper.selectByEmail(registerRequest.getEmail()) != null) {
                return Result.error(ResultCode.EMAIL_EXISTS);
            }
            
            // 检查手机号是否存在（如果提供了手机号）
            if (StrUtil.isNotBlank(registerRequest.getPhone()) && 
                userMapper.selectByPhone(registerRequest.getPhone()) != null) {
                return Result.error(ResultCode.PHONE_EXISTS);
            }
            
            // 创建用户
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(BCrypt.hashpw(registerRequest.getPassword(), BCrypt.gensalt()));
            user.setNickname(registerRequest.getNickname());
            user.setEmail(registerRequest.getEmail());
            user.setPhone(registerRequest.getPhone());
            user.setStatus(CommonConstants.UserStatus.NORMAL);
            user.setGender(CommonConstants.Gender.UNKNOWN);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            
            userMapper.insert(user);
            
            log.info("用户注册成功: {}", user.getUsername());
            return Result.success(CommonConstants.REGISTER_SUCCESS);
            
        } catch (Exception e) {
            log.error("用户注册失败", e);
            return Result.error("注册失败");
        }
    }

    @Override
    public Result<Void> register(RegisterRequest registerRequest) {
        try {
            // 验证验证码
            String captchaKey = CommonConstants.CachePrefix.CAPTCHA + registerRequest.getUuid();
            String storedCaptcha = redisTemplate.opsForValue().get(captchaKey);

            if (StrUtil.isBlank(storedCaptcha)) {
                return Result.error(ResultCode.CAPTCHA_EXPIRED);
            }

            if (!storedCaptcha.equalsIgnoreCase(registerRequest.getCaptcha())) {
                return Result.error(ResultCode.CAPTCHA_ERROR);
            }

            // 删除验证码
            redisTemplate.delete(captchaKey);

            // 验证密码一致性
            if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                return Result.error(ResultCode.PASSWORD_NOT_MATCH);
            }

            // 检查用户名是否存在
            if (userMapper.selectByUsername(registerRequest.getUsername()) != null) {
                return Result.error(ResultCode.USERNAME_EXISTS);
            }

            // 检查邮箱是否存在
            if (userMapper.selectByEmail(registerRequest.getEmail()) != null) {
                return Result.error(ResultCode.EMAIL_EXISTS);
            }

            // 检查手机号是否存在（如果提供了手机号）
            if (StrUtil.isNotBlank(registerRequest.getPhone()) &&
                    userMapper.selectByPhone(registerRequest.getPhone()) != null) {
                return Result.error(ResultCode.PHONE_EXISTS);
            }

            // 创建用户
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setPassword(BCrypt.hashpw(registerRequest.getPassword(), BCrypt.gensalt()));
            user.setNickname(registerRequest.getNickname());
            user.setEmail(registerRequest.getEmail());
            user.setPhone(registerRequest.getPhone());
            user.setStatus(CommonConstants.UserStatus.NORMAL);
            user.setGender(CommonConstants.Gender.UNKNOWN);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());

            userMapper.insert(user);

            log.info("用户注册成功: {}", user.getUsername());
            return Result.success(CommonConstants.REGISTER_SUCCESS);

        } catch (Exception e) {
            log.error("用户注册失败", e);
            return Result.error("注册失败");
        }
    }

    @Override
    public Result<LoginResponse> refreshToken(String refreshToken) {
        try {
            // 验证刷新Token
            if (!jwtUtil.validateToken(refreshToken)) {
                return Result.error(ResultCode.REFRESH_TOKEN_INVALID);
            }
            
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            String username = jwtUtil.getUsernameFromToken(refreshToken);
            
            // 检查Redis中的刷新Token
            String refreshKey = CommonConstants.CachePrefix.REFRESH_TOKEN + userId;
            String storedRefreshToken = redisTemplate.opsForValue().get(refreshKey);
            
            if (!refreshToken.equals(storedRefreshToken)) {
                return Result.error(ResultCode.REFRESH_TOKEN_INVALID);
            }
            
            // 生成新的Token
            String newAccessToken = jwtUtil.generateToken(userId, username);
            String newRefreshToken = jwtUtil.generateRefreshToken(userId, username);
            
            // 更新Redis中的Token
            String tokenKey = CommonConstants.CachePrefix.LOGIN_TOKEN + userId;
            redisTemplate.opsForValue().set(tokenKey, newAccessToken, Duration.ofHours(24));
            redisTemplate.opsForValue().set(refreshKey, newRefreshToken, Duration.ofDays(7));
            
            LoginResponse response = new LoginResponse();
            response.setAccessToken(newAccessToken);
            response.setRefreshToken(newRefreshToken);
            response.setExpiresIn(24 * 60 * 60L);
            
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("刷新Token失败", e);
            return Result.error(ResultCode.REFRESH_TOKEN_INVALID);
        }
    }

    @Override
    public Result<Void> logout(String token) {
        try {
            if (!jwtUtil.validateToken(token)) {
                return Result.error(ResultCode.TOKEN_INVALID);
            }
            
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 删除Redis中的Token
            String tokenKey = CommonConstants.CachePrefix.LOGIN_TOKEN + userId;
            String refreshKey = CommonConstants.CachePrefix.REFRESH_TOKEN + userId;
            
            redisTemplate.delete(tokenKey);
            redisTemplate.delete(refreshKey);
            
            log.info("用户退出登录: {}", userId);
            return Result.success(CommonConstants.LOGOUT_SUCCESS);
            
        } catch (Exception e) {
            log.error("用户退出登录失败", e);
            return Result.error("退出登录失败");
        }
    }

    @Override
    public Result<Boolean> validateToken(String token) {
        try {
            if (!jwtUtil.validateToken(token)) {
                return Result.success(false);
            }
            
            Long userId = jwtUtil.getUserIdFromToken(token);
            String tokenKey = CommonConstants.CachePrefix.LOGIN_TOKEN + userId;
            String storedToken = redisTemplate.opsForValue().get(tokenKey);
            
            return Result.success(token.equals(storedToken));
            
        } catch (Exception e) {
            log.error("验证Token失败", e);
            return Result.success(false);
        }
    }

    @Override
    public Result<String> getCaptcha(String uuid) {
        try {
            // 生成验证码
            String captcha = kaptchaProducer.createText();
            BufferedImage image = kaptchaProducer.createImage(captcha);
            
            // 存储验证码到Redis，5分钟过期
            String captchaKey = CommonConstants.CachePrefix.CAPTCHA + uuid;
            redisTemplate.opsForValue().set(captchaKey, captcha, Duration.ofMinutes(5));
            
            // 转换图片为Base64
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            String base64Image = Base64Encoder.encodeStr(outputStream.toByteArray(), false, false);
            
            return Result.success("data:image/png;base64," + base64Image);
            
        } catch (Exception e) {
            log.error("生成验证码失败", e);
            return Result.error("生成验证码失败");
        }
    }
} 