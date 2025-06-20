package com.nextera.managenextera.controller;

import com.nextera.managenextera.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传下载控制器
 *
 * @author nextera
 * @since 2025-01-28
 */
@Tag(name = "文件管理", description = "文件上传下载相关接口")
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Value("${file.upload.path:/uploads}")
    private String uploadPath;

    @Value("${file.upload.max-size:10485760}")
    private long maxFileSize; // 10MB

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    /**
     * 上传头像
     */
    @Operation(summary = "上传头像", description = "上传用户头像文件")
    @PostMapping("/avatar/upload")
    @PreAuthorize("hasAuthority('system:admin:edit')")
    public Result<Map<String, Object>> uploadAvatar(
            @Parameter(description = "头像文件") @RequestParam("file") MultipartFile file) {
        
        try {
            // 验证文件
            String validationResult = validateImageFile(file);
            if (validationResult != null) {
                return Result.error(validationResult);
            }

            // 创建上传目录
            String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String uploadDir = uploadPath + "/avatar/" + dateFolder;
            Path uploadDirPath = Paths.get(uploadDir);
            if (!Files.exists(uploadDirPath)) {
                Files.createDirectories(uploadDirPath);
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String newFileName = UUID.randomUUID().toString() + fileExtension;
            
            // 保存文件
            Path filePath = uploadDirPath.resolve(newFileName);
            Files.copy(file.getInputStream(), filePath);

            // 生成访问URL
            String fileUrl = "/file/avatar/download/" + dateFolder + "/" + newFileName;
            
            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("fileName", newFileName);
            result.put("originalName", originalFilename);
            result.put("filePath", filePath.toString());
            result.put("fileUrl", fileUrl);
            result.put("fileSize", file.getSize());
            
            log.info("头像上传成功: 原文件名={}, 新文件名={}, 文件大小={}", 
                    originalFilename, newFileName, file.getSize());
            
            return Result.success("头像上传成功", result);
            
        } catch (IOException e) {
            log.error("头像上传失败", e);
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 下载头像
     */
    @Operation(summary = "下载头像", description = "下载用户头像文件")
    @GetMapping("/avatar/download/{year}/{month}/{day}/{fileName}")
    public ResponseEntity<Resource> downloadAvatar(
            @Parameter(description = "年份") @PathVariable String year,
            @Parameter(description = "月份") @PathVariable String month,
            @Parameter(description = "日期") @PathVariable String day,
            @Parameter(description = "文件名") @PathVariable String fileName) {
        
        try {
            // 构建文件路径
            String dateFolder = year + "/" + month + "/" + day;
            String filePath = uploadPath + "/avatar/" + dateFolder + "/" + fileName;
            Path path = Paths.get(filePath);
            
            log.info("尝试访问文件: {}", filePath);
            
            if (!Files.exists(path)) {
                log.warn("文件不存在: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(path);
            
            // 确定内容类型
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            log.info("成功访问头像文件: {}, 内容类型: {}", filePath, contentType);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);
                    
        } catch (IOException e) {
            log.error("下载头像失败: {}", fileName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取头像信息
     */
    @Operation(summary = "获取头像信息", description = "获取头像文件信息")
    @GetMapping("/avatar/info/{year}/{month}/{day}/{fileName}")
    @PreAuthorize("hasAuthority('system:admin:view')")
    public Result<Map<String, Object>> getAvatarInfo(
            @Parameter(description = "年份") @PathVariable String year,
            @Parameter(description = "月份") @PathVariable String month,
            @Parameter(description = "日期") @PathVariable String day,
            @Parameter(description = "文件名") @PathVariable String fileName) {
        
        try {
            String dateFolder = year + "/" + month + "/" + day;
            String filePath = uploadPath + "/avatar/" + dateFolder + "/" + fileName;
            Path path = Paths.get(filePath);
            
            if (!Files.exists(path)) {
                return Result.error("文件不存在");
            }

            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("fileName", fileName);
            fileInfo.put("filePath", filePath);
            fileInfo.put("fileSize", Files.size(path));
            fileInfo.put("contentType", Files.probeContentType(path));
            fileInfo.put("lastModified", Files.getLastModifiedTime(path).toString());
            
            return Result.success(fileInfo);
            
        } catch (IOException e) {
            log.error("获取头像信息失败: {}", fileName, e);
            return Result.error("获取头像信息失败: " + e.getMessage());
        }
    }

    /**
     * 删除头像
     */
    @Operation(summary = "删除头像", description = "删除用户头像文件")
    @DeleteMapping("/avatar/delete/{year}/{month}/{day}/{fileName}")
    @PreAuthorize("hasAuthority('system:admin:edit')")
    public Result<String> deleteAvatar(
            @Parameter(description = "年份") @PathVariable String year,
            @Parameter(description = "月份") @PathVariable String month,
            @Parameter(description = "日期") @PathVariable String day,
            @Parameter(description = "文件名") @PathVariable String fileName) {
        
        try {
            String dateFolder = year + "/" + month + "/" + day;
            String filePath = uploadPath + "/avatar/" + dateFolder + "/" + fileName;
            Path path = Paths.get(filePath);
            
            if (!Files.exists(path)) {
                return Result.error("文件不存在");
            }

            Files.delete(path);
            log.info("头像删除成功: {}", filePath);
            
            return Result.success("头像删除成功");
            
        } catch (IOException e) {
            log.error("删除头像失败: {}", fileName, e);
            return Result.error("删除头像失败: " + e.getMessage());
        }
    }

    /**
     * 验证图片文件
     */
    private String validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            return "文件不能为空";
        }

        // 检查文件大小
        if (file.getSize() > maxFileSize) {
            return "文件大小不能超过 " + (maxFileSize / 1024 / 1024) + "MB";
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return "只能上传图片文件";
        }

        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return "文件名不能为空";
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!extension.matches("\\.(jpg|jpeg|png|gif|bmp|webp)")) {
            return "只支持 JPG、PNG、GIF、BMP、WEBP 格式的图片";
        }

        return null; // 验证通过
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
} 