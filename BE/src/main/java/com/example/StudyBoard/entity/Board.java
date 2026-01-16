package com.example.StudyBoard.entity;

import com.example.StudyBoard.constant.BoardStatus;
import com.example.StudyBoard.exception.BusinessException;
import com.example.StudyBoard.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Board extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long boardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BoardStatus status;

    @Column(name = "recruitment_start_date", nullable = false)
    private LocalDateTime recruitmentStartDate;

    @Column(name = "recruitment_end_date", nullable = false)
    private LocalDateTime recruitmentEndDate;

    //최대정원
    @Column(nullable = false)
    private int capacity;

    //현재 신청 인원
    @Column(nullable = false)
    private int currentCount;

    //모집 게시글 생성자
    public Board(Member member, String title, int capacity, String content, LocalDateTime recruitmentStartDate, LocalDateTime recruitmentEndDate) {
        this.member = member;
        this.title = title;
        this.content = content;
        this.capacity = capacity;
        this.currentCount = 0;
        this.status = BoardStatus.RECRUITING;
        this.recruitmentStartDate = recruitmentStartDate;
        this.recruitmentEndDate = recruitmentEndDate;
    }

    //신청인원 처리
    public void apply() {
        //게시글 상태 확인
        if (this.status == BoardStatus.CLOSED) {
            throw new BusinessException(ErrorCode.RECRUITMENT_CLOSED);
        }

        //신청인원 초과 여부
        if (this.currentCount >= this.capacity) {
            throw new BusinessException(ErrorCode.CAPACITY_EXCEEDED);
        }
        this.currentCount++;
    }
}
