package com.itpan.backend.model.dto;

import com.itpan.backend.model.vo.TokenVo;
import jdk.nashorn.internal.parser.Token;

public record RefreshTokenRequest(TokenVo tokenVo) {
}
