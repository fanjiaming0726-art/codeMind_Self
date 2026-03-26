package com.example.codemind_self.module.knowledge.controller;


import cn.dev33.satoken.annotation.SaCheckLogin;
import com.example.codemind_self.common.result.Result;
import com.example.codemind_self.module.knowledge.entity.KnowledgeBaseDTO;
import com.example.codemind_self.module.knowledge.entity.KnowledgeBaseVO;
import com.example.codemind_self.module.knowledge.service.KnowledgeBaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@SaCheckLogin
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    @PostMapping("/create")
    public Result<KnowledgeBaseVO> create(@Valid @RequestBody KnowledgeBaseDTO dto){
        return Result.success(knowledgeBaseService.create(dto));
    }

    @GetMapping
    public Result<List<KnowledgeBaseVO>> listMine(){
        return Result.success(knowledgeBaseService.listMine());
    }


    @PutMapping("/{id}")
    public Result<KnowledgeBaseVO> update(@PathVariable Long id, @Valid @RequestBody KnowledgeBaseDTO dto){
        return Result.success(knowledgeBaseService.update(id,dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id){
        knowledgeBaseService.delete(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<KnowledgeBaseVO> getById(@PathVariable Long id){
        return Result.success(knowledgeBaseService.getById(id));
    }


}
