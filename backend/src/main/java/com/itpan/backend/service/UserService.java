package com.itpan.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itpan.backend.model.dto.PasswordChangeDTO;
import com.itpan.backend.model.dto.user.UserCreateDTO;
import com.itpan.backend.model.dto.user.UserQueryDTO;
import com.itpan.backend.model.dto.user.UserUpdateAdminDTO;
import com.itpan.backend.model.dto.user.UserUpdateDTO;
import com.itpan.backend.model.entity.User;
import com.itpan.backend.model.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

public interface UserService extends IService<User> {
    User getUserByUsername(String username);

    /**
     * 管理员更新用户信息
     */
    User updateUserByAdmin(UserUpdateAdminDTO updateDTO);

    Boolean deleteUser(Long id);

    IPage<UserVO> getPageList(UserQueryDTO userQueryDTO);

    User createUser(UserCreateDTO userCreateDTO);

    boolean updatePassword(PasswordChangeDTO passwordDTO);

    boolean updateProfile(UserUpdateDTO updateDTO);
}