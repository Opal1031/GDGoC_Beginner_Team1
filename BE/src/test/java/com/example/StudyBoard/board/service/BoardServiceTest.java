package com.example.StudyBoard.board.service;

import com.example.StudyBoard.board.dto.request.BoardCreateRequest;
import com.example.StudyBoard.board.dto.request.BoardEditRequest;
import com.example.StudyBoard.board.dto.response.BoardResponse;
import com.example.StudyBoard.board.entity.Board;
import com.example.StudyBoard.board.repository.BoardRepository;
import com.example.StudyBoard.constant.BoardStatus;
import com.example.StudyBoard.constant.Role;
import com.example.StudyBoard.exception.BusinessException;
import com.example.StudyBoard.exception.ErrorCode;
import com.example.StudyBoard.member.entity.Member;
import com.example.StudyBoard.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class BoardServiceTest {

    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member testMember;

    //고정시간
    private final LocalDateTime FIXED_START = LocalDateTime.of(2026, 3, 1, 10, 0);
    private final LocalDateTime FIXED_END = FIXED_START.plusDays(7);

    @BeforeEach
    void setUp() {
        //member 생성
        testMember = new Member();
        ReflectionTestUtils.setField(testMember, "email", "test1@naver.com");
        ReflectionTestUtils.setField(testMember, "name", "test1");
        ReflectionTestUtils.setField(testMember, "password", "password123");
        ReflectionTestUtils.setField(testMember, "role", Role.USER);
        memberRepository.save(testMember);
    }
    //board 생성
    private BoardCreateRequest createBoardRequest(String title, String content, int capacity) {
        return new BoardCreateRequest(title, content, capacity, FIXED_START.toLocalDate(), FIXED_END.toLocalDate(), FIXED_END.plusDays(1).toLocalDate(), FIXED_END.plusDays(30).toLocalDate());
    }

    private BoardEditRequest createEditRequest(
            String title,
            String content,
            int capacity,
            LocalDateTime recruitStart,
            LocalDateTime recruitEnd,
            LocalDateTime studyStart,
            LocalDateTime studyEnd
    ) {
        BoardEditRequest request = new BoardEditRequest();
        ReflectionTestUtils.setField(request, "title", title);
        ReflectionTestUtils.setField(request, "content", content);
        ReflectionTestUtils.setField(request, "capacity", capacity);
        ReflectionTestUtils.setField(request, "recruitmentStartDate", recruitStart);
        ReflectionTestUtils.setField(request, "recruitmentEndDate", recruitEnd);
        ReflectionTestUtils.setField(request, "studyStartDate", studyStart);
        ReflectionTestUtils.setField(request, "studyEndDate", studyEnd);
        return request;
    }


    @Test
    @DisplayName("게시글 생성 태스트")
    void createBoard_success() {
        BoardCreateRequest request =
                createBoardRequest("test board-1", "test -1", 5);

        // when
        BoardResponse response =
                boardService.create(request, testMember.getMemberId());

        // then
        assertThat(response.getTitle()).isEqualTo("test board-1");
        assertThat(response.getContent()).isEqualTo("test -1");
        assertThat(response.getCapacity()).isEqualTo(5);
        assertThat(response.getRecruitmentStartDate()).isEqualTo(FIXED_START.toLocalDate().atStartOfDay());
        assertThat(response.getRecruitmentEndDate()).isEqualTo(FIXED_END.toLocalDate().atTime(23, 59, 59));
        assertThat(response.getWriterName()).isEqualTo(testMember.getName());
        assertThat(response.getStatus()).isEqualTo(BoardStatus.RECRUITING);
    }

    @Test
    @DisplayName("게시글 생성 실패 - 스터디 시작일이 종료일 이후")
    void createBoard_fail_invalidStudyPeriod() {
        BoardCreateRequest request = new BoardCreateRequest(
                "test",
                "content",
                5,
                FIXED_START.toLocalDate(),
                FIXED_END.toLocalDate(),
                FIXED_END.plusDays(5).toLocalDate(),
                FIXED_END.plusDays(1).toLocalDate()
        );

        assertThatThrownBy(() -> boardService.create(request, testMember.getMemberId())).isInstanceOf(BusinessException.class).hasFieldOrPropertyWithValue("errorCode",ErrorCode.INVALID_STUDY_PERIOD);
    }

    @Test
    @DisplayName("게시글 생성 실패 - 모집 종료일이 스터디 시작일 이후")
    void createBoard_fail_invalidPeriodSequence() {
        BoardCreateRequest request = new BoardCreateRequest(
                "test",
                "content",
                5,
                FIXED_START.toLocalDate(),
                FIXED_END.plusDays(10).toLocalDate(),
                FIXED_END.plusDays(5).toLocalDate(),
                FIXED_END.plusDays(30).toLocalDate()
        );

        assertThatThrownBy(() -> boardService.create(request, testMember.getMemberId())).isInstanceOf(BusinessException.class).hasFieldOrPropertyWithValue("errorCode",ErrorCode.INVALID_PERIOD_SEQUENCE);
    }


    @Test
    @DisplayName("게시글 수정 테스트")
    void editBoard_success() {
        // given
        Board board = Board.create(
                testMember,
                "test board",
                5,
                "test content",
                FIXED_START,
                FIXED_END,
                FIXED_END.plusDays(1),
                FIXED_END.plusDays(30)
        );
        boardRepository.save(board);

        //수정 시간
        LocalDateTime newStart = FIXED_START.plusDays(2);
        LocalDateTime newEnd = FIXED_END.plusDays(5);

        BoardEditRequest editRequest =
                createEditRequest("edit board", "edit content", 10, newStart, newEnd,newEnd.plusDays(1),newEnd.plusDays(30));

        // when
        BoardResponse response =
                boardService.edit(board.getBoardId(), testMember.getMemberId(), editRequest);

        // then
        assertThat(response.getTitle()).isEqualTo("edit board");
        assertThat(response.getContent()).isEqualTo("edit content");
        assertThat(response.getCapacity()).isEqualTo(10);
        assertThat(response.getRecruitmentStartDate()).isEqualTo(newStart);
        assertThat(response.getRecruitmentEndDate()).isEqualTo(newEnd);
    }
    @Test
    @DisplayName("게시글 수정 실패 - 참여 인원보다 적은 정원으로 변경 불가")
    void editBoard_fail_capacityExceeded(){
        //given
        Board board = Board.create(
                testMember,
                "test board",
                5,
                "test content",
                FIXED_START,
                FIXED_END,
                FIXED_END.plusDays(1),
                FIXED_END.plusDays(30)
        );
        boardRepository.save(board);

        ReflectionTestUtils.setField(board, "currentCount", 4);
        boardRepository.save(board);

        BoardEditRequest editRequest = createEditRequest("test board", "test content", 3, FIXED_START, FIXED_END,FIXED_END.plusDays(1),FIXED_END.plusDays(30));

        //when&then
        assertThatThrownBy(() -> boardService.edit(board.getBoardId(), testMember.getMemberId(), editRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CAPACITY_EXCEEDED);
    }

    @Test
    @DisplayName("게시글 수정 실패 - 작성자가 아님")
    void editBoard_fail_notWriter() {
        // given
        Board board = Board.create(
                testMember,
                "test2 edit board",
                5,
                "test2 edit content",
                FIXED_START,
                FIXED_END,
                FIXED_END.plusDays(1),
                FIXED_END.plusDays(30)
        );
        boardRepository.save(board);
        //test2 member 생성
        Member other = new Member();
        ReflectionTestUtils.setField(other, "email", "test2@naver.com");
        ReflectionTestUtils.setField(other, "name", "test2");
        ReflectionTestUtils.setField(other, "password", "password123");
        ReflectionTestUtils.setField(other, "role", Role.USER);
        memberRepository.save(other);

        BoardEditRequest editRequest =
                createEditRequest("edit title", "edit content", 5,
                        FIXED_START, FIXED_END,FIXED_END.plusDays(1),FIXED_END.plusDays(30));

        // when & then
        assertThatThrownBy(() ->
                boardService.edit(board.getBoardId(), other.getMemberId(), editRequest)
        )
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_ACCESS);
    }

    @Test
    @DisplayName("게시글 수정 실패 - 스터디 시작일이 종료일 이후")
    void editBoard_fail_invalidStudyPeriod(){
        Board board = Board.create(
                testMember,
                "title",
                5,
                "content",
                FIXED_START,
                FIXED_END,
                FIXED_END.plusDays(1),
                FIXED_END.plusDays(30)
        );

        boardRepository.save(board);

        BoardEditRequest request = createEditRequest(
                "title",
                "content",
                5,
                FIXED_START,
                FIXED_END,
                FIXED_END.plusDays(5),
                FIXED_END.plusDays(1)
        );

        assertThatThrownBy(() -> boardService.edit(board.getBoardId(), testMember.getMemberId(), request)).isInstanceOf(BusinessException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_STUDY_PERIOD);
    }

    @Test
    @DisplayName("게시글 수정 실패 - 모집 종료일이 스터디 시작일 이후")
    void editBoard_fail_invalidPeriodSequence(){
        Board board = Board.create(
                testMember,
                "title",
                5,
                "content",
                FIXED_START,
                FIXED_END,
                FIXED_END.plusDays(1),
                FIXED_END.plusDays(30)
        );

        boardRepository.save(board);

        BoardEditRequest request = createEditRequest(
                "title",
                "content",
                5,
                FIXED_START,
                FIXED_END.plusDays(10),
                FIXED_END.plusDays(5),
                FIXED_END.plusDays(30)
        );

        assertThatThrownBy(() -> boardService.edit(board.getBoardId(), testMember.getMemberId(), request)).isInstanceOf(BusinessException.class).hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PERIOD_SEQUENCE);
    }

    @Test
    @DisplayName("게시글 삭제 테스트")
    void deleteBoard_success() {
        // given
        Board board = Board.create(
                testMember,
                "delete test board",
                5,
                "delete test content",
                FIXED_START,
                FIXED_END,
                FIXED_END.plusDays(1),
                FIXED_END.plusDays(30)
        );
        boardRepository.save(board);

        // when
        boardService.delete(board.getBoardId(), testMember.getMemberId());

        // then
        assertThat(boardRepository.findById(board.getBoardId())).isEmpty();
    }

    @Test
    @DisplayName("모집 중인 게시글 목록 조회")
    void getRecruitingBoards() {
        // given
        Board board1 = Board.create(
                testMember, "test title 1", 5, "content 1",
                FIXED_START, FIXED_END, FIXED_END.plusDays(1),FIXED_END.plusDays(30)
        );
        Board board2 = Board.create(
                testMember, "test title 2", 3, "content 2",
                FIXED_START, FIXED_END, FIXED_END.plusDays(1),FIXED_END.plusDays(30)
        );
        boardRepository.saveAll(List.of(board1, board2));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<BoardResponse> page = boardService.getRecruitingBoards(pageable);
        List<BoardResponse> responses = page.getContent();

        // then
        assertThat(responses).hasSizeGreaterThanOrEqualTo(2);
        assertThat(responses)
                .extracting(BoardResponse::getTitle)
                .contains("test title 1", "test title 2");
        assertThat(responses)
                .allMatch(r -> r.getStatus() == BoardStatus.RECRUITING);
    }

    @Test
    @DisplayName("모집완료 게시글 목록 조회")
    void getClosedBoards() {
        // given
        Board board1 = Board.create(
                testMember, "test title 1", 5, "content 1",
                FIXED_START, FIXED_END, FIXED_END.plusDays(1),FIXED_END.plusDays(30)
        );
        Board board2 = Board.create(
                testMember, "test title 2", 3, "content 2",
                FIXED_START, FIXED_END, FIXED_END.plusDays(1),FIXED_END.plusDays(30)
        );

        ReflectionTestUtils.setField(board1, "status", BoardStatus.CLOSED);
        ReflectionTestUtils.setField(board2, "status", BoardStatus.CLOSED);

        boardRepository.saveAll(List.of(board1, board2));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<BoardResponse> page = boardService.getClosedBoards(pageable);
        List<BoardResponse> responses = page.getContent();

        // then
        assertThat(responses).hasSizeGreaterThanOrEqualTo(2);
        assertThat(responses)
                .extracting(BoardResponse::getTitle)
                .contains("test title 1", "test title 2");
        assertThat(responses)
                .allMatch(r -> r.getStatus() == BoardStatus.CLOSED);
    }

    @Test
    @DisplayName("전체 게시글 목록 조회")
    void getAllBoards() {
        // given
        Board board1 = Board.create(
                testMember, "test title 1", 5, "content 1",
                FIXED_START, FIXED_END, FIXED_END.plusDays(1),FIXED_END.plusDays(30)
        );
        Board board2 = Board.create(
                testMember, "test title 2", 3, "content 2",
                FIXED_START, FIXED_END, FIXED_END.plusDays(1),FIXED_END.plusDays(30)
        );

        ReflectionTestUtils.setField(board1, "status", BoardStatus.CLOSED);

        boardRepository.saveAll(List.of(board1, board2));

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<BoardResponse> page = boardService.getAllBoards(pageable);
        List<BoardResponse> responses = page.getContent();

        // then
        assertThat(responses).hasSizeGreaterThanOrEqualTo(2);
        assertThat(responses)
                .extracting(BoardResponse::getTitle)
                .contains("test title 1", "test title 2");
    }

    @Test
    @DisplayName("게시글 단건 조회 성공")
    void getBoard_success() {
        // given
        Board board = Board.create(
                testMember,
                "test title 1",
                5,
                "test content 1",
                FIXED_START,
                FIXED_END,
                FIXED_END.plusDays(1),
                FIXED_END.plusDays(30)
        );
        boardRepository.save(board);

        // when
        BoardResponse response = boardService.get(board.getBoardId());

        // then
        assertThat(response.getTitle()).isEqualTo("test title 1");
        assertThat(response.getContent()).isEqualTo("test content 1");
        assertThat(response.getCapacity()).isEqualTo(5);
        assertThat(response.getWriterName()).isEqualTo(testMember.getName());
        assertThat(response.getStatus()).isEqualTo(BoardStatus.RECRUITING);
    }

    //pageable 테스트
    @Test
    @DisplayName("pageable 테스트")
    void pageable_success(){

        boardRepository.deleteAll();

        for(int i=1; i<=15; i++) {
            Board board = Board.create(
                    testMember,
                    "test" + i,
                    5,
                    "test content",
                    FIXED_START,
                    FIXED_END,
                    FIXED_END.plusDays(1),
                    FIXED_END.plusDays(30)
            );
            boardRepository.save(board);
        }

        Pageable pageable = PageRequest.of(0, 10, Sort.by("boardId").descending());

        Page<BoardResponse> result = boardService.getRecruitingBoards(pageable);

        assertThat(result.getContent()).hasSize(10);
        assertThat(result.getTotalElements()).isEqualTo(15);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.hasNext()).isTrue();

        assertThat(result.getContent().get(0).getBoardId())
                .isGreaterThan(result.getContent().get(1).getBoardId());

    }
}