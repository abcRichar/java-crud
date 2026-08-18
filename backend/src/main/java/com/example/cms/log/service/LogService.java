package com.example.cms.log.service;

import com.example.cms.common.response.PageResult;
import com.example.cms.log.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    public PageResult<Map<String, Object>> getLoginLogs(String keyword, Integer status, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Map<String, Object>> records = logRepository.findLoginLogs(keyword, status, offset, pageSize);
        long total = logRepository.countLoginLogs(keyword, status);
        return PageResult.of(records, total, page, pageSize);
    }

    public PageResult<Map<String, Object>> getOperationLogs(String keyword, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Map<String, Object>> records = logRepository.findOperationLogs(keyword, offset, pageSize);
        long total = logRepository.countOperationLogs(keyword);
        return PageResult.of(records, total, page, pageSize);
    }
}
