package com.example.StudyBoard.board.service;

import com.example.StudyBoard.board.dto.request.BoardCreateRequest;
import com.example.StudyBoard.board.dto.request.BoardEditRequest;
import com.example.StudyBoard.board.dto.response.BoardResponse;
import com.example.StudyBoard.board.entity.Board;
import com.example.StudyBoard.board.repository.BoardRepository;
import com.example.StudyBoard.constant.BoardStatus;
import com.example.StudyBoard.exception.BusinessException;
import com.example.StudyBoard.exception.ErrorCode;
import com.example.StudyBoard.member.entity.Member;
import com.example.StudyBoard.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    //게시글 생성
    public BoardResponse create(BoardCreateRequest request, Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        Board board = Board.create(
                member,
                request.getTitle(),
                request.getCapacity(),
                request.getContent(),
                request.getRecruitmentStartDate().atStartOfDay(),
                request.getRecruitmentEndDate().atTime(23, 59, 59),
                request.getStudyStartDate().atStartOfDay(),
                request.getStudyEndDate().atTime(23, 59, 59)
        );

        Board savedBoard = boardRepository.save(board);
        return BoardResponse.from(savedBoard);
    }


    private Board findBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));
    }


    //단건 조회
    @Transactional(readOnly = true)
    public BoardResponse get(Long boardId) {
        return BoardResponse.from(findBoard(boardId));
    }

    // 모집중인 게시글 목록 조회
    @Transactional(readOnly = true)
    public Page<BoardResponse> getRecruitingBoards(Pageable pageable) {
        return boardRepository.findAllByStatus(BoardStatus.RECRUITING, pageable)
                .map(BoardResponse::from);
    }

    //모집완료 게시글 목록 조회
    @Transactional(readOnly = true)
    public Page<BoardResponse> getClosedBoards(Pageable pageable){
        return boardRepository.findAllByStatus(BoardStatus.CLOSED, pageable)
                .map(BoardResponse::from);
    }

    //전체 게시글 목록 조회
    @Transactional(readOnly = true)
    public Page<BoardResponse> getAllBoards(Pageable pageable){
        return boardRepository.findAll(pageable)
                .map(BoardResponse::from);
    }

    //내가 만든 스터디 목록 조회
    @Transactional(readOnly = true)
    public Page<BoardResponse> getMyBoards(Long memberId, Pageable pageable){
        return boardRepository.findAllByMember_MemberId(memberId, pageable)
                .map(BoardResponse::from);
    }

    //게시글 삭제
    public void delete(Long boardId, Long memberId) {
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));

        //작성자만 삭제 가능
        if(!board.getMember().getMemberId().equals(memberId)){
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }

        boardRepository.delete(board);
    }

    //게시글 수정
    public BoardResponse edit(Long boardId, Long memberId, BoardEditRequest request){
        Board board = boardRepository.findById(boardId).orElseThrow(() -> new BusinessException(ErrorCode.BOARD_NOT_FOUND));

        if(!board.getMember().getMemberId().equals(memberId)){
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }

        board.update(
                request.getTitle(),
                request.getContent(),
                request.getCapacity(),
                request.getRecruitmentStartDate(),
                request.getRecruitmentEndDate(),
                request.getStudyStartDate(),
                request.getStudyEndDate()
        );

        return BoardResponse.from(board);
    }
}