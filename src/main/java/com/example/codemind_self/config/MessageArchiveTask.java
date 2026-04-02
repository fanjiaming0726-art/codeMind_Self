package com.example.codemind_self.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.codemind_self.module.chat.entity.ChatMessage;
import com.example.codemind_self.module.chat.entity.ChatMessageArchive;
import com.example.codemind_self.module.chat.mapper.ChatMessageArchiveMapper;
import com.example.codemind_self.module.chat.mapper.ChatMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageArchiveTask {

    private final ChatMessageMapper chatMessageMapper;
    private final ChatMessageArchiveMapper chatMessageArchiveMapper;

    @Scheduled(cron = "0 0 2 * * ?")
    public void archiveOldMessage(){

        LocalDateTime threshold = LocalDateTime.now().minusDays(30);

        List<ChatMessage> oldMessages = chatMessageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>().lt(ChatMessage::getCreateTime,threshold)
        );

        if(oldMessages.isEmpty()){
            return;
        }

        for(ChatMessage msg : oldMessages){
            ChatMessageArchive archive = new ChatMessageArchive();
            BeanUtils.copyProperties(msg,archive);
            chatMessageArchiveMapper.insert(archive);
        }

        chatMessageMapper.delete(new LambdaQueryWrapper<ChatMessage>().lt(ChatMessage::getCreateTime,threshold));
        log.info("建档完成，迁移{}条信息",oldMessages.size());


    }


}
