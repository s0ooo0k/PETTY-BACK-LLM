package io.github.petty.users.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.petty.users.dto.CustomUserDetails;
import io.github.petty.users.dto.LoginDTO;
import io.github.petty.users.entity.Users;
import io.github.petty.users.repository.UsersRepository;
import io.github.petty.users.service.RefreshTokenService;
import io.github.petty.users.util.CookieUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UsersRepository usersRepository;
    private final CookieUtils cookieUtils;

    // expirationTime 주입
    @Value("${jwt.expiration-time}")
    private long expirationTime;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil, RefreshTokenService refreshTokenService, UsersRepository usersRepository, CookieUtils cookieUtils) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.usersRepository = usersRepository;
        this.cookieUtils = cookieUtils;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username;
        String password;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            LoginDTO loginDTO = objectMapper.readValue(request.getInputStream(), LoginDTO.class);

            username = loginDTO.getUsername();
            password = loginDTO.getPassword();

        } catch (IOException e) {
            throw new AuthenticationServiceException("Failed to parse JSON request", e);
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);
        authToken.setDetails(this.authenticationDetailsSource.buildDetails(request));

        return this.authenticationManager.authenticate(authToken);
    }

    //로그인 성공시 (여기서 JWT를 발급)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        // 액세스 토큰 생성
        String token = jwtUtil.createJwt(username, role, 3600000L); // expirationTime

        // 사용자 조회
        Users user = usersRepository.findByUsername(username);

        // 리프레시 토큰 생성
        UUID refreshToken = refreshTokenService.createRefreshToken(user);

        // 쿠키 설정 코드
        cookieUtils.setTokenCookies(response, token, refreshToken);
    }

    //로그인 실패시
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(401);
    }
}