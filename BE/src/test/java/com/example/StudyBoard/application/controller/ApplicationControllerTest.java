package com.example.StudyBoard.application.controller;

import com.example.StudyBoard.auth.dto.LoginRequest;
import com.example.StudyBoard.board.dto.request.BoardCreateRequest;
import com.example.StudyBoard.member.dto.request.MemberRegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class ApplicationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("내가 신청한 스터디 목록 조회 성공")
    public void successGetMyAppliedBoards() throws Exception{
        //given
        MemberRegisterRequest registerRequest = new MemberRegisterRequest(
                "test@naver.com",
                "테스트유저",
                "12345678"
        );
        mockMvc.perform(post("/members/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest(
                "test@naver.com",
                "12345678"
        );

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String accessToken = objectMapper.readTree(loginResponse).get("accessToken").asText();

        BoardCreateRequest boardCreateRequest = new BoardCreateRequest("신청 대상 스터디", "내용", 5, LocalDate.now(), LocalDate.now().plusDays(3), LocalDate.now().plusDays(4), LocalDate.now().plusDays(30));
        String boardResponse = mockMvc.perform(post("/boards/create")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(boardCreateRequest)))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

        Long boardId = objectMapper.readTree(boardResponse).get("boardId").asLong();

        MemberRegisterRequest applicantRegister = new MemberRegisterRequest(
                "applicant@naver.com",
                "신청자",
                "12345678"
        );
        mockMvc.perform(post("/members/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applicantRegister)))
                .andExpect(status().isCreated());

        LoginRequest applicantLogin = new LoginRequest(
                "applicant@naver.com",
                "12345678"
        );

        String applicantResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applicantLogin)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String applicantToken = objectMapper.readTree(applicantResponse).get("accessToken").asText();

        //when
        //then
        mockMvc.perform(post("/applications/boards/{boardId}", boardId)
                .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/applications/my")
                .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isOk());
    }
}
