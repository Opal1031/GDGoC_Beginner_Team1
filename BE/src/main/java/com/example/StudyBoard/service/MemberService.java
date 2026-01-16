package com.example.StudyBoard.service;

import com.example.StudyBoard.constant.Role;
import com.example.StudyBoard.dto.request.MemberLoginRequest;
import com.example.StudyBoard.dto.request.MemberRegisterRequest;
import com.example.StudyBoard.dto.response.MemberInfoResponse;
import com.example.StudyBoard.entity.Member;
import com.example.StudyBoard.exception.BusinessException;
import com.example.StudyBoard.exception.ErrorCode;
import com.example.StudyBoard.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public void registerMember(MemberRegisterRequest request){
        if(memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATED_EMAIL);
        }

        Member member = new Member(request.email(), request.name(), passwordEncoder.encode(request.password()), Role.USER);
        memberRepository.save(member);
    }

    public MemberInfoResponse loginMember(MemberLoginRequest request){
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.NULL_MEMBER));

        if(!passwordEncoder.matches(request.password(), member.getPassword())){
            throw new BusinessException(ErrorCode.INCORRECT_PASSWORD);
        }

        return new MemberInfoResponse(member.getMemberId(), member.getEmail(), member.getName(), member.getRole());
    }


}
