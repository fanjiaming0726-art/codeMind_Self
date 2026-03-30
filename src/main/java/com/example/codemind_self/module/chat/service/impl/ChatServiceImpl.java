package com.example.codemind_self.module.chat.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.codemind_self.common.constant.RedisConstant;
import com.example.codemind_self.common.exception.BusinessException;
import com.example.codemind_self.infrastructure.ai.RagService;
import com.example.codemind_self.infrastructure.redis.CacheService;
import com.example.codemind_self.infrastructure.redis.RedisService;
import com.example.codemind_self.module.chat.entity.*;
import com.example.codemind_self.module.chat.mapper.ChatMessageMapper;
import com.example.codemind_self.module.chat.mapper.ConversationMapper;
import com.example.codemind_self.module.chat.service.ChatService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.output.Response;
import io.grpc.internal.JsonUtil;
import kotlin.jvm.internal.Lambda;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationMapper conversationMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final RagService ragService;
    private final RedisService redisService;
    private final CacheService cacheService;
    private final OpenAiStreamingChatModel streamingChatModel;

    @Override
    public SseEmitter chat(ChatRequestDTO dto, Long userId) {
         String rateLimitKey = RedisConstant.RATE_LIMIT_PREFIX + userId;
         if(redisService.isRateLimited(rateLimitKey,
                 RedisConstant.RATE_LIMIT_COUNT,
                 RedisConstant.RATE_LIMIT_WINDOW)){
             throw new BusinessException("请求过于频繁，请稍后再试");
         }

         String cacheKey = RedisConstant.CHAT_CACHE_PREFIX + dto.getKbId() + ":" + dto.getQuestion().hashCode();

         SseEmitter emitter = new SseEmitter(3 * 60 * 1000L);

         String cached = cacheService.getWithLock(cacheKey,
                 RedisConstant.randomTtl(RedisConstant.CHAT_CACHE_TTL),
                 () -> null
         );

         Conversation conv = getOrCreateConversation(dto,userId);

         if(cached != null){
             try {
                 emitter.send(SseEmitter.event().data(cached));
                 emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                 emitter.complete();
                 saveMessage(conv.getId(), "user",dto.getQuestion(),0);
                 saveMessage(conv.getId(),"assistant",cached,0);
             }catch (Exception e){
                 emitter.completeWithError(e);
             }
             return emitter;

         }
         List<String> relevantChunks = ragService.search(dto.getQuestion(),5);

         String prompt = buildPrompt(dto.getQuestion(),relevantChunks);

         saveMessage(conv.getId(),"user",dto.getQuestion(),0);

         StringBuilder fullResponse = new StringBuilder();
         streamingChatModel.generate(
                 UserMessage.from(prompt),
                 new StreamingResponseHandler<AiMessage>() {
                     @Override
                     public void onNext(String token) {
                         try {
                             emitter.send(SseEmitter.event().data(token));
                             fullResponse.append(token);
                         }catch (IOException e){
                             log.error("SSE发送失败",e);
                             emitter.completeWithError(e);
                         }
                     }

                     @Override
                     public void onError(Throwable error) {
                         log.error("大模型调用失败",error);
                         emitter.completeWithError(error);
                     }

                     @Override
                     public void onComplete(Response<AiMessage> response) {
                         try {
                             emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                             emitter.complete();
                             String answer = fullResponse.toString();
                             saveMessage(conv.getId(), "assistant", answer, 0);
                             redisService.set(cacheKey, answer, RedisConstant.CHAT_CACHE_TTL);
                             cacheService.putLocal(cacheKey,answer);
                         }catch (IOException e){
                             emitter.completeWithError(e);
                         }
                     }
                 }
         );
         return emitter;


    }

    @Override
    public List<ConversationVO> listConversations(Long kbId, Long userId) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getKbId,kbId)
                .eq(Conversation::getUserId,userId)
                .orderByDesc(Conversation::getCreateTime);
        List<Conversation> list = conversationMapper.selectList(wrapper);
        return list.stream().map(conv ->{
            ConversationVO vo = new ConversationVO();
            BeanUtils.copyProperties(conv,vo);
            return vo;
        }).toList();
    }

    @Override
    public List<ChatMessageVO> listMessages(Long convId, Long userId) {
        String contextKey = RedisConstant.CHAT_CONTEXT_PREFIX + convId;
        String cached = cacheService.getWithMutiLevel(contextKey,
                RedisConstant.CHAT_CONTEXT_TTL,
                ()->{
                    LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(ChatMessage::getConvId,convId)
                            .orderByDesc(ChatMessage::getCreateTime);
                    List<ChatMessage> list = chatMessageMapper.selectList(wrapper);
                    List<ChatMessageVO> volist = list.stream().map(conv ->{
                        ChatMessageVO vo = new ChatMessageVO();
                        BeanUtils.copyProperties(conv,vo);
                        return vo;
                    }).toList();
                    return JSONUtil.toJsonStr(volist);
                }

        );

        if(cached == null){
            return Collections.emptyList();
        }

        return JSONUtil.toList(cached,ChatMessageVO.class);
    }

    private Conversation getOrCreateConversation(ChatRequestDTO dto,Long userId){
        if(dto.getConvId() != null){
            Conversation conv = conversationMapper.selectById(dto.getConvId());
            if(conv == null){
                throw new BusinessException("会话不存在");
            }
            return conv;
        }
        Conversation conv = new Conversation();
        conv.setUserId(userId);
        conv.setKbId(dto.getKbId());
        String title = dto.getQuestion().length() > 20 ? dto.getQuestion().substring(0,20) + "..." : dto.getQuestion();
        conv.setTitle(title);

        conversationMapper.insert(conv);
        return conv;
    }

    private void saveMessage(Long convId,String role,String content, int tokens){
        ChatMessage msg = new ChatMessage();
        msg.setContent(content);
        msg.setRole(role);
        msg.setTokens(tokens);
        msg.setConvId(convId);
        chatMessageMapper.insert(msg);

        // 删除两级缓存
        cacheService.evict(RedisConstant.CHAT_CONTEXT_PREFIX + convId);
    }

    private String buildPrompt(String question, List<String> chunks){

        StringBuilder context = new StringBuilder();
        for(int i = 0; i < chunks.size(); i++){
            context.append("### 代码块").append(i + 1).append("\n");
            context.append(chunks.get(i)).append("\n\n");

        }

        return """
                你是一个代码分析助手，请基于以下代码上下文回答用户的问题。
                如果上下文中没有相关信息，请如实说明，不要编造
                
                
                ## 代码上下文
                %s
                
                
                ## 用户问题
                %s
                
                
                
                """.formatted(context,question);
    }

}
