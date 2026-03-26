package com.example.codemind_self.module.user.entity;

import lombok.Data;
import java.time.LocalDateTime;

// 把UserVO转换为前端的json字符串时需要调用Getter方法
@Data
public class UserVO {

    private Long id;
    private String username;
    private String email;
    private String avatar;
    private LocalDateTime createTime;
}
