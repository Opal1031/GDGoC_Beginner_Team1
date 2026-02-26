package com.example.StudyBoard.board.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BoardCreateRequest {
    @NotBlank(message = "제목을 입력해주세요")
    private String title;

    @NotBlank(message = "내용을 입력해주세요")
    private String content;

    @Min(value = 1, message = "모집 인원은 최소 1명 이상이어야 합니다")
    private int capacity;

    @NotNull(message = "모집 시작일을 입력해주세요")
    @FutureOrPresent(message = "모집 시작일은 현재 시점 이후여야 합니다")
    private LocalDate recruitmentStartDate;

    @NotNull(message = "모집 종료일을 입력해주세요")
    @Future(message = "모집 종료일은 현재 시점 이후여야 합니다")
    private LocalDate recruitmentEndDate;

    @NotNull(message = "스터디 시작일을 입력해주세요")
    private LocalDate studyStartDate;

    @NotNull(message = "스터디 종료일을 입력해주세요")
    @Future(message = "스터디 종료일은 현재 시점 이후여야 합니다")
    private LocalDate studyEndDate;
}
