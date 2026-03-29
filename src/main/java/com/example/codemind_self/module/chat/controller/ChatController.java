package com.example.codemind_self.module.chat.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.example.codemind_self.common.result.Result;
import com.example.codemind_self.module.chat.entity.ChatMessageVO;
import com.example.codemind_self.module.chat.entity.ChatRequestDTO;
import com.example.codemind_self.module.chat.entity.ConversationVO;
import com.example.codemind_self.module.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    @PostMapping(value = "/stream",produces = "text/event-stream")
    public SseEmitter stream(@Valid @RequestBody ChatRequestDTO dto){
        Long userId = StpUtil.getLoginIdAsLong();
        return chatService.chat(dto,userId);
    }

    @GetMapping("/conversations")
    public Result<List<ConversationVO>> listConversations(@RequestParam Long kbId){
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(chatService.listConversations(kbId,userId));
    }

    @GetMapping("/messages")
    public Result<List<ChatMessageVO>> listMessages(@RequestParam Long convId){
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(chatService.listMessages(convId, userId));
    }

}
