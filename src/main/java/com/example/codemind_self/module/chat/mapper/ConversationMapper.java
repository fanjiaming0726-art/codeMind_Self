package com.example.codemind_self.module.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.codemind_self.module.chat.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConversationMapper extends BaseMapper<Conversation> {
}
