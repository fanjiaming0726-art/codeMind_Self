package com.example.codemind_self.module.user.service;


import com.example.codemind_self.module.user.entity.LoginDTO;
import com.example.codemind_self.module.user.entity.RegisterDTO;
import com.example.codemind_self.module.user.entity.UserVO;

public interface UserService {

    void Register(RegisterDTO dto);

    String login(LoginDTO dto);

    UserVO getCurrentUser();

}
