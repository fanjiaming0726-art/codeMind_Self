package com.example.codemind_self.module.knowledge.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.codemind_self.common.result.PageRequest;
import com.example.codemind_self.module.knowledge.entity.KnowledgeBaseDTO;
import com.example.codemind_self.module.knowledge.entity.KnowledgeBaseVO;

import java.util.List;

public interface KnowledgeBaseService {

    KnowledgeBaseVO create(KnowledgeBaseDTO dto);

    KnowledgeBaseVO update(Long id,KnowledgeBaseDTO dto);

    void delete(Long id);

    KnowledgeBaseVO getById(Long id);

    IPage<KnowledgeBaseVO> listMine(PageRequest pageRequest);

}
