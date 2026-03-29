package com.example.codemind_self.module.review.service;

import com.example.codemind_self.module.review.entity.CodeReviewVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CodeReviewService {

    CodeReviewVO review(MultipartFile file);

    List<CodeReviewVO> litMine();

    CodeReviewVO getById(Long id);

    void delete(Long id);

}
