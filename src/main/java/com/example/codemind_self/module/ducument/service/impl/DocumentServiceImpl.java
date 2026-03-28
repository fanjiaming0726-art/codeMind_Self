package com.example.codemind_self.module.ducument.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.codemind_self.common.constant.MqConstant;
import com.example.codemind_self.common.exception.BusinessException;
import com.example.codemind_self.common.result.ResultCode;
import com.example.codemind_self.infrastructure.minio.MinioService;
import com.example.codemind_self.module.ducument.entity.Document;
import com.example.codemind_self.module.ducument.entity.DocumentStatusEnum;
import com.example.codemind_self.module.ducument.entity.DocumentVO;
import com.example.codemind_self.module.ducument.mapper.DocumentMapper;
import com.example.codemind_self.module.ducument.mq.DocumentParseMessage;
import com.example.codemind_self.module.ducument.service.DocumentService;
import com.example.codemind_self.module.knowledge.mapper.KnowledgeBaseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final MinioService minioService;
    private final DocumentMapper documentMapper;
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    private final RocketMQTemplate rocketMQTemplate;

    private static final List<String> ALLOWED_SUFFIX = Arrays.asList(
            // 主流语言
            ".java", ".py", ".js", ".ts", ".go", ".c", ".cpp", ".cs",
            ".rb", ".rs", ".kt", ".swift", ".scala", ".php",
            // 前端相关
            ".html", ".css", ".scss", ".vue", ".jsx", ".tsx",
            // 配置和脚本
            ".xml", ".yml", ".yaml", ".json", ".sql", ".sh", ".bat",
            // 文档（保留，README 等也是代码仓库的一部分）
            ".md", ".txt"
    );

    @Override
    public DocumentVO upload(Long kbId, MultipartFile file) {
        Long userId = StpUtil.getLoginIdAsLong();
        if(knowledgeBaseMapper.selectById(kbId) == null){
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        String originalFilename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String suffix = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase() : "";
        if(!ALLOWED_SUFFIX.contains(suffix)){
            throw new BusinessException("不支持的文件类型，支持：" + ALLOWED_SUFFIX);
        }

        String objectName = minioService.upload(file);

        Document doc = new Document();
        doc.setKbId(kbId);
        doc.setUserId(userId);
        doc.setFileName(file.getOriginalFilename());
        doc.setFileUrl(objectName);
        doc.setFileSize(file.getSize());
        doc.setStatus(DocumentStatusEnum.PARSING.getCode());
        doc.setChunkCount(0);

        documentMapper.insert(doc);

        DocumentParseMessage parseMessage = new DocumentParseMessage();
        parseMessage.setDocumentId(doc.getId());
        parseMessage.setKbId(kbId);
        parseMessage.setObjectName(objectName);
        parseMessage.setUserId(userId);
        rocketMQTemplate.convertAndSend(MqConstant.DOCUMENT_TOPIC,parseMessage);
        log.info("文档解析消息已发送，document：{}",doc.getId());
        return convertToVO(doc);

    }

    @Override
    public List<DocumentVO> listByKbId(Long kbId) {
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Document::getKbId,kbId)
                .orderByDesc(Document::getCreateTime);
        List<Document> documents = documentMapper.selectList(wrapper);
        return documents.stream().map(this::convertToVO).toList();
    }

    @Override
    public void delete(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        if(documentMapper.selectById(id) == null){
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if(!documentMapper.selectById(id).getUserId().equals(userId)){
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        documentMapper.deleteById(id);

    }

    private DocumentVO convertToVO(Document doc){

        DocumentVO vo = new DocumentVO();
        BeanUtils.copyProperties(doc,vo);
        vo.setStatusDesc(DocumentStatusEnum.getDesc(doc.getStatus()));
        vo.setFileUrl(minioService.getPresignedUrl(doc.getFileUrl()));

        return vo;
    }

}
