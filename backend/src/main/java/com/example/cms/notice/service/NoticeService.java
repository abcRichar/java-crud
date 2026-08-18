package com.example.cms.notice.service;

import com.example.cms.common.enums.ResultCode;
import com.example.cms.common.exception.BusinessException;
import com.example.cms.common.response.PageResult;
import com.example.cms.common.utils.SecurityUtils;
import com.example.cms.notice.dto.NoticeCreateDTO;
import com.example.cms.notice.dto.NoticeUpdateDTO;
import com.example.cms.notice.entity.Notice;
import com.example.cms.notice.repository.NoticeRepository;
import com.example.cms.notice.vo.NoticeVO;
import com.example.cms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    public PageResult<NoticeVO> getPage(String keyword, String type, String status, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Notice> notices = noticeRepository.findPage(keyword, type, status, offset, pageSize);
        long total = noticeRepository.count(keyword, type, status);
        List<NoticeVO> voList = notices.stream().map(this::toVO).toList();
        return PageResult.of(voList, total, page, pageSize);
    }

    public NoticeVO getById(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOTICE_NOT_FOUND));
        return toVO(notice);
    }

    @Transactional
    public Long create(NoticeCreateDTO dto) {
        Notice notice = new Notice();
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setType(dto.getType());
        notice.setStatus("DRAFT");
        notice.setCreatedBy(SecurityUtils.getCurrentUserId());

        Long noticeId = noticeRepository.insert(notice);

        // Pre-assign target users (they'll see the notice once published)
        if (dto.getUserIds() != null && !dto.getUserIds().isEmpty()) {
            noticeRepository.insertNoticeUsers(noticeId, dto.getUserIds());
        }

        return noticeId;
    }

    @Transactional
    public void update(NoticeUpdateDTO dto) {
        Notice existing = noticeRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException(ResultCode.NOTICE_NOT_FOUND));

        Notice notice = new Notice();
        notice.setId(dto.getId());
        notice.setTitle(dto.getTitle());
        notice.setContent(dto.getContent());
        notice.setType(dto.getType() != null ? dto.getType() : existing.getType());
        noticeRepository.update(notice);

        // Reassign target users if provided
        if (dto.getUserIds() != null) {
            noticeRepository.deleteNoticeUsers(dto.getId());
            if (!dto.getUserIds().isEmpty()) {
                noticeRepository.insertNoticeUsers(dto.getId(), dto.getUserIds());
            }
        }
    }

    @Transactional
    public void delete(Long id) {
        noticeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOTICE_NOT_FOUND));
        noticeRepository.deleteNoticeUsers(id);
        noticeRepository.softDelete(id);
    }

    @Transactional
    public void publish(Long id) {
        noticeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOTICE_NOT_FOUND));
        noticeRepository.updateStatus(id, "PUBLISHED");
    }

    @Transactional
    public void withdraw(Long id) {
        noticeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOTICE_NOT_FOUND));
        noticeRepository.updateStatus(id, "WITHDRAWN");
    }

    @Transactional
    public void markAsRead(Long noticeId) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        noticeRepository.markAsRead(noticeId, userId);
    }

    public long countUnread() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) return 0;
        return noticeRepository.countUnread(userId);
    }

    private NoticeVO toVO(Notice notice) {
        NoticeVO vo = new NoticeVO();
        vo.setId(notice.getId());
        vo.setTitle(notice.getTitle());
        vo.setContent(notice.getContent());
        vo.setType(notice.getType());
        vo.setStatus(notice.getStatus());
        vo.setCreatedBy(notice.getCreatedBy());
        vo.setCreatedAt(notice.getCreatedAt());
        vo.setUpdatedAt(notice.getUpdatedAt());
        return vo;
    }
}
