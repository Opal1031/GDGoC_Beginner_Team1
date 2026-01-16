package com.example.StudyBoard.controller;

import com.example.StudyBoard.constant.Role;
import com.example.StudyBoard.dto.request.MemberLoginRequest;
import com.example.StudyBoard.dto.request.MemberRegisterRequest;
import com.example.StudyBoard.entity.Member;
import com.example.StudyBoard.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @DisplayName("회원가입 성공 테스트")
    public void register() throws Exception{
        //given
        MemberRegisterRequest request = new MemberRegisterRequest(
                "test@naver.com",
                "테스트유저",
                "password1234"
        );
        //when

        //then
        mockMvc.perform(post("/members/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());
    }


    @Test
    @DisplayName("회원가입 실패 테스트")
    public void failedRegister() throws Exception{
        //given
        MemberRegisterRequest request = new MemberRegisterRequest(
                "not-email-format",
                "테스트유저",
                "password1234"
        );

        // when
        // then
        mockMvc.perform(post("/members/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("-001"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    public void login() throws Exception{
        //given
        MemberRegisterRequest registerRequest = new MemberRegisterRequest(
                "test@naver.com",
                "테스트유저",
                "12345678"
        );
        //when
        mockMvc.perform(post("/members/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        MemberLoginRequest memberLoginRequest = new MemberLoginRequest(
                "test@naver.com",
                "12345678"
        );
        //then
        mockMvc.perform(post("/members/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(memberLoginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@naver.com"))
                .andExpect(jsonPath("$.name").value("테스트유저"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("로그인 실패 테스트 - 비밀번호 불일치")
    public void failedLogin() throws Exception{
        //given
        MemberRegisterRequest registerRequest = new MemberRegisterRequest(
                "test@naver.com",
                "테스트유저",
                "12345678"
        );
        //when
        mockMvc.perform(post("/members/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());


        MemberLoginRequest memberLoginRequest = new MemberLoginRequest(
                "test@naver.com",
                "1234567810"
        );
        //then
        mockMvc.perform(post("/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(memberLoginRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("-102"))
                .andExpect(jsonPath("$.errorDescription").value("비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("회원가입 실패 테스트 - 중복 이메일")
    public void duplicatedEmail() throws Exception{
        //given
        MemberRegisterRequest registerRequest = new MemberRegisterRequest(
                "test@naver.com",
                "테스트유저",
                "12345678"
        );

        mockMvc.perform(post("/members/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpect(status().isCreated());
        //when
        MemberRegisterRequest duplicateRequest = new MemberRegisterRequest(
                "test@naver.com",
                "테스트유저2",
                "12345678"
        );
        //then
        mockMvc.perform(post("/members/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("-100"))
                .andExpect(jsonPath("$.errorDescription").value("이미 가입된 이메일입니다."));
    }

}