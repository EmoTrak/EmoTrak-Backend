package com.example.emotrak.Service;

import com.example.emotrak.dto.ReportResponseDto;

import com.example.emotrak.entity.Daily;
import com.example.emotrak.entity.User;
import com.example.emotrak.entity.UserRoleEnum;
import com.example.emotrak.exception.CustomErrorCode;
import com.example.emotrak.exception.CustomException;
import com.example.emotrak.repository.AdminRepository;
import com.example.emotrak.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {
    private final AdminRepository adminRepository;
    private final BoardRepository boardRepository;

    //신고 게시글 조회
    public List<ReportResponseDto> reportBoard(User user) {
        if (user.getRole() != UserRoleEnum.ADMIN) {
            throw new CustomException(CustomErrorCode.UNAUTHORIZED_ACCESS);
        }

        List<Object[]> objectList = adminRepository.getReportBoard();

        List<ReportResponseDto> reportBoardResponseDtoList = new ArrayList<>();
        for (Object[] object : objectList) {
            ReportResponseDto reportResponseDto = new ReportResponseDto(object);
            reportBoardResponseDtoList.add(reportResponseDto);
        }
        return reportBoardResponseDtoList;
    }

    public List<ReportResponseDto> reportComment(User user) {
        if (user.getRole() != UserRoleEnum.ADMIN) {
            throw new CustomException(CustomErrorCode.UNAUTHORIZED_ACCESS);
        }
        List<Object[]> objectList = adminRepository.getReportComment();

        List<ReportResponseDto> reportBoardResponseDtoList = new ArrayList<>();

        for (Object[] object : objectList) {
            ReportResponseDto reportResponseDto = new ReportResponseDto(object);
            reportBoardResponseDtoList.add(reportResponseDto);
        }
        return reportBoardResponseDtoList;
    }

    public void restrictBoard(Long boardId, User user) {
        if (user.getRole() != UserRoleEnum.ADMIN) {
            throw new CustomException(CustomErrorCode.UNAUTHORIZED_ACCESS);
        }

        Daily daily = boardRepository.findById(boardId).orElseThrow(
                () -> new CustomException(CustomErrorCode.BOARD_NOT_FOUND)
        );

        if (daily.isHasRestrict()) {
            throw new CustomException(CustomErrorCode.UNAUTHORIZED_ACCESS);
        }
        daily.restricted();
    }
}