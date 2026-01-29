package com.itpan.backend.model.dto;

import com.itpan.backend.model.vo.TokenVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {
    private TokenVo tokenVo;
}
