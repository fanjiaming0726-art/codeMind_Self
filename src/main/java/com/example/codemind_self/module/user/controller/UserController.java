package com.example.codemind_self.module.user.controller;

import com.example.codemind_self.common.result.Result;
import com.example.codemind_self.module.user.entity.LoginDTO;
import com.example.codemind_self.module.user.entity.RegisterDTO;
import com.example.codemind_self.module.user.entity.UserVO;
import com.example.codemind_self.module.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto){
        userService.Register(dto);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginDTO dto){
        String token = userService.login(dto);
        return Result.success(token);
    }

    @PostMapping("/getCurrentUser")
    public Result<UserVO> getCurrentUser(){
        UserVO userVO = userService.getCurrentUser();
        return Result.success(userVO);
    }
}
