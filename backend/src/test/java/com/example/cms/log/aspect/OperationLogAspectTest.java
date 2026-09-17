package com.example.cms.log.aspect;

import com.example.cms.log.annotation.OperationLog;
import com.example.cms.log.repository.LogRepository;
import com.example.cms.user.dto.UserCreateDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationLogAspectTest {

    @Mock
    private LogRepository logRepository;
    @Mock
    private ProceedingJoinPoint joinPoint;
    @Mock
    private MethodSignature signature;
    @Mock
    private OperationLog operationLog;

    @AfterEach
    void cleanUp() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    @Test
    void masksPasswordBeforeSavingOperationLog() throws Throwable {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("tester");
        dto.setNickname("测试用户");
        dto.setPassword("secret123");

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/users");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.getArgs()).thenReturn(new Object[]{dto});
        when(joinPoint.proceed()).thenReturn(null);
        when(signature.getParameterNames()).thenReturn(new String[]{"dto"});
        when(operationLog.value()).thenReturn("新增用户");

        OperationLogAspect aspect = new OperationLogAspect(logRepository, new ObjectMapper());
        aspect.around(joinPoint, operationLog);

        ArgumentCaptor<String> paramsCaptor = ArgumentCaptor.forClass(String.class);
        verify(logRepository).insertOperationLog(
                anyLong(), anyString(), eq("POST"), eq("/users"),
                paramsCaptor.capture(), eq("新增用户"), anyString(), anyLong()
        );
        assertThat(paramsCaptor.getValue()).doesNotContain("secret123");
    }
}
