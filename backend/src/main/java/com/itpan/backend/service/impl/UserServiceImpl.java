package com.itpan.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itpan.backend.mapper.UserMapper;
import com.itpan.backend.model.entity.User;
import com.itpan.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final UserMapper userMapper;
    @Override
    public User getUserByUsername(String username) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        return user;
    }

    @Override
    public User updateUser(User user) {
        baseMapper.updateById(user);
        return user;
    }

    @Override
    public Boolean deleteUser(Long id) {
        return baseMapper.deleteById(id)>0;
    }
}