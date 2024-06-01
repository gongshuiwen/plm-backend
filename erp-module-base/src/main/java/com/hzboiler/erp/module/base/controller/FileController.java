package com.hzboiler.erp.module.base.controller;

import com.hzboiler.erp.core.protocal.Result;
import com.hzboiler.erp.module.base.storage.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Tag(name = "文件接口")
@RequestMapping("/file")
public class FileController {

    @Autowired
    private StorageService storageService;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<String> upload(@RequestBody MultipartFile file) throws Exception {
        return Result.success(storageService.store(file));
    }

    @Operation(summary = "下载文件")
    @GetMapping("/download/{name}")
    public Resource download(@PathVariable String name) throws Exception {
        return storageService.loadAsResource(name);
    }
}
