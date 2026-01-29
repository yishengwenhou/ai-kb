package com.itpan.backend.model.dto.auth;

import com.itpan.backend.model.vo.TokenVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {
    private TokenVO tokenVo;
}
