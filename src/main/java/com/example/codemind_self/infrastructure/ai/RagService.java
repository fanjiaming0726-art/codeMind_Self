package com.example.codemind_self.infrastructure.ai;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final EmbeddingModel embeddingModel;
    private final MilvusEmbeddingStore milvusEmbeddingStore;

    public void storeChunks(List<String> chunks, Long documentId, Long kbId){
        for(int i = 0; i < chunks.size(); i++){
            String chunk  = chunks.get(i);

            Metadata metadata = Metadata.from("documentId",String.valueOf(documentId));

            metadata.put("kbId",kbId);
            metadata.put("chunkIndex",String.valueOf(i));
            TextSegment textSegment = TextSegment.from(chunk,metadata);

            Embedding embedding = embeddingModel.embed(textSegment).content();

            milvusEmbeddingStore.add(embedding,textSegment);
        }
        log.info("向量化完成，documentId：{}，chunks：{}",documentId,chunks.size());

    }

    /***
     * 问题向量化，建立访问milvus数据库的请求，发送请求得到结果，结果文本化返回
     * @param query
     * @param maxResults
     * @return
     */

    public List<String> search(String query, int maxResults){

        Embedding queryEmbedding = embeddingModel.embed(query).content();
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(maxResults)

                .minScore(0.6)
                .build();
        List<EmbeddingMatch<TextSegment>> ansList = milvusEmbeddingStore.search(request).matches();

        return ansList.stream().map(ans -> ans.embedded().text()).toList();

    }
}
