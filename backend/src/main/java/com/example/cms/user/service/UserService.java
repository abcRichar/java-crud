package com.example.cms.user.service;

import com.example.cms.common.enums.ResultCode;
import com.example.cms.common.exception.BusinessException;
import com.example.cms.common.response.PageResult;
import com.example.cms.user.dto.UserCreateDTO;
import com.example.cms.user.dto.UserQueryDTO;
import com.example.cms.user.dto.UserUpdateDTO;
import com.example.cms.user.entity.User;
import com.example.cms.user.repository.UserRepository;
import com.example.cms.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PageResult<UserVO> getPage(UserQueryDTO query) {
        int page = query.getPage() == null || query.getPage() < 1 ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();
        int offset = (page - 1) * pageSize;

        List<User> users = userRepository.findPage(query.getKeyword(), query.getStatus(), offset, pageSize);
        long total = userRepository.count(query.getKeyword(), query.getStatus());

        List<UserVO> voList = users.stream().map(this::toVO).toList();
        return PageResult.of(voList, total, page, pageSize);
    }

    public UserVO getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));
        return toVO(user);
    }

    @Transactional
    public Long create(UserCreateDTO dto) {
        // Check username uniqueness
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail() != null ? dto.getEmail() : "");
        user.setPhone(dto.getPhone() != null ? dto.getPhone() : "");
        user.setAvatar(dto.getAvatar() != null ? dto.getAvatar() : "");
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(1);

        Long userId = userRepository.insert(user);

        // Assign roles
        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            userRepository.insertUserRoles(userId, dto.getRoleIds());
        }

        return userId;
    }

    @Transactional
    public void update(UserUpdateDTO dto) {
        User existing = userRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        User user = new User();
        user.setId(dto.getId());
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : existing.getNickname());
        user.setAvatar(dto.getAvatar() != null ? dto.getAvatar() : existing.getAvatar());
        user.setEmail(dto.getEmail() != null ? dto.getEmail() : existing.getEmail());
        user.setPhone(dto.getPhone() != null ? dto.getPhone() : existing.getPhone());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : existing.getStatus());

        userRepository.update(user);

        // Reassign roles if provided
        if (dto.getRoleIds() != null) {
            userRepository.deleteUserRoles(dto.getId());
            if (!dto.getRoleIds().isEmpty()) {
                userRepository.insertUserRoles(dto.getId(), dto.getRoleIds());
            }
        }
    }

    @Transactional
    public void delete(Long id) {
        userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));
        userRepository.deleteUserRoles(id);
        userRepository.softDelete(id);
    }

    @Transactional
    public void updateStatus(Long id, int status) {
        userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));
        userRepository.updateStatus(id, status);
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));
        String hashed = passwordEncoder.encode(newPassword);
        userRepository.updatePassword(id, hashed);
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());

        List<Long> roleIds = userRepository.findRoleIdsByUserId(user.getId());
        vo.setRoleIds(roleIds);
        vo.setRoleNames(new ArrayList<>(roleIds.stream().map(String::valueOf).toList()));

        return vo;
    }
}
