package com.litCitrus.zamongcampusServer.security.jwt;

import com.litCitrus.zamongcampusServer.repository.jwt.RefreshTokenRepository;
import com.litCitrus.zamongcampusServer.util.CookieAndHeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

/**
 * resolveToken은 header의 token을 꺼내는 역할
 * doFilter는 jwt 토큰 인증 정보를 현재 실행중인 스레드(securityContext, 41줄)에 저장.
 */
public class JwtFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtFilter(TokenProvider tokenProvider, RefreshTokenRepository refreshTokenRepository) {
        this.tokenProvider = tokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * doFilter는 jwt 토큰 인증 정보를 현재 실행중인 스레드(securityContext, 417줄)에 저장.
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String requestURI = httpServletRequest.getRequestURI();
        String jwt = CookieAndHeaderUtil.resolveToken(httpServletRequest).orElse(null);

        if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) { // token 값이 정상인지 확인
            logger.debug("jwt가 유효합니다.");

            CookieAndHeaderUtil.readCookie((HttpServletRequest) servletRequest, CookieAndHeaderUtil.REFRESH_TOKEN_KEY)
                    .ifPresent(refreshToken -> {
                        refreshTokenRepository.findUserByRefreshTokenAndAccessToken(refreshToken, jwt)
                                .ifPresent(jwtToken -> {
                                            if (jwtToken.isValid()) {
                                                Authentication authentication = tokenProvider.getAuthentication(jwt, Optional.of(jwtToken.getUser()));
                                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                                logger.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}", authentication.getName(), requestURI);
                                            }
                                        }
                                );
                    });
        } else {
            logger.debug("유효한 JWT 토큰이 없습니다, uri: {}", requestURI);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
