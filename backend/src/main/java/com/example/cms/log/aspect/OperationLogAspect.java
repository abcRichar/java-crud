package com.example.cms.log.aspect;

import com.example.cms.common.utils.SecurityUtils;
import com.example.cms.log.annotation.OperationLog;
import com.example.cms.log.repository.LogRepository;
import com.example.cms.security.SecurityUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final LogRepository logRepository;
    private final ObjectMapper objectMapper;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long costTime = System.currentTimeMillis() - startTime;

        try {
            saveLog(joinPoint, operationLog, costTime);
        } catch (Exception e) {
            log.warn("Failed to save operation log: {}", e.getMessage());
        }

        return result;
    }

    private void saveLog(ProceedingJoinPoint joinPoint, OperationLog operationLog, long costTime) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return;

        HttpServletRequest request = attributes.getRequest();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Long userId = SecurityUtils.getCurrentUserId();
        String username = SecurityUtils.getCurrentUsername();

        // Build params
        String params = buildParams(joinPoint, signature);

        String operation = operationLog.value().isEmpty()
                ? signature.getDeclaringType().getSimpleName() + "." + signature.getName()
                : operationLog.value();

        String ip = getClientIp(request);

        logRepository.insertOperationLog(
                userId != null ? userId : 0,
                username != null ? username : "anonymous",
                request.getMethod(),
                request.getRequestURI(),
                params,
                operation,
                ip,
                costTime
        );
    }

    private String buildParams(ProceedingJoinPoint joinPoint, MethodSignature signature) {
        try {
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            if (paramNames == null || paramNames.length == 0) return "";

            Map<String, Object> paramMap = new HashMap<>();
            for (int i = 0; i < paramNames.length; i++) {
                Object arg = args[i];
                if (arg instanceof HttpServletRequest || arg instanceof MultipartFile) {
                    continue;
                }
                paramMap.put(paramNames[i], arg);
            }

            String json = objectMapper.writeValueAsString(paramMap);
            // Truncate to prevent overly long params
            return json.length() > 2000 ? json.substring(0, 2000) + "..." : json;
        } catch (Exception e) {
            return "parse error: " + e.getMessage();
        }
    }

    private String getClientIp(HttpServletRequest request) {
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
        return ip != null ? ip : "unknown";
    }
}
