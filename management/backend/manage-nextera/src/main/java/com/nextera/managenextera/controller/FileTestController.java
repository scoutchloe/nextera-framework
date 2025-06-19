package com.nextera.managenextera.controller;

import com.nextera.managenextera.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件测试控制器
 *
 * @author nextera
 * @since 2025-01-28
 */
@Tag(name = "文件测试", description = "文件功能测试相关接口")
@RestController
@RequestMapping("/test/file")
@Slf4j
public class FileTestController {

    @Value("${file.upload.path:/uploads}")
    private String uploadPath;

    /**
     * 测试文件上传目录
     */
    @Operation(summary = "测试文件上传目录", description = "检查文件上传目录是否存在")
    @GetMapping("/check-upload-dir")
    public Result<Map<String, Object>> checkUploadDir() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 检查基础上传目录
            Path basePath = Paths.get(uploadPath);
            result.put("uploadPath", uploadPath);
            result.put("absolutePath", basePath.toAbsolutePath().toString());
            result.put("exists", Files.exists(basePath));
            result.put("isDirectory", Files.isDirectory(basePath));
            result.put("readable", Files.isReadable(basePath));
            result.put("writable", Files.isWritable(basePath));
            
            // 检查头像目录
            Path avatarPath = basePath.resolve("avatar");
            result.put("avatarPath", avatarPath.toString());
            result.put("avatarExists", Files.exists(avatarPath));
            
            // 创建目录（如果不存在）
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
                log.info("创建基础上传目录: {}", basePath);
            }
            
            if (!Files.exists(avatarPath)) {
                Files.createDirectories(avatarPath);
                log.info("创建头像目录: {}", avatarPath);
            }
            
            result.put("message", "目录检查完成");
            
            return Result.success("目录检查成功", result);
            
        } catch (Exception e) {
            log.error("检查上传目录失败", e);
            return Result.error("检查上传目录失败: " + e.getMessage());
        }
    }

    /**
     * 列出指定日期的头像文件
     */
    @Operation(summary = "列出头像文件", description = "列出指定日期的头像文件")
    @GetMapping("/list-avatars/{year}/{month}/{day}")
    public Result<Map<String, Object>> listAvatars(
            @PathVariable String year,
            @PathVariable String month,
            @PathVariable String day) {
        
        try {
            String dateFolder = year + "/" + month + "/" + day;
            Path avatarDirPath = Paths.get(uploadPath, "avatar", dateFolder);
            
            Map<String, Object> result = new HashMap<>();
            result.put("dateFolder", dateFolder);
            result.put("avatarDirPath", avatarDirPath.toAbsolutePath().toString());
            result.put("exists", Files.exists(avatarDirPath));
            
            if (Files.exists(avatarDirPath)) {
                File[] files = avatarDirPath.toFile().listFiles();
                if (files != null) {
                    String[] fileNames = new String[files.length];
                    for (int i = 0; i < files.length; i++) {
                        fileNames[i] = files[i].getName();
                    }
                    result.put("files", fileNames);
                    result.put("fileCount", files.length);
                } else {
                    result.put("files", new String[0]);
                    result.put("fileCount", 0);
                }
            } else {
                result.put("files", new String[0]);
                result.put("fileCount", 0);
                result.put("message", "目录不存在");
            }
            
            return Result.success("文件列表获取成功", result);
            
        } catch (Exception e) {
            log.error("列出头像文件失败", e);
            return Result.error("列出头像文件失败: " + e.getMessage());
        }
    }

    /**
     * 检查特定头像文件
     */
    @Operation(summary = "检查头像文件", description = "检查特定头像文件是否存在")
    @GetMapping("/check-avatar/{year}/{month}/{day}/{fileName}")
    public Result<Map<String, Object>> checkAvatar(
            @PathVariable String year,
            @PathVariable String month,
            @PathVariable String day,
            @PathVariable String fileName) {
        
        try {
            String dateFolder = year + "/" + month + "/" + day;
            String filePath = uploadPath + "/avatar/" + dateFolder + "/" + fileName;
            Path path = Paths.get(filePath);
            
            Map<String, Object> result = new HashMap<>();
            result.put("fileName", fileName);
            result.put("filePath", filePath);
            result.put("absolutePath", path.toAbsolutePath().toString());
            result.put("exists", Files.exists(path));
            
            if (Files.exists(path)) {
                result.put("size", Files.size(path));
                result.put("contentType", Files.probeContentType(path));
                result.put("readable", Files.isReadable(path));
                result.put("lastModified", Files.getLastModifiedTime(path).toString());
            }
            
            // 生成访问URL
            String accessUrl = "/api/file/avatar/download/" + year + "/" + month + "/" + day + "/" + fileName;
            result.put("accessUrl", accessUrl);
            
            return Result.success("文件检查完成", result);
            
        } catch (Exception e) {
            log.error("检查头像文件失败", e);
            return Result.error("检查头像文件失败: " + e.getMessage());
        }
    }
} 