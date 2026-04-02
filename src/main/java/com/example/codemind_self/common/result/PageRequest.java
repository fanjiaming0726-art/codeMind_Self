package com.example.codemind_self.common.result;

import lombok.Data;

@Data
public class PageRequest {

    private Integer pageNum = 1;
    private Integer pageSize = 10;

}
