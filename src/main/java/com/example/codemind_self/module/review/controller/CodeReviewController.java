package com.example.codemind_self.module.review.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.example.codemind_self.common.result.Result;
import com.example.codemind_self.module.review.entity.CodeReviewVO;
import com.example.codemind_self.module.review.service.CodeReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
@SaCheckLogin
public class CodeReviewController {

    private final CodeReviewService codeReviewService;

    @PostMapping("/review")
    public Result<CodeReviewVO> review(@RequestParam MultipartFile file){
        return Result.success(codeReviewService.review(file));
    }

    @GetMapping("/list")
    public Result<List<CodeReviewVO>> listMine(){
        return Result.success(codeReviewService.litMine());
    }

    @GetMapping("/{id}")
    public Result<CodeReviewVO> getById(@PathVariable Long id){
        return Result.success(codeReviewService.getById(id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id){
        codeReviewService.delete(id);
        return Result.success();
    }

}
