package com.example.codemind_self.module.user.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.codemind_self.common.exception.BusinessException;
import com.example.codemind_self.common.result.ResultCode;
import com.example.codemind_self.module.user.entity.LoginDTO;
import com.example.codemind_self.module.user.entity.RegisterDTO;
import com.example.codemind_self.module.user.entity.User;
import com.example.codemind_self.module.user.entity.UserVO;
import com.example.codemind_self.module.user.mapper.UserMapper;
import com.example.codemind_self.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    // 采用构造器注入的模式，可以确保该字段不可变
    private final UserMapper userMapper;

    @Override
    public void Register(RegisterDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,dto.getUsername());
        Long count = userMapper.selectCount(wrapper);
        if(count > 0){
            throw new BusinessException("用户已存在");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setEmail(dto.getEmail());
        userMapper.insert(user);
    }

    @Override
    public String login(LoginDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,dto.getUsername());
        User user = userMapper.selectOne(wrapper);
        if(user == null){
            throw new BusinessException("用户不存在");
        }
        if(!BCrypt.checkpw(dto.getPassword(),user.getPassword())){
            throw new BusinessException("密码错误");
        }
        StpUtil.login(user.getId());
        return StpUtil.getTokenValue();

    }

    @Override
    public UserVO getCurrentUser() {
        Long id = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(id);
        if(user == null){
            throw new BusinessException(ResultCode.NOT_FOUND);

        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);
        return userVO;
    }
}
