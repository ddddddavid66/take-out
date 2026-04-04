package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;
    @PostMapping("upload")
    public Result<String> upload(MultipartFile file){
        log.info("上传文件{}",file);
        String originName = file.getOriginalFilename();
        //截取后缀
        int index = originName.lastIndexOf(".");
        String suffix = originName.substring(index);
        String newName = UUID.randomUUID().toString() + suffix;
        String url ;
        try {
            url = aliOssUtil.upload(file.getBytes(), newName);
        } catch (IOException e) {
            log.info("文件上传失败,{}",e);
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
        return Result.success(url);
    }
}
