package com.aoshen.usercenter.controller;

import com.aoshen.usercenter.common.BaseResponse;
import com.aoshen.usercenter.common.QiniuUtil;
import com.aoshen.usercenter.common.ResultUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

import static com.aoshen.usercenter.contant.ServerConstant.PROD_ADDRESS;
import static com.aoshen.usercenter.contant.ServerConstant.SERVER_ADDRESS;


@RestController
@RequestMapping("/fileOss")
@CrossOrigin(origins = {SERVER_ADDRESS, PROD_ADDRESS}, allowCredentials = "true")  // 允许跨域
public class FileController {
    /**
     * 图片同步到七牛云，将七牛云上的图片链接更新到admin
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    public BaseResponse<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String newFilename = UUID.randomUUID().toString();
        QiniuUtil.upload2Qiniu(file.getBytes(), newFilename);
        String url = "http://rtawoxtcx.hn-bkt.clouddn.com/" + newFilename;
        return ResultUtils.success(url);
    }
}
