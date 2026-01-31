package com.itpan.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.itpan.backend.model.dto.user.UserQueryDTO;
import com.itpan.backend.model.entity.User;
import com.itpan.backend.model.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    IPage<UserVO> selectUserVoPage(IPage<UserVO> page, @Param("query") UserQueryDTO query);
}