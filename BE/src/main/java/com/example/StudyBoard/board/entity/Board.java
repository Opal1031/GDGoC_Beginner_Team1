package com.example.StudyBoard.board.entity;

import com.example.StudyBoard.application.entity.Application;
import com.example.StudyBoard.constant.BoardStatus;
import com.example.StudyBoard.entity.BaseEntity;
import com.example.StudyBoard.exception.BusinessException;
import com.example.StudyBoard.exception.ErrorCode;
import com.example.StudyBoard.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Board extends BaseEntity {
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

    //board-Application
    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Application> applications = new ArrayList<>();

//최대정원
    @Column(nullable = false)
    private int capacity;

//현재 신청 인원
    @Column(nullable = false)
    private int currentCount;


    //스터디 기간
    @Column(name = "study_start_date",nullable = false)
    private LocalDateTime studyStartDate;

    @Column(name = "study_end_date", nullable = false)
    private LocalDateTime studyEndDate;

//모집 게시글 생성자
    public Board(Member member, String title, int capacity, String content, LocalDateTime recruitmentStartDate, LocalDateTime recruitmentEndDate, LocalDateTime studyStartDate, LocalDateTime studyEndDate) {
        this.member = member;
        this.title = title;
        this.content = content;
        this.capacity = capacity;
        this.currentCount = 0;
        this.status = BoardStatus.RECRUITING;
        this.recruitmentStartDate = recruitmentStartDate;
        this.recruitmentEndDate = recruitmentEndDate;
        this.studyStartDate = studyStartDate;
        this.studyEndDate = studyEndDate;
    }

//신청인원 처리
    public void apply(LocalDateTime now) {
        validateRecruitmentAvailable(now);

        this.currentCount++;

        if (this.currentCount >= this.capacity) {
            this.status = BoardStatus.CLOSED;
        }
    }

    private void validateRecruitmentAvailable(LocalDateTime now) {

        if (this.status == BoardStatus.CLOSED) {
            throw new BusinessException(ErrorCode.RECRUITMENT_CLOSED);
        }

        if (now.isBefore(recruitmentStartDate) || now.isAfter(recruitmentEndDate)) {
            throw new BusinessException(ErrorCode.RECRUITMENT_PERIOD_INVALID);
        }

        if (this.currentCount >= this.capacity) {
            throw new BusinessException(ErrorCode.CAPACITY_EXCEEDED);
        }
    }

    public static Board create(
            Member member,
            String title,
            int capacity,
            String content,
            LocalDateTime recruitStart,
            LocalDateTime recruitEnd,
            LocalDateTime studyStart,
            LocalDateTime studyEnd

    ) {

        if (member == null) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        if (capacity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (!recruitStart.isBefore(recruitEnd)) {
            throw new BusinessException(ErrorCode.INVALID_RECRUITMENT_PERIOD);
        }

        if(!studyStart.isBefore(studyEnd)) {
            throw new BusinessException(ErrorCode.INVALID_STUDY_PERIOD);
        }

        if (studyStart.isBefore(recruitEnd)) {
            throw new BusinessException(ErrorCode.INVALID_PERIOD_SEQUENCE);
        }
        return new Board(member, title, capacity, content, recruitStart, recruitEnd, studyStart, studyEnd);
    }

    public void release(){
        if(this.currentCount > 0) {
            this.currentCount--;
        }

        if(this.status == BoardStatus.CLOSED && this.currentCount <this.capacity) {
            this.status = BoardStatus.RECRUITING;
        }
    }

    //게시글 수정
    public void update(String title, String content, int capacity, LocalDateTime recruitStart, LocalDateTime recruitEnd, LocalDateTime studyStart, LocalDateTime studyEnd) {
        if(capacity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if(!recruitStart.isBefore(recruitEnd)) {
            throw new BusinessException(ErrorCode.INVALID_RECRUITMENT_PERIOD);
        }

        if(!studyStart.isBefore(studyEnd)) {
            throw new BusinessException(ErrorCode.INVALID_STUDY_PERIOD);
        }

        if (studyStart.isBefore(recruitEnd)) {
            throw new BusinessException(ErrorCode.INVALID_PERIOD_SEQUENCE);
        }

        if(this.currentCount > capacity) {
            throw new BusinessException(ErrorCode.CAPACITY_EXCEEDED);
        }

        this.title = title;
        this.content = content;
        this.capacity = capacity;
        this.recruitmentStartDate = recruitStart;
        this.recruitmentEndDate = recruitEnd;
        this.studyStartDate = studyStart;
        this.studyEndDate = studyEnd;

        //CLOSE->OPEN
        if (this.status == BoardStatus.CLOSED && this.currentCount < capacity) {
            this.status = BoardStatus.RECRUITING;
        }
    }
}