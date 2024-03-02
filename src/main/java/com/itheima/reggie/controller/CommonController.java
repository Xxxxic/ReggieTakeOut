package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件的上传和下载
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {
    @Value("${reggie.path}")
    private String AssetsPath;

    /**
     * 文件上传
     *
     * @param file 上传的文件
     * @return 文件名字
     */
    @PostMapping("/upload")
    // 参数名必须和前端表单中的name的值一致
    public R<String> upload(MultipartFile file) {
        log.info(file.toString());

        // 给上传的文件起一个名字存放
        String originalFileName = file.getOriginalFilename();
        String suffix = null;
        if (originalFileName != null) {
            suffix = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID() + suffix;

        // 创建一个目录存放
        File dir = new File(AssetsPath);
        if (!dir.exists()) {
            // 不存在 创建一个目录
            boolean mkdirs = dir.mkdirs();
        }

        // 文件转存
        try {
            file.transferTo(new File(AssetsPath + '/' + fileName));
        } catch (IOException e) {
            //e.printStackTrace();
            log.error("文件上传出错: {}", e.toString());
            return R.error("文件上传出错");
        }

        return R.success(fileName);
    }

    /**
     * 下载文件
     */
    @GetMapping("/download")
    // 参数名必须和前端表单中的name的值一致
    public void download(String name, HttpServletResponse response) {
        // 在try后面添加一个小括号: JVM资源管理
        try (
                // 通过输入流读取文件内容 FileInputStream 用File从存储图片的地方获取用户需要的图片对象
                // 不用new File： FileInputStream直接用名字初始化即可
                FileInputStream fi = new FileInputStream(AssetsPath + '/' + name);
                // 输出流将文件写回浏览器
                ServletOutputStream os = response.getOutputStream()
        ) {
            // 设置写回去的文件类型
            response.setContentType("image/jpeg");

            // 缓存区读写文件
            int len;
            byte[] buf = new byte[1024];
            while ((len = fi.read(buf)) != -1) {
                os.write(buf, 0, len);
                os.flush();
            }
        } catch (Exception e) {
            //e.printStackTrace();
            log.error("文件下载失败: {}", e.toString());
        }
    }
}
