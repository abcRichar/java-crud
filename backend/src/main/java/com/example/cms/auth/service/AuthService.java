package com.example.cms.auth.service;

import com.example.cms.auth.dto.LoginRequest;
import com.example.cms.auth.dto.RefreshTokenRequest;
import com.example.cms.auth.repository.AuthRepository;
import com.example.cms.auth.vo.LoginVO;
import com.example.cms.auth.vo.UserInfoVO;
import com.example.cms.common.enums.ResultCode;
import com.example.cms.common.exception.BusinessException;
import com.example.cms.security.JwtTokenProvider;
import com.example.cms.user.entity.User;
import com.example.cms.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public LoginVO login(LoginRequest request, HttpServletRequest httpRequest) {
        String username = request.getUsername();
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    authRepository.insertLoginLog(username, ip, userAgent, 0, "用户不存在");
                    return new BusinessException(ResultCode.USER_NOT_FOUND);
                });

        if (user.getStatus() != 1) {
            authRepository.insertLoginLog(username, ip, userAgent, 0, "用户已被禁用");
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            authRepository.insertLoginLog(username, ip, userAgent, 0, "密码错误");
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        authRepository.insertLoginLog(username, ip, userAgent, 1, "登录成功");

        UserInfoVO userInfo = buildUserInfoVO(user);
        long expiresIn = jwtTokenProvider.getAccessTokenExpiration() / 1000;

        return new LoginVO(accessToken, refreshToken, expiresIn, userInfo);
    }

    public LoginVO refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ResultCode.REFRESH_TOKEN_INVALID);
        }

        String tokenType = jwtTokenProvider.getTokenTypeFromToken(refreshToken);
        if (!"REFRESH".equals(tokenType)) {
            throw new BusinessException(ResultCode.REFRESH_TOKEN_INVALID);
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        // Verify user still exists and is active
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        UserInfoVO userInfo = buildUserInfoVO(user);
        long expiresIn = jwtTokenProvider.getAccessTokenExpiration() / 1000;

        return new LoginVO(newAccessToken, newRefreshToken, expiresIn, userInfo);
    }

    public UserInfoVO getCurrentUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));
        return buildUserInfoVO(user);
    }

    public void logout() {
        // Stateless JWT: client discards tokens. Server-side blacklist can be added via Redis if needed.
        log.info("User logged out");
    }

    private UserInfoVO buildUserInfoVO(User user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());

        List<String> roleCodes = userRepository.findRoleCodesByUserId(user.getId());
        vo.setRoles(roleCodes);

        List<String> permissions = userRepository.findPermissionsByUserId(user.getId());
        vo.setPermissions(permissions);

        return vo;
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return "unknown";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
