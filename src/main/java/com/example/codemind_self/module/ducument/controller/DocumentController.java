package com.example.codemind_self.module.ducument.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.example.codemind_self.common.result.Result;
import com.example.codemind_self.module.ducument.entity.DocumentVO;
import com.example.codemind_self.module.ducument.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
@SaCheckLogin
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload/{kbId}")
    public Result<DocumentVO> upload(@PathVariable Long kbId, @RequestParam("file") MultipartFile file){
        return Result.success(documentService.upload(kbId,file));
    }

    @GetMapping("/list/{kbId}")
    public Result<List<DocumentVO>> list(@PathVariable Long kbId){
        return Result.success(documentService.listByKbId(kbId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id){
        documentService.delete(id);
        return Result.success();
    }
}
