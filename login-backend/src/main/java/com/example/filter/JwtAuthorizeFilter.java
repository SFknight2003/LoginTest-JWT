package com.example.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthorizeFilter extends OncePerRequestFilter {  // 每次请求都会执行一次

    @Resource
    JwtUtils utils;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");  // 该请求头会携带token
        DecodedJWT jwt = utils.resolveJwt(authorization);   // 解析jwt
        if (jwt != null) {  // 不等于空才可进行授权
            UserDetails user = utils.toUser(jwt);    // 解析用户
            // SpringSecurity内部验证token
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user,null, user.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails((request)));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            request.setAttribute("id", utils.toId(jwt));
        }
        filterChain.doFilter(request,response);
    }

}
