package com.kbhc.codetest.api.auth.service;

import com.kbhc.codetest.entity.member.Member;
import com.kbhc.codetest.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return memberRepository.findByEmail(email)
                .map(this::createUserDetails)
                .orElseThrow(() ->
                        new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }

    private UserDetails createUserDetails(Member member) {
        // admin 기능 추가시 DB에 컬럼 추가하여 SimpleGrantedAuthority에 셋팅
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .authorities(Collections.singleton(authority)) // 권한 부여
                .build();
    }
}
