package com.example.codemind_self.module.review.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.codemind_self.common.exception.BusinessException;
import com.example.codemind_self.common.result.ResultCode;
import com.example.codemind_self.module.review.entity.CodeReview;
import com.example.codemind_self.module.review.entity.CodeReviewVO;
import com.example.codemind_self.module.review.mapper.CodeReviewMapper;
import com.example.codemind_self.module.review.service.CodeReviewService;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CodeReviewServiceImpl implements CodeReviewService {

    private final CodeReviewMapper codeReviewMapper;
    private final OpenAiChatModel chatModel;
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
    public CodeReviewVO review(MultipartFile file) {
        Long userId = StpUtil.getLoginIdAsLong();

        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        String suffix = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")).toLowerCase() : "";
        if(!ALLOWED_SUFFIX.contains(suffix)){
            throw new BusinessException("不支持的文件类型，支持：" + ALLOWED_SUFFIX);
        }
        String code;
        try {
            code = new String(file.getBytes(), StandardCharsets.UTF_8);
            if (code.isBlank()) {
                throw new BusinessException("文件内容为空");
            }
        }catch (Exception e){
            throw new BusinessException("文件读取失败");
        }

        String prompt = buildPrompt(code,fileName);

        String report;
        report = chatModel.generate(prompt);

        int issuesCount = extractIssuesCount(report);

        CodeReview review = new CodeReview();
        review.setUserId(userId);
        review.setFileName(fileName);
        review.setReport(report);
        review.setIssuesCount(issuesCount);

        codeReviewMapper.insert(review);

        return convertToVO(review);

    }

    @Override
    public List<CodeReviewVO> litMine() {
        Long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<CodeReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CodeReview::getUserId, userId)
                .orderByDesc(CodeReview::getCreateTime);
        List<CodeReview> list = codeReviewMapper.selectList(wrapper);
        return list.stream().map(this::convertToVO).toList();
    }

    @Override
    public CodeReviewVO getById(Long id) {
        CodeReview review = getAndCheckOwner(id);
        return convertToVO(review);
    }

    @Override
    public void delete(Long id) {
        getAndCheckOwner(id);
        codeReviewMapper.deleteById(id);
    }

    private CodeReviewVO convertToVO(CodeReview review) {
        CodeReviewVO vo = new CodeReviewVO();
        BeanUtils.copyProperties(review, vo);
        return vo;
    }

    private String buildPrompt(String code,String fileName){
        return """
                你是一个资深的代码审查专家，请对以下代码进行全面审查，
                
                ## 文件名
                %s
                
                ## 代码内容
                
                %s
               
                请按照以下格式输出审查报告：
                
                ### 总体评价
                
                ### 发现的问题
                （逐条列出，每条包含，问题描述，问题严重程度（低/中/高），修改建议）
                
                ### 问题总数
                共发现 X 个问题
              
              
                ### 优化建议
                （给出改进方向）
                
                
                """.formatted(fileName,code);
    }
    private int extractIssuesCount(String report){
        Pattern pattern = Pattern.compile("共发现\\s*(\\d+)\\s*个问题");
        Matcher matcher = pattern.matcher(report);
        if(matcher.find()){
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    private CodeReview getAndCheckOwner(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        CodeReview review = codeReviewMapper.selectById(id);
        if (review == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!review.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return review;
    }
}
