package com.example.emotrak.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ReportResponseDto {
    private long totalCount;
    private List<ReportHistory> contents = new ArrayList<>();

    public ReportResponseDto(long totalCount, List<ReportHistory> reportHistoryList) {
        this.totalCount = totalCount;
        this.contents = reportHistoryList;
    }
}
