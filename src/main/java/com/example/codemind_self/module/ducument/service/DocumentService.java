package com.example.codemind_self.module.ducument.service;

import com.example.codemind_self.module.ducument.entity.DocumentVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {


    DocumentVO upload(Long kbId, MultipartFile file);

    List<DocumentVO> listByKbId(Long kbId);

    void delete(Long id);

}
