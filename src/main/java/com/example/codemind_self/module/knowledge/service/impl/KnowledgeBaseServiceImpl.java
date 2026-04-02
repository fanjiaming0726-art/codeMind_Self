package com.example.codemind_self.module.knowledge.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.codemind_self.common.exception.BusinessException;
import com.example.codemind_self.common.result.PageRequest;
import com.example.codemind_self.common.result.ResultCode;
import com.example.codemind_self.module.knowledge.entity.KnowledgeBase;
import com.example.codemind_self.module.knowledge.entity.KnowledgeBaseDTO;
import com.example.codemind_self.module.knowledge.entity.KnowledgeBaseVO;
import com.example.codemind_self.module.knowledge.mapper.KnowledgeBaseMapper;
import com.example.codemind_self.module.knowledge.service.KnowledgeBaseService;
import dev.langchain4j.agent.tool.P;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    @Override
    public KnowledgeBaseVO create(KnowledgeBaseDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();

        KnowledgeBase base = new KnowledgeBase();
        base.setName(dto.getName());
        base.setDescription(dto.getDescription());
        base.setUserId(userId);
        base.setDocCount(0);
        knowledgeBaseMapper.insert(base);

        KnowledgeBaseVO vo = new KnowledgeBaseVO();
        BeanUtils.copyProperties(base,vo);
        return vo;
    }

    @Override
    public KnowledgeBaseVO update(Long id, KnowledgeBaseDTO dto) {
        KnowledgeBase base = getAndCheckOwnerBase(id);
        base.setName(dto.getName());
        base.setDescription(dto.getDescription());

        knowledgeBaseMapper.updateById(base);
        KnowledgeBaseVO vo = new KnowledgeBaseVO();
        BeanUtils.copyProperties(base,vo);
        return vo;
    }

    @Override
    public void delete(Long id) {
        KnowledgeBase base = getAndCheckOwnerBase(id);
        knowledgeBaseMapper.deleteById(base);
    }

    @Override
    public KnowledgeBaseVO getById(Long id) {
        KnowledgeBase base = getAndCheckOwnerBase(id);
        KnowledgeBaseVO vo = new KnowledgeBaseVO();
        BeanUtils.copyProperties(base,vo);
        return vo;
    }

    @Override
    public IPage<KnowledgeBaseVO> listMine(PageRequest pageRequest) {
        Long userId = StpUtil.getLoginIdAsLong();
        Page<KnowledgeBase> page = new Page<>(pageRequest.getPageNum(),pageRequest.getPageSize());
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBase::getUserId,userId)
                .orderByDesc(KnowledgeBase::getCreateTime);
        Page<KnowledgeBase> basePage = knowledgeBaseMapper.selectPage(page,wrapper);

        return basePage.convert(base ->{
                    KnowledgeBaseVO vo = new KnowledgeBaseVO();
                    BeanUtils.copyProperties(base,vo);
                    return vo;
                });
    }

    // 针对知识库这种私密数据，需要校验权限
    private KnowledgeBase getAndCheckOwnerBase(Long id){
        Long userId = StpUtil.getLoginIdAsLong();
        KnowledgeBase base = knowledgeBaseMapper.selectById(id);
        if(base == null){
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if(!base.getUserId().equals(userId)){
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return base;
    }
}
