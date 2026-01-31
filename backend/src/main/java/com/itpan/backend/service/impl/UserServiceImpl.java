package com.itpan.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itpan.backend.common.constants.UserConstant;
import com.itpan.backend.mapper.DepartmentMapper;
import com.itpan.backend.mapper.UserMapper;
import com.itpan.backend.model.dto.PasswordChangeDTO;
import com.itpan.backend.model.dto.user.UserCreateDTO;
import com.itpan.backend.model.dto.user.UserQueryDTO;
import com.itpan.backend.model.dto.user.UserUpdateAdminDTO;
import com.itpan.backend.model.dto.user.UserUpdateDTO;
import com.itpan.backend.model.entity.User;
import com.itpan.backend.model.vo.UserVO;
import com.itpan.backend.service.UserService;
import com.itpan.backend.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final UserMapper userMapper;
    private final DepartmentMapper departmentMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getUserByUsername(String username) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        return user;
    }

    @Override
    public User updateUserByAdmin(UserUpdateAdminDTO updateDTO) {
        User existingUser = baseMapper.selectById(updateDTO.getId());
        if (existingUser == null) {
            return null;
        }

        User user1 = User.builder()
                .id(updateDTO.getId())
                .username(updateDTO.getUsername())
                .phone(updateDTO.getPhone())
                .email(updateDTO.getEmail())
                .gender(updateDTO.getGender())
                .realName(updateDTO.getRealName())
                .avatarUrl(updateDTO.getAvatarUrl())
                .deptId(updateDTO.getDeptId())
                .status(updateDTO.getStatus())
                .build();

        baseMapper.updateById(user1);
        return user1;
    }

    @Override
    public Boolean deleteUser(Long id) {
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    public IPage<UserVO> getPageList(UserQueryDTO userQueryDTO) {
        IPage<UserVO> page = new Page<>(userQueryDTO.getPageNum(), userQueryDTO.getPageSize());
        IPage<UserVO> userVOIPage = baseMapper.selectUserVoPage(page, userQueryDTO);
        return userVOIPage;
    }

    @Override
    public User createUser(UserCreateDTO userCreateDTO) {
        User user = User.builder()
                .username(userCreateDTO.getUsername())
                .phone(userCreateDTO.getPhone())
                .email(userCreateDTO.getEmail())
                .gender(userCreateDTO.getGender())
                .password(passwordEncoder.encode(UserConstant.BASE_PASSWORD))
                .realName(userCreateDTO.getRealName())
                .deptId(userCreateDTO.getDeptId())
                .status(userCreateDTO.getStatus())
                .avatarUrl(userCreateDTO.getAvatarUrl())
                .build();
        baseMapper.insert(user);
        return user;
    }

    @Override
    public boolean updatePassword(PasswordChangeDTO passwordDTO) {
        Long currentUserId = UserContext.getUserId();
        User user =baseMapper.selectById(currentUserId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 验证原密码
        if (!passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword())) {
           throw new RuntimeException("原密码不正确");
        }

        // 更新新密码
        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        return baseMapper.updateById(user) > 0;

    }

    @Override
    public boolean updateProfile(UserUpdateDTO updateDTO) {
        Long currentUserId = UserContext.getUserId();
        User user = baseMapper.selectById(currentUserId);
        if (user == null) {
           throw new RuntimeException("用户不存在");
        }

        User user1 = User.builder()
                .id(currentUserId)
                .phone(user.getPhone())
                .email(user.getEmail())
                .gender(user.getGender())
                .realName(updateDTO.getRealName())
                .avatarUrl(updateDTO.getAvatarUrl())
                .build();

        return baseMapper.updateById(user1) > 0;
    }
}