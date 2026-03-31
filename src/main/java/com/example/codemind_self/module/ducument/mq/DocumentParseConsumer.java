package com.example.codemind_self.module.ducument.mq;

import com.example.codemind_self.common.constant.MqConstant;
import com.example.codemind_self.infrastructure.ai.RagService;
import com.example.codemind_self.infrastructure.minio.MinioService;
import com.example.codemind_self.module.ducument.entity.Document;
import com.example.codemind_self.module.ducument.entity.DocumentStatusEnum;
import com.example.codemind_self.module.ducument.mapper.DocumentMapper;
import io.reactivex.rxjava3.core.Completable;
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

    private static final int CHUNK_SIZE = 50; // 非Java文件的默认切块行数

    @Override
    public void onMessage(DocumentParseMessage message) {
        Long documentId = message.getDocumentId();

        try {
            log.info("收到解析文档消息：{}", message);


            String content = minioService.downloadAsString(message.getObjectName());

            List<String> chunks = splitIntoChunks(content, message.getObjectName());

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
     * @param fileName
     * @return
     */
    private List<String> splitIntoChunks(String content,String fileName){
        if(fileName != null && fileName.endsWith(".java")){
            return splitJavaByMethod(content);
        }
        return splitByLines(content,CHUNK_SIZE);


    }
    private List<String> splitJavaByMethod(String content){
        List<String> chunks = new ArrayList<>();
        String[] lines = content.split("\n");

        StringBuilder header = new StringBuilder();
        StringBuilder currentMethod = new StringBuilder();
        int braceCount = 0;
        boolean inMethod = false;
        boolean headerDone = false;

        for(String line : lines){
           String trimmed = line.trim();
           if(!headerDone){
               // 包名，package，import，注解
               if(trimmed.startsWith("package: ") || trimmed.startsWith("import ") || trimmed.startsWith("@") || trimmed.isEmpty()){
                   header.append(line).append("\n");
               }

               // 类声明
               if(trimmed.startsWith("class ") || trimmed.startsWith("interface ") || trimmed.startsWith("enum ")){
                   header.append(line).append("\n");
                   if(trimmed.contains("{")){
                       headerDone = true;
                   }
               }

           }
            if(!inMethod && isMethodSignature(trimmed)){
                inMethod = true;
                currentMethod = new StringBuilder();
                currentMethod.append(line).append("\n");
                if(trimmed.endsWith(";")){
                    chunks.add(header.toString() + currentMethod.toString());
                    inMethod = false;
                    continue;
                }
                braceCount = countBraces(line);

                if(braceCount == 0 && trimmed.endsWith("}")){
                    chunks.add(header.toString() + currentMethod.toString());
                    inMethod = false;
                }
                continue;
            }
            if(inMethod){
                currentMethod.append(line).append("\n");
                braceCount += countBraces(line);


                if(braceCount <= 0 && trimmed.contains("}")){
                    chunks.add(header.toString() + currentMethod.toString());
                    inMethod = false;
                    currentMethod = new StringBuilder();
                }
            }else{
                if(!trimmed.isEmpty() && !trimmed.equals("}")){
                    header.append(line).append("\n");
                }
            }
        }
        if(inMethod && !currentMethod.isEmpty()){
            chunks.add(header.toString() + currentMethod.toString());
        }
        if(chunks.isEmpty()){
            chunks.add(content);
        }
        return chunks;

    }
    private boolean isMethodSignature(String line){
        if(line.startsWith("//") || line.startsWith("*") || line.startsWith("/*")){
            return false;
        }

        return line.matches(".*(public|private|protected|static)\\s+.*\\(.*\\).*") || line.matches("\\s*\\w+\\s+\\w+\\s*\\(.*\\).*");
    }

    private int countBraces(String line){
        int count = 0;
        boolean inString = false;
        boolean inChar = false;
        for(int i = 0; i < line.length(); i++){
            char c = line.charAt(i);
            if(c == '"' && !inChar) inString = !inString;
            if(c == '\'' && !inString) inChar = !inChar;
            if(!inString && !inChar){
                if(c == '{') count++;
                if(c == '}') count--;
            }
        }
        return count;

    }


    // 按固定行数切块（非Java文件用）
    private List<String> splitByLines(String content, int chunkSize) {
        String[] lines = content.split("\n");
        List<String> chunks = new ArrayList<>();
        StringBuilder chunk = new StringBuilder();
        int lineCount = 0;

        for (String line : lines) {
            chunk.append(line).append("\n");
            lineCount++;
            if (lineCount >= chunkSize) {
                chunks.add(chunk.toString());
                chunk = new StringBuilder();
                lineCount = 0;
            }
        }
        if (!chunk.isEmpty()) {
            chunks.add(chunk.toString());
        }
        return chunks;
    }

}
