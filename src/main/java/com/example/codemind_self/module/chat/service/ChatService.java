package com.example.codemind_self.module.chat.service;

import com.example.codemind_self.module.chat.entity.ChatMessageVO;
import com.example.codemind_self.module.chat.entity.ChatRequestDTO;
import com.example.codemind_self.module.chat.entity.ConversationVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface ChatService {

    SseEmitter chat(ChatRequestDTO dto,Long userId);
    List<ConversationVO> listConversations(Long kbId,Long userId);

    List<ChatMessageVO> listMessages(Long convId,Long userId);

}
