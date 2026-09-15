package com.shop.mall.controller;

import com.shop.mall.common.BusinessException;
import com.shop.mall.common.Result;
import com.shop.mall.common.ResultCode;
import com.shop.mall.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传 Controller
 * 管理后台商品图片上传, 保存到本地 uploads 目录, 返回可访问 URL。
 *
 * @author shop-mall
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${shop.upload.path:./uploads/}")
    private String uploadPath;

    @Value("${shop.upload.url-prefix:/upload/}")
    private String urlPrefix;

    /** 允许的图片扩展名 */
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        UserContext.requireAdmin();
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "上传文件不能为空");
        }

        // 校验文件扩展名
        String originalName = file.getOriginalFilename();
        String extension = getExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException(ResultCode.PARAM_INVALID,
                    "不支持的图片格式: " + extension + ", 仅支持 " + ALLOWED_EXTENSIONS);
        }

        // 按日期分目录: uploads/2026/09/15/xxx.jpg
        // 转为绝对路径, 避免 transferTo 解析到 Tomcat 临时工作目录
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        File baseDir = new File(uploadPath).getAbsoluteFile();
        File destDir = new File(baseDir, dateDir);
        if (!destDir.exists() && !destDir.mkdirs()) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "创建上传目录失败");
        }

        // 生成唯一文件名, 避免重名覆盖
        String newFileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        File destFile = new File(destDir, newFileName);

        try {
            file.transferTo(destFile.getAbsoluteFile());
        } catch (IOException e) {
            log.error("文件上传失败: {}", originalName, e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "文件上传失败: " + e.getMessage());
        }

        // 返回可访问 URL: /upload/2026/09/15/xxx.jpg
        String url = urlPrefix + dateDir + "/" + newFileName;
        log.info("文件上传成功: {} -> {}", originalName, url);
        return Result.success(url);
    }

    /** 获取文件扩展名(不含点) */
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
