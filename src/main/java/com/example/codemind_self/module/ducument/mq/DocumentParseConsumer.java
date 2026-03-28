package com.example.codemind_self.module.ducument.mq;

import com.example.codemind_self.common.constant.MqConstant;
import com.example.codemind_self.infrastructure.ai.RagService;
import com.example.codemind_self.infrastructure.minio.MinioService;
import com.example.codemind_self.module.ducument.entity.Document;
import com.example.codemind_self.module.ducument.entity.DocumentStatusEnum;
import com.example.codemind_self.module.ducument.mapper.DocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component // 想要使得@RocketMQMessageListener生效，前提是这个类必须是Bean，
@RequiredArgsConstructor
@RocketMQMessageListener(topic = MqConstant.DOCUMENT_TOPIC,consumerGroup = MqConstant.DOCUMENT_CONSUMER_GROUP)
public class DocumentParseConsumer implements RocketMQListener<DocumentParseMessage> {

    private final MinioService minioService;
    private final RagService ragService;
    private final DocumentMapper documentMapper;

    private static final int CHUNK_SIZE = 50;

    @Override
    public void onMessage(DocumentParseMessage message) {
        Long documentId = message.getDocumentId();

        try {
            log.info("收到解析文档消息：{}", message);


            String content = minioService.downloadAsString(message.getObjectName());

            List<String> chunks = splitIntoChunks(content, CHUNK_SIZE);

            ragService.storeChunks(chunks, documentId, message.getKbId());

            Document doc = new Document();
            doc.setId(documentId);
            doc.setStatus(DocumentStatusEnum.READY.getCode());
            doc.setChunkCount(chunks.size());
            documentMapper.updateById(doc);
            log.info("文档解析完成, documentId: {}, chunks: {}", documentId, chunks.size());
        }catch (Exception e){
            log.error("文档解析失败, documentId: {}", documentId, e);
            Document doc = new Document();
            doc.setId(documentId);
            doc.setStatus(DocumentStatusEnum.FAILED.getCode());
            documentMapper.updateById(doc);
        }

    }

    /**
     * 分块策略：按照50行为一组来切
     * @param content
     * @param chunkSize
     * @return
     */
    private List<String> splitIntoChunks(String content,int chunkSize){
        String[] lines = content.split("\n");
        List<String> chunks = new ArrayList<>();
        StringBuffer chunk = new StringBuffer();
        int lineCount = 0;
        for(int i = 0; i < lines.length; i++){

            chunk.append(lines[i]).append("\n");
            lineCount ++;
            if(lineCount >= chunkSize){
                chunks.add(chunk.toString());
                lineCount = 0;
            }

        }
        if(lineCount > 0){
            chunks.add(chunk.toString());
        }
        return chunks;


    }
}
