package com.example.codemind_self.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.codemind_self.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
