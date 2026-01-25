package com.itpan.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itpan.backend.model.entity.User;

public interface UserService extends IService<User> {
    User getUserByUsername(String username);

    User updateUser(User user);

    Boolean deleteUser(Long id);

    IPage<User> getPageList(String keyword, Integer pageNum, Integer pageSize);
}